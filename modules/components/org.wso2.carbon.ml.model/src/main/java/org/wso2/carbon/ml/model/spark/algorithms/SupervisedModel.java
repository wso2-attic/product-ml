/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.model.spark.algorithms;

import org.apache.commons.math3.stat.regression.ModelSpecificationException;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.dto.Workflow;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.model.exceptions.AlgorithmNameException;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.internal.MLModelUtils;
import org.wso2.carbon.ml.model.internal.ds.MLModelServiceValueHolder;
import org.wso2.carbon.ml.model.spark.dto.ClassClassificationAndRegressionModelSummary;
import org.wso2.carbon.ml.model.spark.dto.ProbabilisticClassificationModelSummary;
import org.wso2.carbon.ml.model.spark.transformations.DoubleArrayToLabeledPoint;
import scala.Tuple2;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.IMPURITY;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.ITERATIONS;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.LEARNING_RATE;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.MAX_BINS;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.MAX_DEPTH;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.NUM_CLASSES;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.RANDOM_SEED;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.REGULARIZATION_PARAMETER;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.REGULARIZATION_TYPE;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.SGD_DATA_FRACTION;
import static org.wso2.carbon.ml.model.internal.constants.MLModelConstants.SUPERVISED_ALGORITHM;

//import org.wso2.carbon.ml.model.internal.DatabaseHandler;

public class SupervisedModel {
    /**
     * @param modelID   Model ID
     * @param workflow  Workflow ID
     * @param sparkConf Spark configuration
     * @throws ModelServiceException
     */
    public void buildModel(String modelID, Workflow workflow, SparkConf sparkConf)
            throws ModelServiceException {
        try {
            sparkConf.setAppName(modelID);
            // create a new java spark context
            JavaSparkContext sc = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            String datasetURL = workflow.getDatasetURL();
            JavaRDD<String> lines = sc.textFile(datasetURL);
            // get header line
            String headerRow = lines.take(1).get(0);
            // get column separator
            String columnSeparator = MLModelUtils.getColumnSeparator(datasetURL);
            // apply pre processing
            JavaRDD<double[]> features = SparkModelUtils.preProcess(sc, workflow, lines, headerRow,
                    columnSeparator);
            // generate train and test datasets by converting tokens to labeled points
            int responseIndex = MLModelUtils.getFeatureIndex(workflow.getResponseVariable(),
                    headerRow, columnSeparator);
            DoubleArrayToLabeledPoint doubleArrayToLabeledPoint = new DoubleArrayToLabeledPoint(responseIndex);
            JavaRDD<LabeledPoint> labeledPoints = features.map(doubleArrayToLabeledPoint);
            JavaRDD<LabeledPoint> trainingData = labeledPoints.sample(false,
                    workflow.getTrainDataFraction(), RANDOM_SEED);
            JavaRDD<LabeledPoint> testingData = labeledPoints.subtract(trainingData);
            // build a machine learning model according to user selected algorithm
            SUPERVISED_ALGORITHM supervisedAlgorithm = SUPERVISED_ALGORITHM.valueOf(
                    workflow.getAlgorithmName());
            switch (supervisedAlgorithm) {
            case LOGISTIC_REGRESSION:
                buildLogisticRegressionModel(modelID, trainingData, testingData, workflow);
                break;
            case DECISION_TREE:
                buildDecisionTreeModel(modelID, trainingData, testingData, workflow);
                break;
            case SVM:
                buildSVMModel(modelID, trainingData, testingData, workflow);
                break;
            case LINEAR_REGRESSION:
                buildLinearRegressionModel(modelID, trainingData, testingData, workflow);
                break;
            default:
                throw new AlgorithmNameException("Incorrect algorithm name");
            }
            // stop spark context
            sc.stop();
        } catch (ModelSpecificationException e) {
            throw new ModelServiceException("An error occurred while building supervised machine learning model: " +
                    e.getMessage(), e);
        }
    }

    /**
     * This method builds a logistic regression model
     *
     * @param modelID      Model ID
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param testingData  Testing data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @throws ModelServiceException
     */
    private void buildLogisticRegressionModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow) throws ModelServiceException {
        try {
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            dbService.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            LogisticRegression logisticRegression = new LogisticRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LogisticRegressionModel model = logisticRegression.trainWithSGD(trainingData,
                    Double.parseDouble(hyperParameters.get(LEARNING_RATE)),
                    Integer.parseInt(hyperParameters.get(ITERATIONS)),
                    hyperParameters.get(REGULARIZATION_TYPE),
                    Double.parseDouble(hyperParameters.get(REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(SGD_DATA_FRACTION)));
            model.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = logisticRegression.test(model,
                    testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
                    SparkModelUtils.generateProbabilisticClassificationModelSummary(scoresAndLabels);
            dbService.updateModel(modelID, model, probabilisticClassificationModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occured while building logistic regression model: "
                    + e.getMessage(), e);
        }
    }

    /**
     * This method builds a decision tree model
     *
     * @param modelID      Model ID
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param testingData  Testing data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @throws ModelServiceException
     */
    private void buildDecisionTreeModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow) throws ModelServiceException {
        try {
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            dbService.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            DecisionTree decisionTree = new DecisionTree();
            DecisionTreeModel decisionTreeModel = decisionTree.train(trainingData,
                    Integer.parseInt(hyperParameters.get(NUM_CLASSES)),
                    new HashMap<Integer, Integer>(), hyperParameters.get(IMPURITY),
                    Integer.parseInt(hyperParameters.get(MAX_DEPTH)),
                    Integer.parseInt(hyperParameters.get(MAX_BINS)));
            JavaPairRDD<Double, Double> predictionsAndLabels = decisionTree.test(decisionTreeModel,
                    trainingData);
            ClassClassificationAndRegressionModelSummary classClassificationAndRegressionModelSummary = SparkModelUtils
                    .getClassClassificationModelSummary(predictionsAndLabels);
            dbService.updateModel(modelID, decisionTreeModel, classClassificationAndRegressionModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occured while building decision tree model: " + e.getMessage(),
                    e);
        }

    }

    /**
     * This method builds a support vector machine (SVM) model
     *
     * @param modelID      Model ID
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param testingData  Testing data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @throws ModelServiceException
     */
    private void buildSVMModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow) throws ModelServiceException {
        try {
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            dbService.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            SVM svm = new SVM();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            SVMModel svmModel = svm.train(trainingData, Integer.parseInt(hyperParameters.get(ITERATIONS)),
                    hyperParameters.get(REGULARIZATION_TYPE),
                    Double.parseDouble(hyperParameters.get(REGULARIZATION_PARAMETER)),
                    Double.parseDouble(hyperParameters.get(LEARNING_RATE)),
                    Double.parseDouble(hyperParameters.get(SGD_DATA_FRACTION)));
            svmModel.clearThreshold();
            JavaRDD<Tuple2<Object, Object>> scoresAndLabels = svm.test(svmModel,
                    testingData);
            ProbabilisticClassificationModelSummary probabilisticClassificationModelSummary =
                    SparkModelUtils.generateProbabilisticClassificationModelSummary(scoresAndLabels);
            dbService.updateModel(modelID, svmModel, probabilisticClassificationModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occured while building SVM model: " + e.getMessage(), e);
        }
    }

    /**
     * This method builds a linear regression model
     *
     * @param modelID      Model ID
     * @param trainingData Training data as a JavaRDD of LabeledPoints
     * @param testingData  Testing data as a JavaRDD of LabeledPoints
     * @param workflow     Machine learning workflow
     * @throws ModelServiceException
     */
    private void buildLinearRegressionModel(String modelID, JavaRDD<LabeledPoint> trainingData,
            JavaRDD<LabeledPoint> testingData, Workflow workflow) throws ModelServiceException {
        try {
            DatabaseService dbService = MLModelServiceValueHolder.getDatabaseService();
            dbService.insertModel(modelID, workflow.getWorkflowID(),
                    new Time(System.currentTimeMillis()));
            LinearRegression linearRegression = new LinearRegression();
            Map<String, String> hyperParameters = workflow.getHyperParameters();
            LinearRegressionModel linearRegressionModel = linearRegression.train(trainingData,
                    Integer.parseInt(hyperParameters.get(ITERATIONS)),
                    Double.parseDouble(hyperParameters.get(LEARNING_RATE)),
                    Double.parseDouble(hyperParameters.get(SGD_DATA_FRACTION)));
            JavaRDD<Tuple2<Double, Double>> predictionsAndLabels = linearRegression.test(linearRegressionModel,
                    testingData);
            ClassClassificationAndRegressionModelSummary regressionModelSummary = SparkModelUtils
                    .generateRegressionModelSummary(predictionsAndLabels);
            dbService.updateModel(modelID, linearRegressionModel, regressionModelSummary,
                    new Time(System.currentTimeMillis()));
        } catch (DatabaseHandlerException e) {
            throw new ModelServiceException("An error occured while building linear regression model: "
                    + e.getMessage(), e);
        }
    }
}
