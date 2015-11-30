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

package org.wso2.carbon.ml.analysis.test;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * This class contains test cases related to setting model configurations
 */
@Test(groups="addModelConfigs")
public class ModelConfigurationsTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    private int projectId;
    private int analysisId;
    private int analysisId2;
    private final String ALGORITHM_NAME = "LOGISTIC_REGRESSION";
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = getMLHttpClient();
        createDataset(MLIntegrationTestConstants.DATASET_NAME_DIABETES, "1.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        isDatasetProcessed(getVersionSetIds().get(0), MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_DIABETES,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
        analysisId = createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME, projectId);
        analysisId2 = createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME_2, projectId);
    }

    /**
     * Test adding default values to customized features an analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(priority=1, description = "Add model configurations to the analysis")
    public void testSetModelConfigurations() throws MLHttpClientException, IOException {
        Map <String,String> configurations = new HashMap<String,String>();
        configurations.put(MLConstants.ALGORITHM_NAME, ALGORITHM_NAME);
        configurations.put(MLConstants.ALGORITHM_TYPE, "Classification");
        configurations.put(MLConstants.RESPONSE_VARIABLE, "Class");
        configurations.put(MLConstants.TRAIN_DATA_FRACTION, "0.7");
        CloseableHttpResponse response = mlHttpclient.setModelConfiguration(analysisId,
                configurations);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Get response variable of the analyses")
    public void testGetResponseVariable() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + analysisId + "/responseVariables");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException
     * @throws NamingException
     * @throws IOException
     */
    @Test(priority = 2, description = "Get response variable for malformed analysis id")
    public void testGetResponseVariableForMalformedAnalysisId()
            throws MLHttpClientException, NamingException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/abc/responseVariables");
        assertEquals("Unexpected response received", Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Get algorithm of the analyses")
    public void testGetAlgorithm() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + analysisId + "/algorithmName");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Get algorithm type of the analyses")
    public void testGetAlgorithmType() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + analysisId + "/algorithmType");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Get train data fraction of the analyses")
    public void testGetTrainDataFractionType() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + analysisId + "/trainDataFraction");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test setting default values to hyper-parameters of an analysis.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 3, description = "Set default values to hyperparameters")
    public void testSetDefaultHyperparameters() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient
                .doHttpPost("/api/analyses/" + analysisId + "/hyperParams/defaults", null);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test setting default values to hyper-parameters of an analysis with an unknown algorithm
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 3, description = "Set default values to hyperparameters for an unknown algorithm")
    public void testSetDefaultHyperparametersForAnUnknownAlgo() throws MLHttpClientException, IOException {
        Map<String, String> configurations = new HashMap<String, String>();
        configurations.put(MLConstants.ALGORITHM_NAME, "ABC");
        configurations.put(MLConstants.ALGORITHM_TYPE, "Classification");
        configurations.put(MLConstants.RESPONSE_VARIABLE, "Class");
        configurations.put(MLConstants.TRAIN_DATA_FRACTION, "0.7");
        CloseableHttpResponse response = mlHttpclient.setModelConfiguration(analysisId2, configurations);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response = mlHttpclient.doHttpPost("/api/analyses/" + analysisId2 + "/hyperParams/defaults", null);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test setting default values to hyper-parameters of an analysis without setting model configs.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 3, description = "Set default values to hyperparameters without setting model configs.")
    public void testSetDefaultHyperparametersWithoutModelConfigs() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient
                .doHttpPost("/api/analyses/" + analysisId2 + "/hyperParams/defaults", null);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test setting customized hyper-parameters of an analysis.
     * 
     * @throws IOException
     * @throws MLHttpClientException
     */
    @Test(priority = 3, description = "Set customized hyperparameters", dependsOnMethods = "testSetDefaultHyperparameters")
    public void testSetCustomizedHyperParameters() throws IOException, MLHttpClientException {
        String payload = "[{\"key\" :\"Learning_Rate\",\"value\" : \"0.1\"},{\"key\":\"Iterations\",\"value\":\"100\"}]";
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/analyses/" + analysisId + "/hyperParams",
                payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 4, description = "Get hyper parameters of the analyses")
    public void testGetHyperParameters() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/" + analysisId + "/hyperParameters");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 4, description = "Get hyper parameters of the analyses and of a algorithm")
    public void testGetHyperParametersOfAlgorithm() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient
                .doHttpGet("/api/analyses/" + analysisId + "/hyperParameters?algorithmName=" + ALGORITHM_NAME);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}