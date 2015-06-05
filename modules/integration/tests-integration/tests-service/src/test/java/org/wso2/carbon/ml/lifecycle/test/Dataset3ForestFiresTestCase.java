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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.MLTestUtils;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;

/**
 * This class contains the entire ML life-cycle for Breast Cancer dataset
 */
@Test(groups="ForestFiresDataset", dependsOnGroups="breastCancerDataset")
public class Dataset3ForestFiresTestCase extends MLBaseTest {

    // When running only this test case, the dataset ID has to be 1
    // Set to true if you are running only this test class
    private boolean datasetIdOverride = false;

    private MLHttpClient mlHttpclient;
    private static String modelName;
    private static int modelId;
    private CloseableHttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);

        if (datasetIdOverride)
            MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES = 1;
    }

    /**
     * Creates dataset for Numerical Prediction - Forest fires
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of forest fires data from a CSV file", groups="createDatasetForestFires")
    public void testCreateDatasetForestFires() throws MLHttpClientException, IOException {
        response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_FOREST_FIRES,
                "1.0", MLIntegrationTestConstants.FOREST_FIRES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for Forest fires data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for forest fires dataset", groups="createProjectForestFires", dependsOnGroups="createDatasetForestFires")
    public void testCreateProjectForestFires() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES,
                MLIntegrationTestConstants.DATASET_NAME_FOREST_FIRES);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * A test case for predicting for a given set of data points
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictForestFires() throws MLHttpClientException, JSONException {
        String payload = "[[8,6,2,5,91.7,33.3,77.5,9,8.3,97,4,0.2],[7,5,8,6,92.5,88,698.6,7.1,22.8,40,4,0]]";
        response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/predict", payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray predictions = new JSONArray(reply);
        assertEquals(2, predictions.length());
    }

    /**
     * A test case for building a model with the given learning algorithm
     * @param algorithmName             Name of the learning algorithm
     * @param algorithmType             Type of the learning algorithm
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    private void buildModelWithLearningAlgorithm(String algorithmName, String algorithmType) throws MLHttpClientException,
            IOException, JSONException, InterruptedException {
        modelName= MLTestUtils.setConfiguration(algorithmName, algorithmType,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_FOREST_FIRES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES),
                MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES, mlHttpclient);
        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
        // Waiting for model building to end
        Thread.sleep(MLIntegrationTestConstants.THREAD_SLEEP_TIME);
        // Checks whether model building completed successfully is true
        assertEquals("Model building did not complete successfully", true, MLTestUtils.checkModelStatus(modelName, mlHttpclient));
    }

    /**
     * Creates a test case for creating an analysis, building a Linear Regression model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a linear regression model and predict for forest fires dataset", groups="createLinearRegressionModelForestFires", dependsOnGroups="createProjectForestFires")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        // Check whether the project is created otherwise skipped
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_FOREST_FIRES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        buildModelWithLearningAlgorithm("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Linear Regression model
        testPredictForestFires();
    }

    /**
     * Creates a test case for creating an analysis, building a Ridge Regression model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a ridge regression model and predict for forest fires dataset", groups="createRidgeRegressionModelForestFires", dependsOnGroups="createLinearRegressionModelForestFires")
    public void testBuildRidgeRegressionModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("RIDGE_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Ridge Regression model
        testPredictForestFires();
    }

    /**
     * Creates a test case for creating an analysis, building a Lasso Regression model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Lasso regression model and predict for forest fires dataset", groups="createLassoRegressionModelForestFires", dependsOnGroups="createRidgeRegressionModelForestFires")
    public void testBuildLassoRegressionModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("LASSO_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Lasso Regression model
        testPredictForestFires();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        mlHttpclient.doHttpDelete("/api/projects/" + MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES);
        mlHttpclient.doHttpDelete("/api/datasets/" + MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES);
    }
}
