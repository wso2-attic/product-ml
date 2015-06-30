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
 * This class contains the entire ML life-cycle for a dataset resides in DAS tables.
 */
@Test(groups = "dasDataset")
public class Dataset8DASDataTestCase extends MLBaseTest {

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
        int datasetId = createDatasetFromDASTable(MLIntegrationTestConstants.DATASET_NAME_DAS, version,
                MLIntegrationTestConstants.DAS_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_DAS,
                MLIntegrationTestConstants.DATASET_NAME_DAS);
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
        modelName = MLTestUtils.setConfiguration(algorithmName, algorithmType,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DAS, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                projectId, versionSetId, mlHttpclient);
        String payload = "[{\"name\" : \"id\",\"tenantId\" : -1234,\"type\" : \"NUMERICAL\",\"include\" : false,\"imputeOption\": \"DISCARD\"}, "
                + "{\"name\" : \"meta_type\",\"tenantId\" : -1234,\"type\" : \"NUMERICAL\",\"include\" : false,\"imputeOption\": \"DISCARD\"}, "
                + "{\"name\" : \"property\",\"tenantId\" : -1234,\"type\" : \"NUMERICAL\",\"include\" : false,\"imputeOption\": \"DISCARD\"}, "
                + "{\"name\" : \"timeStamp\",\"tenantId\" : -1234,\"type\" : \"NUMERICAL\",\"include\" : false,\"imputeOption\": \"DISCARD\"}]";
        int analysisId = mlHttpclient.getAnalysisId(projectId, algorithmName + versionSetId);
        mlHttpclient.setFeatureCustomized(analysisId, payload);
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
    @Test(description = "Build a linear regression model and predict for DAS dataset", groups = "createLinearRegressionModelDAS")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);
        // Predict using built Linear Regression model
        testPredictDAS();
    }

    /**
     * A test case for predicting for a given set of data points
     * 
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictDAS() throws MLHttpClientException, JSONException {
        String payload = "[[21,3,2],[211,1,7]]";
        response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/predict", payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray predictions = new JSONArray(reply);
        assertEquals(2, predictions.length());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
