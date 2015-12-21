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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.lifecycle.test;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.MLTestUtils;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * This class contains the entire ML life-cycle for Diabetes dataset
 */
@Test(groups = "diabetesDataset")
public class Dataset1DiabetesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private static String modelName;
    private static int modelId;
    private CloseableHttpResponse response;
    private int versionSetId;
    private int projectId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException, IOException, JSONException {
        super.init();
        mlHttpclient = getMLHttpClient();
        String version = "1.0";
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_DIABETES, version,
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_DIABETES,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
    }

    /**
     * A test case for predicting for a given set of data points
     * 
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictDiabetes() throws MLHttpClientException, JSONException {
        testPredictDiabetes(false);
    }
    
    private void testPredictDiabetes(boolean skipDecoding) throws MLHttpClientException, JSONException {
        String payload = "[[1,89,66,23,94,28.1,0.167,21],[2,197,70,45,543,30.5,0.158,53]]";
        String url = skipDecoding ? "/api/models/" + modelId + "/predict?skipDecoding=true" : "/api/models/" + modelId + "/predict";
        response = mlHttpclient.doHttpPost(url, payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray predictions = new JSONArray(reply);
        assertEquals("Expected 2 predictions but received only " + predictions.length(), 2, predictions.length());
        if (skipDecoding) {
            assertEquals("Expected a double value but found " + predictions.get(0), true,
                    predictions.get(0) instanceof Double);
            assertEquals("Expected a double value but found " + predictions.get(1), true,
                    predictions.get(1) instanceof Double);
        }
    }

    /**
     * A test case for predicting with a dataset incompatible with the trained dataset in terms of number of features
     *
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictDiabetesInvalidNumberOfFeatures() throws MLHttpClientException, JSONException {
        String payload = "[[1,89,66,23,94,28.1,0.167],[2,197,70,45,543,30.5,0.158]]";
        response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/predict", payload);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatusLine().getStatusCode());
    }

    /**
     * A test case for predicting with a dataset incompatible with the trained dataset in terms of numerical feature
     * type
     *
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictDiabetesInvalidNumericalFeatures() throws MLHttpClientException, JSONException {
        // One of the values is non-numerical
        String payload = "[[1,89,66,23,94,28afdc.1,0.167,21],[2,197,70,45,543,30.5,0.158,53]]";
        response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/predict", payload);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatusLine().getStatusCode());
    }
    
    /**
     * A test case for predicting for a given set of data points from a file.
     * 
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictDiabetesFromFile() throws MLHttpClientException, JSONException {
        response = mlHttpclient.predictFromCSV(modelId, MLIntegrationTestConstants.DIABETES_DATASET_TEST);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray predictions = new JSONArray(reply);
        assertEquals(7, predictions.length());
    }

    /**
     * A test case for building a model with the given learning algorithm
     * 
     * @param algorithmName Name of the learning algorithm
     * @param algorithmType Type of the learning algorithm
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    private void buildModelWithLearningAlgorithm(String algorithmName, String algorithmType)
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        modelName = MLTestUtils.createModelWithConfigurations(algorithmName, algorithmType,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                projectId, versionSetId, mlHttpclient);
        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
        // Waiting for model building to end
        boolean status = MLTestUtils.checkModelStatusCompleted(modelName, mlHttpclient,
                MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        // Checks whether model building completed successfully
        assertEquals("Model building did not complete successfully", true, status);
    }

    /**
     * Creates a test case for creating an analysis, building a Naive Bayes model and predicting using the built model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Naive Bayes model and predict for Diabetes dataset", groups = "createNaiveBayesModelDiabetes")
    public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();

        // Predict for a incompatible dataset
        testPredictDiabetesInvalidNumberOfFeatures();
    }

    /**
     * Creates a test case for creating an analysis, building a SVM model, predicting using the built model,
     * exporting and publishing the model in PMML format
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a SVM model and predict for Diabetes dataset", groups = "createSVMModelDiabetes", dependsOnGroups = "createNaiveBayesModelDiabetes")
    public void testBuildSVMModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("SVM", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();

        // Predict for dataset with incompatible numerical feature
        testPredictDiabetesInvalidNumericalFeatures();
        testExportAsPMML(modelId);
        testPublishAsPMML(modelId);
    }

    /**
     * Creates a test case for creating an analysis, building a Decision tree model and predicting using the built model,
     * exporting and publishing the model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Decision Tree model and predict for Diabetes dataset", groups = "createDecisionTreeModelDiabetes", dependsOnGroups = "createSVMModelDiabetes")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();
    }
    
    /**
     * Creates a test case for creating an analysis, building a Random forest model and predicting using the built model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Random Forest model and predict for Diabetes dataset", groups = "createRandomForestModelDiabetes", dependsOnGroups = "createDecisionTreeModelDiabetes")
    public void testBuildRandomForestModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("RANDOM_FOREST_CLASSIFICATION", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built model
        testPredictDiabetes();
    }

    /**
     * Creates a test case for creating an analysis, building a Stacked Autoencoders model and predicting using the built
     * model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Stacked Autoencoders model and predict for Diabetes dataset", groups = "createStackedAutoencodersModelDiabetes", dependsOnGroups = "createRandomForestModelDiabetes")
    public void testBuildStackedAutoencodersModel()
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("STACKED_AUTOENCODERS", MLIntegrationTestConstants.DEEP_LEARNING);
        // Predict using built model
        testPredictDiabetes();
    }

    /**
     * Creates a test case for creating an analysis, building a Logistic Regression model and predicting using the built
     * model, exporting and publishing the model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Logistic Regression model and predict for Diabetes dataset", groups = "createLogisticRegressionDiabetes", dependsOnGroups = "createStackedAutoencodersModelDiabetes")
    public void testBuildLogisticRegressionModel()
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();
        testPredictDiabetes(true);
        testPredictDiabetesFromFile();
        testExportAsPMML(modelId);
        testPublishAsPMML(modelId);
    }

    /**
     * Creates a test case for creating an analysis, building a K-Means clustering model, exporting and publishing the
     * model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a K-means model", groups = "createKMeansDiabetes", dependsOnGroups = "createLogisticRegressionDiabetes")
    public void testBuildKMeansModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("K_MEANS", MLIntegrationTestConstants.CLUSTERING);
        testExportAsPMML(modelId);
        testPublishAsPMML(modelId);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
