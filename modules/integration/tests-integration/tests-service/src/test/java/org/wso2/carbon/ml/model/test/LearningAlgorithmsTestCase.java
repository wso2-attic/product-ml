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

package org.wso2.carbon.ml.model.test;

import static org.testng.AssertJUnit.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to building models for learning algorithms
 */
@Test(groups = "buildModels", dependsOnGroups = "getModels")
public class LearningAlgorithmsTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private static String analysisName;
    private static String modelName;
    private static int analysisId;
    private static int modelId;
    private CloseableHttpResponse response;

    /**
     * Sets the configuration of the model to be trained
     *
     * @param algorithmName     Name of the learning algorithm
     * @param algorithmType     Type of the learning algorithm
     * @param response          Response attribute
     * @param trainDataFraction Fraction of data from the dataset to be trained with
     * @param projectID         ID of the project
     * @param datasetID     Additional information about the name
     * @throws MLHttpClientException
     */
    private void setConfiguration(String algorithmName, String algorithmType, String response,
                                  String trainDataFraction, int projectID, int datasetID) throws MLHttpClientException, IOException, JSONException {
        analysisName = algorithmName + datasetID;

        //Create an analysis
        mlHttpclient.createAnalysis(analysisName, projectID);
        analysisId = mlHttpclient.getAnalysisId(analysisName);

        //Set Model Configurations
        Map<String, String> configurations = new HashMap<String, String>();
        configurations.put(MLIntegrationTestConstants.ALGORITHM_NAME, algorithmName);
        configurations.put(MLIntegrationTestConstants.ALGORITHM_TYPE, algorithmType);
        configurations.put(MLIntegrationTestConstants.RESPONSE, response);
        configurations.put(MLIntegrationTestConstants.TRAIN_DATA_FRACTION_CONFIG, trainDataFraction);
        mlHttpclient.setModelConfiguration(analysisId, configurations);

        //Set default Hyper-parameters
        mlHttpclient.doHttpPost("/api/analyses/" + analysisId + "/hyperParams/defaults", null);

        // Create a model
        CloseableHttpResponse httpResponse = mlHttpclient.createModel(analysisId, mlHttpclient.getAVersionSetIdOfDataset(datasetID));
        modelName = mlHttpclient.getModelName(httpResponse);
        modelId = mlHttpclient.getModelId(modelName);

        //Set storage location for model
        mlHttpclient.createFileModelStorage(modelId, getModelStorageDirectory());
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the project exists.
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_DIABETES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_CONCRETE_SLUMP);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_BREAST_CANCER);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_FOREST_FIRES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_GAMMA_TELESCOPE);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
    }

    // Tests for classification algorithms

    @Test(description = "Build a SVM model")
    public void testBuildSVMModel() throws MLHttpClientException, IOException, JSONException {
        setConfiguration("SVM", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES),
                MLIntegrationTestConstants.DATASET_ID_DIABETES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Decision Tree model")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES),
                MLIntegrationTestConstants.DATASET_ID_DIABETES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Naive Bayes model")
    public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES),
                MLIntegrationTestConstants.DATASET_ID_DIABETES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Logistic Regression model")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES),
                MLIntegrationTestConstants.DATASET_ID_DIABETES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    // Tests for Numerical Prediction algorithms

    @Test(description = "Build a Linear Regression model")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_CONCRETE_SLUMP, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP),
                MLIntegrationTestConstants.DATASET_ID_CONCRETE_SLUMP);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Ridge Regression model")
    public void testBuildRidgeRegressionModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("RIDGE_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_CONCRETE_SLUMP, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP),
                MLIntegrationTestConstants.DATASET_ID_CONCRETE_SLUMP);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Lasso Regression model")
    public void testBuildLassoRegressionModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("LASSO_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_CONCRETE_SLUMP, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP),
                MLIntegrationTestConstants.DATASET_ID_CONCRETE_SLUMP);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    // Tests for clustering algorithms

    @Test(description = "Build a K-means clustering model")
    public void testBuildKMeansModel() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("K-MEANS", MLIntegrationTestConstants.CLUSTERING, null,
                MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES),
                MLIntegrationTestConstants.DATASET_ID_DIABETES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Additional tests for learning algorithms with different datasets.
     * Following datasets are used:
     * Wisconsin breast cancer dataset - Classification
     * Forest fires dataset - Numerical prediction
     */

    // Classification

    @Test(description = "Build a SVM model for additional dataset-1")
    public void testBuildSVMModelAdditional01() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("SVM", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_BREAST_CANCER, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER),
                MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Decision Tree model for additional dataset-1")
    public void testBuildDecisionTreeModelAdditional01() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_BREAST_CANCER, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER),
                MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Naive Bayes model for additional dataset-1")
    public void testBuildNaiveBayesModelAdditional01() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_BREAST_CANCER, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER),
                MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Logistic Regression model for additional dataset-1")
    public void testBuildLogisticRegressionModelAdditional01() throws MLHttpClientException, IOException, JSONException {
        setConfiguration("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_BREAST_CANCER, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER),
                MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    // Numerical Prediction

    @Test(description = "Build a Linear Regression model for additional dataset-2")
    public void testBuildLinearRegressionModelAdditional02() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_FOREST_FIRES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES),
                MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Ridge Regression model for additional dataset-2")
    public void testBuildRidgeRegressionModelAdditional02() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("RIDGE_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_FOREST_FIRES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES),
                MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Lasso Regression model for additional dataset-2")
    public void testBuildLassoRegressionModelAdditional02() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("LASSO_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_FOREST_FIRES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES),
                MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Tests for datasets of different sizes.
     * Following datasets are used:
     *      Protein tertiary structre dataset (1.5MB) - for numerical prediction
     */

    @Test(description = "Build a Linear Regression model for larger dataset")
    public void testBuildLinearRegressionModelLargeDataset() throws MLHttpClientException, IOException, JSONException  {
        setConfiguration("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_GAMMA_TELESCOPE, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_GAMMA_TELESCOPE),
                MLIntegrationTestConstants.DATASET_ID_GAMMA_TELESCOPE);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException, IOException, InterruptedException, JSONException   {
        // Waiting for building models to end
        Thread.sleep(50000);
    }
}