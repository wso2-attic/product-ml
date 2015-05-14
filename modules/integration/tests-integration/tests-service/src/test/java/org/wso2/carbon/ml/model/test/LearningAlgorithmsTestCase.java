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

package org.wso2.carbon.ml.model.test;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to building models for learning algorithms
 */
@Test(groups="buildModels", dependsOnGroups="getModels")
public class LearningAlgorithmsTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    private static String analysisName;
    private static String modelName;
    private static int analysisId;
    private static int modelId;
    private CloseableHttpResponse response;

    /**
     * Sets the configuration of the model to be trained
     * @param algorithmName     Name of the learning algorithm
     * @param algorithmType     Type of the learning algorithm
     * @param response          Response attribute
     * @param trainDataFraction Fraction of data from the dataset to be trained with
     * @throws MLHttpClientException
     */
    private void setConfiguration(String algorithmName, String algorithmType, String response,
                                  String trainDataFraction, int projectID) throws MLHttpClientException {
        analysisName = "TestAnalysisFor"+algorithmName+"TestCase";

        //Create an analysis
        mlHttpclient.createAnalysis(analysisName, projectID);
        analysisId = mlHttpclient.getAnalysisId(analysisName);

        //Set Model Configurations
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put(MLConstants.ALGORITHM_NAME, algorithmName);
        configurations.put(MLConstants.ALGORITHM_TYPE, algorithmType);
        configurations.put(MLConstants.RESPONSE, response);
        configurations.put(MLConstants.TRAIN_DATA_FRACTION, trainDataFraction);
        mlHttpclient.setModelConfiguration(analysisId, configurations);

        //Set default Hyper-parameters
        mlHttpclient.doHttpPost("/api/analyses/" + analysisId + "/hyperParams/defaults", null);

        // Create a model
        CloseableHttpResponse httpResponse = mlHttpclient.createModel(analysisId, MLIntegrationTestConstants.VERSIONSET_ID);
        modelName = mlHttpclient.getModelName(httpResponse);
        modelId = mlHttpclient.getModelId(modelName);

        //Set storage location for model
        mlHttpclient.createFileModelStorage(modelId, getModelStorageDirectory());
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the project exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_DIABETES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_CONCRETE_SLUMP);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
    }

    // Tests for classification algorithms

//    @Test(description = "Build a SVM model")
//    public void testBuildSVMModel() throws MLHttpClientException, IOException{
//        setConfiguration("SVM", "Classification", "Class", "0.7",
//                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
//        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
//        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
//                .getStatusCode());
//        response.close();
//    }

    @Test(description = "Build a Decision Tree model")
    public void testBuildDecisionTreeModel() throws MLHttpClientException, IOException{
        setConfiguration("DECISION_TREE", "Classification", "Class", "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Naive Bayes model")
    public void testBuildNaiveBayesModel() throws MLHttpClientException, IOException{
        setConfiguration("NAIVE_BAYES", "Classification", "Class", "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Logistic Regression model")
    public void testBuildLogisticRegressionModel() throws MLHttpClientException, IOException{
        setConfiguration("LOGISTIC_REGRESSION", "Classification", "Class", "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    // Tests for Numerical Prediction algorithms

    @Test(description = "Build a Linear Regression model")
    public void testBuildLinearRegressionModel() throws MLHttpClientException, IOException{
        setConfiguration("LINEAR_REGRESSION", "NUMERICAL_PREDICTION", "CompressiveStrength(28-day)(Mpa)", "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Ridge Regression model")
    public void testBuildRidgeRegressionModel() throws MLHttpClientException, IOException{
        setConfiguration("RIDGE_REGRESSION", "NUMERICAL_PREDICTION", "CompressiveStrength(28-day)(Mpa)", "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @Test(description = "Build a Lasso Regression model")
    public void testBuildLassoRegressionModel() throws MLHttpClientException, IOException{
        setConfiguration("LASSO_REGRESSION", "NUMERICAL_PREDICTION", "CompressiveStrength(28-day)(Mpa)", "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    // Tests for clustering algorithms

    @Test(description = "Build a K-means clustering model")
    public void testBuildKMeansModel() throws MLHttpClientException, IOException{
        setConfiguration("K-MEANS", "CLUSTERING", null, "0.7",
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
        response = mlHttpclient.doHttpPost("/api/models/" + modelId, null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException, InterruptedException {
        // FIXME:
        Thread.sleep(50000);
    }
}