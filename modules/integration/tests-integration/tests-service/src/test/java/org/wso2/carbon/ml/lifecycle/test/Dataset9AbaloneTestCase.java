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
 * This class contains the entire ML life-cycle for Abalone dataset
 */
@Test(groups = "abaloneDataset")
public class Dataset9AbaloneTestCase extends MLBaseTest {

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
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_ABALONE, version,
                MLIntegrationTestConstants.ABALONE_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_ABALONE,
                MLIntegrationTestConstants.DATASET_NAME_ABALONE);
    }

    /**
     * A test case for predicting for a given set of data points
     *
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictAbalone() throws MLHttpClientException, JSONException {
        String payload = "[[0.455,0.365,0.095,0.514,0.2245,0.101,0.15,15],"
                + "[0.44,0.365,0.125,0.516,0.2155,0.114,0.155,10]]";
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
    private boolean buildModelWithLearningAlgorithm(String algorithmName, String algorithmType)
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        modelName = MLTestUtils.createModelWithConfigurations(algorithmName, algorithmType,
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_ABALONE,
                MLIntegrationTestConstants.TRAIN_DATA_FRACTION, projectId, versionSetId, mlHttpclient);
        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
        // Waiting for model building to end
        boolean status = MLTestUtils.checkModelStatusCompleted(modelName, mlHttpclient,
                MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        return status;
    }

    /**
     * Creates a test case for creating an analysis, building a Naive Bayes model and predicting using the built model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Naive Bayes model and predict for abalone dataset", groups="createNaiveBayesModelAbalone")
    public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictAbalone();
    }

    /**
     * Creates a test case for creating an analysis, building a Random Forest model and predicting using the built model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Random Forest model and predict for abalone dataset", groups="createRandomForestModelAbalone")
    public void testBuildRandomForest() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("RANDOM_FOREST", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictAbalone();
    }

    /**
     * Creates a test case for creating an analysis, building a Logistic Regression LBFGS model and predicting using the built model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Logistic Regression LBFGS model and predict for abalone dataset", groups="createLogisticRegressionModelLBFGSAbalone")
    public void testBuildLogisticRegressionLBFGSModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("LOGISTIC_REGRESSION_LBFGS", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictAbalone();
    }

    // Following 2 tests check whether model building fails with the algorithms that support only binary classification

    /**
     * Creates a test case for creating an analysis, building a SVM model and test for failure
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a SVM model for abalone dataset", groups = "createSVMModelAbalone")
    public void testBuildSVMModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        boolean status = buildModelWithLearningAlgorithm("SVM", MLIntegrationTestConstants.CLASSIFICATION);

        // Model building should fail since SVM cannot handle multi-class classification
        assertEquals("Model building did not complete successfully", false, status);
    }

    /**
     * Creates a test case for creating an analysis, building a Logistic Regression model and test for failure
     * model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Logistic Regression model for abalone dataset", groups = "createLogisticRegressionAbalone")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        boolean status = buildModelWithLearningAlgorithm("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION);

        // Model building should fail since Logistic Regression cannot handle multi-class classification
        assertEquals("Model building did not complete successfully", false, status);
    }

    // Following test checks whether model building fails when a categorical response variable is used with numerical
    // prediction.

    /**
     * Creates a test case for creating an analysis, building a Linear Regression model and test for failure
     * model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Linear Regression model for abalone dataset", groups = "createLinearRegressionAbalone")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        boolean status = buildModelWithLearningAlgorithm("LINEAR_REGRESSION", MLIntegrationTestConstants.NUMERICAL_PREDICTION);

        // Model building should fail since Linear Regression cannot handle categorical response variables
        assertEquals("Model building did not complete successfully", false, status);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
