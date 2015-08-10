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

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * This class contains test cases related to adding features to a ML analysis
 */
@Test(groups="addFeatures")
public class FeatureProcessingTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    private int projectId;
    private int analysisId;
    
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
    }

    /**
     * Test adding default values to customized features an analysis.
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(priority=1, description = "Add default values to customized features")
    public void testAddDefaultsToCustomizedFeatures() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.setFeatureDefaults(analysisId);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test adding customized features an analysis.
     * 
     * @throws IOException
     * @throws MLHttpClientException 
     */
    @Test(priority=2, description = "Add customized features")
    public void testAddCustomizedFeatures() throws  MLHttpClientException, IOException {
        String payload ="[{\"type\" :\"NUMERICAL\",\"include\" : false,\"imputeOption\":\"DISCARD\",\"name\":\"" +
                "Age\"}]";
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/analyses/" + analysisId + "/features", payload);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * 
     * @throws MLHttpClientException 
     */
    @Test(priority=3, description = "Get all features")
    public void testGetAllFeatures() throws MLHttpClientException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/"+analysisId+"/features");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
    }
    
    /**
     * 
     * @throws MLHttpClientException 
     */
    @Test(priority=3, description = "Get all summarized features")
    public void testGetSummarizedFeatures() throws MLHttpClientException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/"+analysisId+"/summarizedFeatures");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
    }
    
    /**
     * 
     * @throws MLHttpClientException 
     */
    @Test(priority=3, description = "Get all filtered features")
    public void testGetFilteredFeatures() throws MLHttpClientException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/"+analysisId+"/filteredFeatures?featureType=CATEGORICAL");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
    }
    
    /**
     * 
     * @throws MLHttpClientException 
     */
    @Test(priority=4, description = "Get summary stats - without providing a feature")
    public void testGetSummaryStatsWithoutFeature() throws MLHttpClientException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/"+analysisId+"/stats");
        assertEquals("Unexpected response received", Response.Status.NOT_FOUND.getStatusCode(), response.getStatusLine()
                .getStatusCode());
    }
    
    /**
     * 
     * @throws MLHttpClientException 
     */
    @Test(priority=4, description = "Get summary stats - with feature")
    public void testGetSummaryStatsWithFeature() throws MLHttpClientException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/analyses/"+analysisId+"/stats?feature=Class");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}