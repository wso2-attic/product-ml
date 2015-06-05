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
@Test(groups="breastCancerDataset", dependsOnGroups="yatchDataset")
public class Dataset2BreastCancerTestCase extends MLBaseTest {

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
            MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER = 1;
    }

    /**
     * Creates dataset for classification and clustering - Breast Cancer
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of breast cancer data from a CSV file", groups="createDatasetBreastCancer")
    public void testCreateDatasetBreastCancer() throws MLHttpClientException, IOException {
        response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_BREAST_CANCER,
                "1.0", MLIntegrationTestConstants.BREAST_CANCER_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for Breast Cancer data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for breast cancer dataset", groups="createProjectBreastCancer", dependsOnGroups="createDatasetBreastCancer")
    public void testCreateProjectBreastCancer() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER,
                MLIntegrationTestConstants.DATASET_NAME_BREAST_CANCER);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * A test case for predicting for a given set of data points
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictBreastCancer() throws MLHttpClientException, JSONException {
        String payload = "[[1015425,3,1,1,1,2,2,3,1,1],[1033078,2,1,1,1,2,1,1,1,5]]";
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
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_BREAST_CANCER, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER),
                MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER, mlHttpclient);
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
     * Creates a test case for creating an analysis, building a Naive Bayes model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Naive Bayes model and predict for breast cancer dataset", groups="createNaiveBayesModelBreastCancer", dependsOnGroups="createProjectBreastCancer")
    public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        // Check whether the project is created otherwise skip
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_BREAST_CANCER);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        buildModelWithLearningAlgorithm("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictBreastCancer();
    }

    /**
     * Creates a test case for creating an analysis, building a SVM model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a SVM model and predict for breast cancer dataset", groups="createSVMModelBreastCancer", dependsOnGroups="createNaiveBayesModelBreastCancer")
    public void testBuildSVMModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("SVM", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictBreastCancer();
    }

    /**
     * Creates a test case for creating an analysis, building a Decision tree model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Decision Tree model and predict for breast cancer dataset", groups="createDecisionTreeModelBreastCancer", dependsOnGroups="createSVMModelBreastCancer")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictBreastCancer();
    }

    /**
     * Creates a test case for creating an analysis, building a Logistic Regression model and predicting using the built model
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Logistic Regression model and predict for breast cancer dataset", groups="createLogisticRegressionBreastCancer", dependsOnGroups="createDecisionTreeModelBreastCancer")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictBreastCancer();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        mlHttpclient.doHttpDelete("/api/projects/" + MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER);
        mlHttpclient.doHttpDelete("/api/datasets/" + MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER);
    }
}
