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
 * This class contains the entire ML life-cycle for Yacht hydrodynamics dataset
 */
@Test(groups = "yatchDataset")
public class Dataset2YachtHydrodynamicsTestCase extends MLBaseTest {

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
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_YACHT, version,
                MLIntegrationTestConstants.YACHT_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_YACHT,
                MLIntegrationTestConstants.DATASET_NAME_YACHT);
    }

    /**
     * A test case for predicting for a given set of data points
     * 
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictYacht() throws MLHttpClientException, JSONException {
        String payload = "[[-2.3,0.568,4.78,3.99,3.17,0.125],[-2.3,0.568,4.78,3.99,3.17,0.300]]";
        response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/predict", payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray predictions = new JSONArray(reply);
        assertEquals(2, predictions.length());
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
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_YACHT, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                projectId, versionSetId, mlHttpclient);
        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
        // Waiting for model building to end
        boolean status = MLTestUtils.checkModelStatus(modelName, mlHttpclient,
                MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        // Checks whether model building completed successfully
        assertEquals("Model building did not complete successfully", true, status);
    }

    /**
     * Creates a test case for creating an analysis, building a Linear Regression model and predicting using the built
     * model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a linear regression model and predict for yacht dataset", groups = "createLinearRegressionModelYacht")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Linear Regression model
        testPredictYacht();
    }

    /**
     * Creates a test case for creating an analysis, building a Ridge Regression model and predicting using the built
     * model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a ridge regression model and predict for yacht dataset", groups = "createRidgeRegressionModelYacht")
    public void testBuildRidgeRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("RIDGE_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Ridge Regression model
        testPredictYacht();
    }

    /**
     * Creates a test case for creating an analysis, building a Lasso Regression model and predicting using the built
     * model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Lasso regression model and predict for yacht dataset", groups = "createLassoRegressionModelYacht")
    public void testBuildLassoRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("LASSO_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Lasso Regression model
        testPredictYacht();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
