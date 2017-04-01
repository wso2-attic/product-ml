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
 * Class contains test cases related to creating analyses
 */
@Test(groups="ml-analyses")
public class CreateAnalysesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = getMLHttpClient();
        createDataset(MLIntegrationTestConstants.DATASET_NAME_DIABETES, "1.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        isDatasetProcessed(getVersionSetIds().get(0), MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
        createProject(MLIntegrationTestConstants.PROJECT_NAME_DIABETES,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
    }

    /**
     * Test creating an analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(priority=1, description = "Create an analysis")
    public void testCreateAnalysis() throws MLHttpClientException, IOException {
        createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
    }

    /**
     * Test creating an analysis with CORS.
     *
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 1, description = "Create an analysis with CORS")
    public void testCreateAnalysisCrossOrigin() throws MLHttpClientException, IOException {
        createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME_2,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
    }
    
    /**
     * Test creating an existing analysis.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(priority=2, description = "Create an existing analysis")
    public void testCreateExistingAnalysis() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES));
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating an analysis without the Name.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create an analysis without a name")
    public void testCreateAnalysisWithoutName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(null, MLIntegrationTestConstants.PROJECT_ID_DIABETES);
        assertEquals("Unexpected response received", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating an analysis without a project ID.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Create an analysis without a ProjectId")
    public void testCreateAnalysisWithoutProjectID() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis("TestAnalysisForAnalysis", -1);
        assertEquals("Unexpected response received", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}