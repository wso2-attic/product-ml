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
 * This class contains the Anomaly detection ML life-cycle for Diabetes dataset
 */

@Test(groups = "diabetesDatasetAnomalyDetection")
public class Dataset10AnomalyDetectionTestCase extends MLBaseTest {

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

    private void testPredictDiabetes(String algorithmName) throws MLHttpClientException, JSONException {

        String payload = "";
        if (algorithmName.equals("K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA")) {
            payload = "[[1,89,66,23,94,28.1,0.167,21,0],[2,197,70,45,543,30.5,0.158,53,1]]";
        } else if (algorithmName.equals("K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA")) {
            payload = "[[1,89,66,23,94,28.1,0.167,21],[2,197,70,45,543,30.5,0.158,53]]";
        }

        response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/predict?percentile=98", payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
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

        if (algorithmName.equals("K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA")) {
            modelName = MLTestUtils.createModelWithConfigurations(algorithmName, algorithmType,
                    MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DEFAULT,
                    MLIntegrationTestConstants.TRAIN_DATA_FRACTION_DEFAULT,
                    MLIntegrationTestConstants.NORMAL_LABELS_DEFAULT, MLIntegrationTestConstants.NEW_NORMAL_LABEL,
                    MLIntegrationTestConstants.NEW_ANOMALY_LABEL, MLIntegrationTestConstants.NORMALIZATION, projectId,
                    versionSetId, mlHttpclient);
        } else if (algorithmName.equals("K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA")) {
            modelName = MLTestUtils.createModelWithConfigurations(algorithmName, algorithmType,
                    MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIABETES,
                    MLIntegrationTestConstants.TRAIN_DATA_FRACTION, MLIntegrationTestConstants.NORMAL_LABELS,
                    MLIntegrationTestConstants.NEW_NORMAL_LABEL, MLIntegrationTestConstants.NEW_ANOMALY_LABEL,
                    MLIntegrationTestConstants.NORMALIZATION, projectId, versionSetId, mlHttpclient);
        }

        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
        // Waiting for model building to end
        boolean status = MLTestUtils.checkModelStatusCompleted(modelName, mlHttpclient,
                MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        // Checks whether model building completed successfully
        assertEquals("Model building did not complete successfully", true, status);
    }


/**
     * Creates a test case for creating an analysis, building a Anomaly detection model and predicting using the built
     * model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */

    @Test(description = "Build a Unlabeled anomaly detection model and predict for Diabetes dataset", groups = "createUnlabeledAnomalyDetectionDiabetes")
    public void testBuildUnlabeledAnomalyDetectionModel()
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA",
                MLIntegrationTestConstants.ANOMALY_DETECTION);
        // Predict using built Anomaly detection model model
//        testPredictDiabetes("K_MEANS_ANOMALY_DETECTION_WITH_UNLABELED_DATA");
    }


///**
//     * Creates a test case for creating an analysis, building a Anomaly detection model and predicting using the built
//     * model
//     *
//     * @throws MLHttpClientException
//     * @throws IOException
//     * @throws JSONException
//     * @throws InterruptedException
//
//    @Test(description = "Build a Labeled anomaly detection model and predict for Diabetes dataset", groups = "createLabeledAnomalyDetectionDiabetes", dependsOnGroups = "createUnlabeledAnomalyDetectionDiabetes")
//    public void testBuildLabeledAnomalyDetectionModel()
//            throws MLHttpClientException, IOException, JSONException, InterruptedException {
//        buildModelWithLearningAlgorithm("K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA",
//                MLIntegrationTestConstants.ANOMALY_DETECTION);
//        // Predict using built Anomaly detection model model
//        testPredictDiabetes("K_MEANS_ANOMALY_DETECTION_WITH_LABELED_DATA");
//    }*/

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}

