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
 * This class contains the Recommendation system with Collaborative filtering ML life-cycle test for dummy
 * recommendation dataset
 */
@Test(groups = "dummyRecommendationDatasetCollaborativeFiltering")
public class Dataset11RecommendationTestCase extends MLBaseTest {

    private static String modelName;
    private static int modelId;
    private MLHttpClient mlHttpclient;
    private CloseableHttpResponse response;
    private int versionSetId;
    private int projectId;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException, IOException, JSONException {
        super.init();
        mlHttpclient = getMLHttpClient();
        String version = "1.0";
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_RECOMMENDATION, version,
                MLIntegrationTestConstants.RECOMMENDATION_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        isDatasetProcessed(versionSetId, MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_RECOMMENDATION,
                MLIntegrationTestConstants.DATASET_NAME_RECOMMENDATION);
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

        if (algorithmName.equals("COLLABORATIVE_FILTERING")) {
            modelName = MLTestUtils.createModelWithConfigurations(algorithmName, algorithmType,
                    MLIntegrationTestConstants.USER_ID_RECOMMENDATION,
                    MLIntegrationTestConstants.PRODUCT_ID_RECOMMENDATION,
                    MLIntegrationTestConstants.RATING_RECOMMENDATION, "explicit",
                    MLIntegrationTestConstants.TRAIN_DATA_FRACTION_DEFAULT, projectId, versionSetId, mlHttpclient);
        } else if (algorithmName.equals("COLLABORATIVE_FILTERING_IMPLICIT")) {
            modelName = MLTestUtils.createModelWithConfigurations(algorithmName, algorithmType,
                    MLIntegrationTestConstants.USER_ID_RECOMMENDATION,
                    MLIntegrationTestConstants.PRODUCT_ID_RECOMMENDATION,
                    MLIntegrationTestConstants.OBSERVATION_LIST_RECOMMENDATION, "implicit",
                    MLIntegrationTestConstants.TRAIN_DATA_FRACTION_DEFAULT, projectId, versionSetId, mlHttpclient);
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
     * A test case for getting user recommendations for given product.
     *
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testGetUserRecommendation() throws MLHttpClientException, JSONException {
        response = mlHttpclient
                .doHttpGet("/api/models/" + modelId + "/user-recommendations?product-id=123&no-of-users=2");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray recommendations = new JSONArray(reply);
        assertEquals(2, recommendations.length());
    }

    /**
     * A test case for getting product recommendations for given user.
     *
     * @throws MLHttpClientException
     * @throws JSONException
     */
    private void testGetProductRecommendation() throws MLHttpClientException, JSONException {
        response = mlHttpclient
                .doHttpGet("/api/models/" + modelId + "/product-recommendations?user-id=1&no-of-products=3");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        String reply = mlHttpclient.getResponseAsString(response);
        JSONArray recommendations = new JSONArray(reply);
        assertEquals(3, recommendations.length());
    }

    /**
     * Creates a test case for creating an analysis, building a Collaborative Filtering explicit model
     * model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */

    @Test(description = "Build a Unlabeled collaborative filtering explicit model for recommendation dataset", groups = "createCollaborativeFilteringExplicitRecommendation")
    public void testBuildCollaborativeFilteringExplicit()
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("COLLABORATIVE_FILTERING", MLIntegrationTestConstants.RECOMMENDATION);
        // Get recommendations for products and users
        testGetProductRecommendation();
        testGetUserRecommendation();
    }

    /**
     * Creates a test case for creating an analysis, building a Collaborative Filtering implicit model
     * model
     *
     * @throws MLHttpClientException
     * @throws IOException
     * @throws JSONException
     * @throws InterruptedException
     */

    @Test(description = "Build a Unlabeled collaborative filtering implicit model for recommendation dataset", groups = "createCollaborativeFilteringImplicitRecommendation", dependsOnGroups = "createCollaborativeFilteringExplicitRecommendation")
    public void testBuildCollaborativeFilteringImplicit()
            throws MLHttpClientException, IOException, JSONException, InterruptedException {
        buildModelWithLearningAlgorithm("COLLABORATIVE_FILTERING_IMPLICIT", MLIntegrationTestConstants.RECOMMENDATION);
        // Get recommendations for products and users
        testGetProductRecommendation();
        testGetUserRecommendation();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws InterruptedException, MLHttpClientException {
        super.destroy();
    }
}
