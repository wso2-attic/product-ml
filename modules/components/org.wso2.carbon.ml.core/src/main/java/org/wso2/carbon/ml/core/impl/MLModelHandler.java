/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ml.core.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.core.exceptions.MLModelBuilderException;
import org.wso2.carbon.ml.core.exceptions.MLModelHandlerException;
import org.wso2.carbon.ml.core.interfaces.MLInputAdapter;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;
import org.wso2.carbon.ml.core.internal.MLModelConfigurationContext;
import org.wso2.carbon.ml.core.spark.algorithms.KMeans;
import org.wso2.carbon.ml.core.spark.algorithms.SupervisedModel;
import org.wso2.carbon.ml.core.spark.algorithms.UnsupervisedModel;
import org.wso2.carbon.ml.core.spark.transformations.HeaderFilter;
import org.wso2.carbon.ml.core.spark.transformations.LineToTokens;
import org.wso2.carbon.ml.core.spark.transformations.MissingValuesFilter;
import org.wso2.carbon.ml.core.spark.transformations.TokensToVectors;
import org.wso2.carbon.ml.core.utils.MLCoreServiceValueHolder;
import org.wso2.carbon.ml.core.utils.MLUtils;
import org.wso2.carbon.ml.core.utils.ThreadExecutor;
import org.wso2.carbon.ml.core.utils.MLUtils.ColumnSeparatorFactory;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import scala.Tuple2;

/**
 * {@link MLModelHandler} is responsible for handling/delegating all the model related requests.
 */
public class MLModelHandler {
    private static final Log log = LogFactory.getLog(MLModelHandler.class);
    private DatabaseService databaseService;
    private Properties mlProperties;
    private ThreadExecutor threadExecutor;

    public MLModelHandler() {
        MLCoreServiceValueHolder valueHolder = MLCoreServiceValueHolder.getInstance();
        databaseService = valueHolder.getDatabaseService();
        mlProperties = valueHolder.getMlProperties();
        threadExecutor = new ThreadExecutor(mlProperties);
    }

    /**
     * Create a new model.
     * 
     * @param model model to be created.
     * @throws MLModelHandlerException
     */
    public void createModel(MLModelNew model) throws MLModelHandlerException {
        try {
            databaseService.insertModel(model);
            log.info(String.format("[Created] %s", model));
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public void deleteModel(int tenantId, String userName, long modelId) throws MLModelHandlerException {
        try {
            databaseService.deleteModel(tenantId, userName, modelId);
            log.info(String.format("[Deleted] Model [id] %s", modelId));
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public long getModelId(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getModelId(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public MLModelNew getModel(int tenantId, String userName, String modelName) throws MLModelHandlerException {
        try {
            return databaseService.getModel(tenantId, userName, modelName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public List<MLModelNew> getAllModels(int tenantId, String userName) throws MLModelHandlerException {
        try {
            return databaseService.getAllModels(tenantId, userName);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    public boolean isValidModelId(int tenantId, String userName, long modelId) throws MLModelHandlerException {
        try {
            return databaseService.isValidModelId(tenantId, userName, modelId);
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    /**
     * @param type type of the storage file, hdfs etc.
     * @param location root directory of the file location.
     * @throws MLModelHandlerException
     */
    public void addStorage(long modelId, MLStorage storage) throws MLModelHandlerException {
        try {
            databaseService.updateModelStorage(modelId, storage.getType(), storage.getLocation());
        } catch (DatabaseHandlerException e) {
            throw new MLModelHandlerException(e);
        }
    }

    /**
     * Build a ML model asynchronously and persist the built model in a given storage.
     * 
     * @param modelId id of the model to be built.
     * @param storageType type of the storage bam, hdfs, file. Default storage is file.
     * @param StoragePath path of the provided storage where the model should be saved.
     * @throws MLModelHandlerException
     * @throws MLModelBuilderException
     */
    public void buildModel(int tenantId, String userName, long modelId) throws MLModelHandlerException,
            MLModelBuilderException {

        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }
        

        /**
         * Spark looks for various configuration files using thread context class loader. Therefore, the class loader
         * needs to be switched temporarily.
         */
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            MLModelNew model = databaseService.getModel(tenantId, userName, modelId);
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            long datasetVersionId = databaseService.getDatasetVersionIdOfModel(modelId);
            // long datasetVersionId = databaseService.getDatasetVersionId(datasetVersionId);
            // long datasetId = databaseService.getDatasetId(datasetVersionId);
            String dataType = databaseService.getDataTypeOfModel(modelId);
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(dataType);
            String dataUrl = databaseService.getDatasetVersionUri(datasetVersionId);
            SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
            Workflow facts = databaseService.getWorkflow(model.getAnalysisId());

            MLModelConfigurationContext context = new MLModelConfigurationContext();
            context.setModelId(modelId);
            context.setColumnSeparator(columnSeparator);
            context.setFacts(facts);
            context.setModel(model);

            JavaSparkContext sparkContext = null;
            sparkConf.setAppName(String.valueOf(modelId));
            // create a new java spark context
            sparkContext = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            JavaRDD<String> lines = sparkContext.textFile(dataUrl);
            // get header line
            String headerRow = lines.take(1).get(0);
            context.setSparkContext(sparkContext);
            context.setLines(lines);
            context.setHeaderRow(headerRow);

            // build the model asynchronously
            threadExecutor.execute(new ModelBuilder(modelId, context));

            log.info(String.format("Build model [id] %s job is successfully submitted to Spark.", modelId));

        } catch (DatabaseHandlerException e) {
            throw new MLModelBuilderException("An error occurred while saving model to database: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    public List<?> predict(int tenantId, String userName, long modelId, String[] data) throws MLModelHandlerException,
            MLModelBuilderException {

        if (!isValidModelId(tenantId, userName, modelId)) {
            String msg = String.format("Failed to build the model. Invalid model id: %s for tenant: %s and user: %s",
                    modelId, tenantId, userName);
            throw new MLModelHandlerException(msg);
        }

        /**
         * Spark looks for various configuration files using thread context class loader. Therefore, the class loader
         * needs to be switched temporarily.
         */
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            MLModelNew model = databaseService.getModel(tenantId, userName, modelId);
            String dataType = databaseService.getDataTypeOfModel(modelId);
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(dataType);
            SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
            Workflow facts = databaseService.getWorkflow(model.getAnalysisId());

            MLModelConfigurationContext context = new MLModelConfigurationContext();
            context.setModelId(modelId);
            context.setColumnSeparator(columnSeparator);
            context.setFacts(facts);
            context.setDataToBePredicted(data);

            JavaSparkContext sparkContext = null;
            sparkConf.setAppName(String.valueOf(modelId));
            // create a new java spark context
            sparkContext = new JavaSparkContext(sparkConf);
            context.setSparkContext(sparkContext);
            
            MLModel builtModel = retrieveModel(modelId);

            // predict
            Predictor predictor = new Predictor(modelId, builtModel, context);
            List<?> predictions = predictor.predict();

            log.info(String.format("Prediction from model [id] %s was successful.", modelId));
            return predictions;

        } catch (DatabaseHandlerException e) {
            throw new MLModelBuilderException("An error occurred while saving model to database: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    private void persistModel(long modelId, String modelName, MLModel model) throws MLModelBuilderException {
        try {
            MLStorage storage = databaseService.getModelStorage(modelId);
            String storageType = storage.getType();
            String storageLocation = storage.getLocation();
            
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLOutputAdapter outputAdapter = ioFactory.getOutputAdapter(storageType + MLConstants.OUT_SUFFIX);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(model);
            oos.flush();
            oos.close();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            // adapter will write the model and close the stream.
            String outPath = storageLocation + File.separator + modelName + "." + MLUtils.getDate();
            outputAdapter.write(outPath, is);
            databaseService.updateModelStorage(modelId, storageType, outPath);
        } catch (Exception e) {
            throw new MLModelBuilderException("Failed to persist the model [id] " + modelId, e);
        }
    }
    
    public MLModel retrieveModel(long modelId) throws MLModelBuilderException {
        InputStream in = null;
        ObjectInputStream ois = null;
        try {
            MLStorage storage = databaseService.getModelStorage(modelId);
            String storageType = storage.getType();
            String storageLocation = storage.getLocation();
            MLIOFactory ioFactory = new MLIOFactory(mlProperties);
            MLInputAdapter inputAdapter = ioFactory.getInputAdapter(storageType + MLConstants.IN_SUFFIX);
            in = inputAdapter.readDataset(new URI(storageLocation));
            ois = new ObjectInputStream(in);
            return (MLModel) ois.readObject();
            
        } catch (Exception e) {
            throw new MLModelBuilderException("Failed to retrieve the model [id] " + modelId, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public List<ClusterPoint> getClusterPoints(int tenantId, String userName, long datasetId, String featureListString, int noOfClusters)
            throws DatabaseHandlerException {
        // assign current thread context class loader to a variable
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        List<String> features = Arrays.asList(featureListString.split("\\s*,\\s*"));

        try {
            List<ClusterPoint> clusterPoints = new ArrayList<ClusterPoint>();

            String datasetURL = databaseService.getDatasetUri(datasetId);
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            // class loader is switched to JavaSparkContext.class's class loader
            Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
            // create a new spark configuration
            SparkConf sparkConf = MLCoreServiceValueHolder.getInstance().getSparkConf();
            sparkConf.setAppName(String.valueOf(datasetId));
            // create a new java spark context
            JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
            // parse lines in the dataset
            JavaRDD<String> lines = sparkContext.textFile(datasetURL);
            // get header line
            String headerRow = lines.take(1).get(0);
            // get column separator
            String columnSeparator = ColumnSeparatorFactory.getColumnSeparator(datasetURL);
            Pattern pattern = Pattern.compile(columnSeparator);
            // get selected feature indices
            List<Integer> featureIndices = new ArrayList<Integer>();
            for (String feature : features) {
                featureIndices.add(MLUtils.getFeatureIndex(feature, headerRow, columnSeparator));
            }
            JavaRDD<org.apache.spark.mllib.linalg.Vector> featureVectors = null;
            double sampleFraction = 10000.0 / (lines.count() - 1);
            // Use entire dataset if number of records is less than or equal to 10000
            if (sampleFraction >= 1.0) {
                featureVectors = lines.filter(new HeaderFilter(headerRow)).map(new LineToTokens(pattern))
                        .filter(new MissingValuesFilter())
                        .map(new TokensToVectors(featureIndices));
            }
            // Use ramdomly selected 10000 rows if number of records is > 10000
            else {
                featureVectors = lines.filter(new HeaderFilter(headerRow))
                        .sample(false, sampleFraction).map(new LineToTokens(pattern))
                        .filter(new MissingValuesFilter())
                        .map(new TokensToVectors(featureIndices));
            }
            KMeans kMeans = new KMeans();
            KMeansModel kMeansModel = kMeans.train(featureVectors, noOfClusters, 100);
            // Populate cluster points list with predicted clusters and features
            List<Tuple2<Integer, org.apache.spark.mllib.linalg.Vector>> kMeansPredictions = kMeansModel.predict(featureVectors).zip(featureVectors)
                    .collect();
            for (Tuple2<Integer, org.apache.spark.mllib.linalg.Vector> kMeansPrediction : kMeansPredictions) {
                ClusterPoint clusterPoint = new ClusterPoint();
                clusterPoint.setCluster(kMeansPrediction._1());
                clusterPoint.setFeatures(kMeansPrediction._2().toArray());
                clusterPoints.add(clusterPoint);
            }
            sparkContext.stop();
            return clusterPoints;
        } catch (DatabaseHandlerException e) {
            throw new DatabaseHandlerException("An error occurred while generating cluster points: " + e.getMessage(), e);
        } finally {
            // switch class loader back to thread context class loader
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    class ModelBuilder implements Runnable {

        private long id;
        private MLModelConfigurationContext ctxt;

        public ModelBuilder(long modelId, MLModelConfigurationContext context) {
            id = modelId;
            ctxt = context;
        }

        @Override
        public void run() {
            try {
                // class loader is switched to JavaSparkContext.class's class loader
                Thread.currentThread().setContextClassLoader(JavaSparkContext.class.getClassLoader());
                String algorithmType = ctxt.getFacts().getAlgorithmClass();
                MLModel model;
                if (MLConstants.CLASSIFICATION.equals(algorithmType)
                        || MLConstants.NUMERICAL_PREDICTION.equals(algorithmType)) {
                    SupervisedModel supervisedModel = new SupervisedModel();
                    model = supervisedModel.buildModel(ctxt);
                } else if (MLConstants.CLUSTERING.equals((algorithmType))) {
                    UnsupervisedModel unsupervisedModel = new UnsupervisedModel();
                    model = unsupervisedModel.buildModel(ctxt);
                } else {
                    throw new MLModelBuilderException(String.format(
                            "Failed to build the model [id] %s . Invalid algorithm type: %s", id, algorithmType));
                }

                persistModel(id, ctxt.getModel().getName(), model);
            } catch (Exception e) {
                log.error(String.format("Failed to build the model [id] %s ", id), e);
            }
        }

    }

}
