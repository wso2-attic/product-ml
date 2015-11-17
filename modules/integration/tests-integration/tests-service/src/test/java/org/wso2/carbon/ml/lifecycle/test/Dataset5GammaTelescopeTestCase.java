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
 * This class contains the entire ML life-cycle for Gamma Telescope dataset
 */
@Test(groups = "gammaTelescopeDataset")
public class Dataset5GammaTelescopeTestCase extends MLBaseTest {

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
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_GAMMA_TELESCOPE, version,
                MLIntegrationTestConstants.GAMMA_TELESCOPE_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_GAMMA_TELESCOPE,
                MLIntegrationTestConstants.DATASET_NAME_GAMMA_TELESCOPE);
    }

    /**
     * A test case for predicting for a given set of data points
     * 
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testPredictGammaTelescope() throws MLHttpClientException, JSONException {
        String payload = "[[18.8562,16.46,2.4385,0.5282,0.2933,25.1269,-6.5401,-16.9327,11.461,162.848],"
                + "[191.8036,49.7183,3.0006,0.2093,0.1225,146.2148,143.6098,31.6216,44.3492,245.4199]]";
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
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_GAMMA_TELESCOPE,
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

    // Test disabled because Naive bayes does not support negative feature values
    // /**
    // * Creates a test case for creating an analysis, building a Naive Bayes model and predicting using the built model
    // * @throws MLHttpClientException
    // * @throws IOException
    // * @throws JSONException
    // * @throws InterruptedException
    // */
    // @Test(description = "Build a Naive Bayes model and predict for gamma telescope dataset",
    // groups="createNaiveBayesModelGammaTelescope", dependsOnGroups="createProjectGammaTelescope")
    // public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException, JSONException,
    // InterruptedException {
    // // Check whether the project is created otherwise skip
    // response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
    // .PROJECT_NAME_GAMMA_TELESCOPE);
    // if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
    // throw new SkipException("Skipping tests because a project is not available");
    // }
    // buildModelWithLearningAlgorithm("NAIVE_BAYES", MLIntegrationTestConstants.CLASSIFICATION);
    // // Predict using built Linear Regression model
    // testPredictGammaTelescope();
    // }

    /**
     * Creates a test case for creating an analysis, building a SVM model and predicting using the built model,
     * exporting and publishing the model in PMML format
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a SVM model and predict for gamma telescope dataset", groups = "createSVMModelGammaTelescope")
    public void testBuildSVMModel() throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("SVM", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictGammaTelescope();
        testExportAsPMML();
        testPublishAsPMML();
    }

    /**
     * Creates a test case for creating an analysis, building a Decision tree model and predicting using the built model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Decision Tree model and predict for gamma telescope dataset", groups = "createDecisionTreeModelGammaTelescope", dependsOnGroups = "createSVMModelGammaTelescope")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictGammaTelescope();
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
    @Test(description = "Build a Logistic Regression model and predict for gamma telescope dataset", groups = "createLogisticRegressionGammaTelescope", dependsOnGroups = "createDecisionTreeModelGammaTelescope")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("LOGISTIC_REGRESSION", MLIntegrationTestConstants.CLASSIFICATION);
        // Predict using built Linear Regression model
        testPredictGammaTelescope();
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
    @Test(description = "Build a K-means model", groups = "createKMeansGammaTelescope", dependsOnGroups = "createLogisticRegressionGammaTelescope")
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
