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
 * This class contains the entire ML life-cycle for Forest Fires dataset
 */
@Test(groups = "ForestFiresDataset")
public class Dataset4ForestFiresTestCase extends MLBaseTest {

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
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_FOREST_FIRES, version,
                MLIntegrationTestConstants.FOREST_FIRES_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES,
                MLIntegrationTestConstants.DATASET_NAME_FOREST_FIRES);
    }

    /**
     * A test case for predicting for a given set of data points
     * 
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
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_FOREST_FIRES,
                MLIntegrationTestConstants.TRAIN_DATA_FRACTION, projectId, versionSetId, mlHttpclient);
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
     * Creates a test case for creating an analysis, building a Linear Regression model and predicting using the built
     * model, exporting and publishing the model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a linear regression model and predict for forest fires dataset", groups = "createLinearRegressionModelForestFires")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Linear Regression model
        testPredictForestFires();
        testExportAsPMML();
        testPublishAsPMML();
    }

    /**
     * Creates a test case for creating an analysis, building a Ridge Regression model and predicting using the built
     * model, exporting and publishing the model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a ridge regression model and predict for forest fires dataset", groups = "createRidgeRegressionModelForestFires", dependsOnGroups = "createLinearRegressionModelForestFires")
    public void testBuildRidgeRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("RIDGE_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Ridge Regression model
        testPredictForestFires();
        testExportAsPMML();
        testPublishAsPMML();
    }

    /**
     * Creates a test case for creating an analysis, building a Lasso Regression model and predicting using the built
     * model, exporting and publishing the model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Lasso regression model and predict for forest fires dataset", groups = "createLassoRegressionModelForestFires", dependsOnGroups = "createRidgeRegressionModelForestFires")
    public void testBuildLassoRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("LASSO_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Lasso Regression model
        testPredictForestFires();
        testExportAsPMML();
        testPublishAsPMML();
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
    @Test(description = "Build a K-means model", groups = "createKMeansForestFires", dependsOnGroups = "createLassoRegressionModelForestFires")
    public void testBuildKMeansModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("K_MEANS", MLIntegrationTestConstants.CLUSTERING);
        testExportAsPMML();
        testPublishAsPMML();
    }
    /**
     * A test case for exporting a model in pmml format
     *
     * @throws MLHttpClientException
     */
    private void testExportAsPMML() throws MLHttpClientException {
        response = mlHttpclient.exportAsPMML(modelId);
        assertEquals("Pmml download has failed. Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
    }

    /**
     * A test case for publishing a model to registry in pmml format
     *
     * @throws MLHttpClientException
     */
    private void testPublishAsPMML() throws MLHttpClientException {
        response = mlHttpclient.doHttpPost("/api/models/"+modelId+"/publish?mode=pmml", null);
        assertEquals("Pmml publish has failed. Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
