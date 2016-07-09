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

import java.io.File;
import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;

/**
 * This class contains the entire ML life-cycle for Digit Recognition dataset
 */
@Test(groups = "DigitRecognitionDataset")
public class ExternalDataset1DigitRecognitionTestCase extends MLBaseTest {

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
        File file = new File(mlHttpclient.getResourceAbsolutePath(MLIntegrationTestConstants.DIGIT_RECOGNITION_DATASET_SAMPLE));
        if (!file.exists()) {
            throw new SkipException("Skipping the tests because the dataset file is not available at: "+ file.getAbsolutePath());
        }
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_DIGITS, version,
                MLIntegrationTestConstants.DIGIT_RECOGNITION_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_DIGITS,
                MLIntegrationTestConstants.DATASET_NAME_DIGITS);
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
                MLIntegrationTestConstants.RESPONSE_ATTRIBUTE_DIGITS, MLIntegrationTestConstants.TRAIN_DATA_FRACTION,
                projectId, versionSetId, mlHttpclient);
        modelId = mlHttpclient.getModelId(modelName);
        response = mlHttpclient.doHttpPost("/api/models/" + modelId);
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
     * Creates a test case for creating an analysis, building a Decision Tree model
     * 
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */
    @Test(description = "Build a Decision tree model", groups = "createDecisionTreeDigitRecognition")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException, JSONException,
            InterruptedException {
        buildModelWithLearningAlgorithm("DECISION_TREE", MLIntegrationTestConstants.CLASSIFICATION);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
