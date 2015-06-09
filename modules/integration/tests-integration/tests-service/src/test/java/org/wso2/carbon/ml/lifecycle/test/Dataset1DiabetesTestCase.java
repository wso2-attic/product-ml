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
 * This class contains the entire ML life-cycle for Diabetes dataset
 */
@Test(groups="diabetesDataset")
public class Dataset1DiabetesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private static String modelName;
    private static int modelId;
    private CloseableHttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);

        // Check whether the project is created otherwise skip
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_DIABETES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
    }

    // Dataset and project have been already created, therefore no need to create them here.

    /**
     * A test case for predicting for a given set of data points
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictDiabetes() throws MLHttpClientException, JSONException {
        String payload = "[[1,89,66,23,94,28.1,0.167,21],[2,197,70,45,543,30.5,0.158,53]]";
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
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES),
                MLIntegrationTestConstants.DATASET_ID_DIABETES, mlHttpclient);
        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
        // Waiting for model building to end
        Thread.sleep(MLIntegrationTestConstants.THREAD_SLEEP_TIME_MEDIUM);
        // Checks whether model building completed successfully is true
        assertEquals("Model building did not complete successfully", true, MLTestUtils.checkModelStatus(modelName, mlHttpclient));
    }

    /**
     * Creates a test case for creating an analysis, building a Naive Bayes model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Naive Bayes model and predict for Diabetes dataset",
            groups="createNaiveBayesModelDiabetes")
    public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();
    }

    /**
     * Creates a test case for creating an analysis, building a SVM model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a SVM model and predict for Diabetes dataset",
            groups="createSVMModelDiabetes", dependsOnGroups="createNaiveBayesModelDiabetes")
    public void testBuildSVMModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("SVM", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();
    }

    /**
     * Creates a test case for creating an analysis, building a Decision tree model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Decision Tree model and predict for Diabetes dataset",
            groups="createDecisionTreeModelDiabetes", dependsOnGroups="createSVMModelDiabetes")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();
    }

    /**
     * Creates a test case for creating an analysis, building a Logistic Regression model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Logistic Regression model and predict for Diabetes dataset",
            groups="createLogisticRegressionDiabetes", dependsOnGroups="createDecisionTreeModelDiabetes")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictDiabetes();
    }

    /**
     * Creates a test case for creating an analysis, building a K-Means clustering model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a K-means model",
            groups="createKMeansDiabetes", dependsOnGroups="createLogisticRegressionDiabetes")
    public void testBuildKMeansModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("K_MEANS", MLIntegrationTestConstants.CLUSTERING);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        mlHttpclient.doHttpDelete("/api/projects/" + MLIntegrationTestConstants.PROJECT_NAME_DIABETES);
        mlHttpclient.doHttpDelete("/api/datasets/" + MLIntegrationTestConstants.DATASET_ID_DIABETES);
    }
}