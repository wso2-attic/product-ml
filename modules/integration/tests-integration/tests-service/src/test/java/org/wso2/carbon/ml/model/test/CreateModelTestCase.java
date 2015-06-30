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
 * Contains test cases related to creating models
 */
@Test(groups="createModels")
public class CreateModelTestCase extends MLBaseTest {
    
    private MLHttpClient mlHttpclient;
    private int projectId;
    private int analysisId;
    private int versionSetId;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = getMLHttpClient();
        String version = "1.0";
        int datasetId = createDataset(MLIntegrationTestConstants.DATASET_NAME_DIABETES, version,
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        versionSetId = getVersionSetId(datasetId, version);
        projectId = createProject(MLIntegrationTestConstants.PROJECT_NAME_DIABETES,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
        analysisId = createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME, projectId);
    }

    /**
     * Test creating a model.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(priority=1, description = "Create a Model")
    public void testCreateModel() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createModel(analysisId, versionSetId);
        MLIntegrationTestConstants.MODEL_NAME = mlHttpclient.getModelName(response);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a Model with an invalid analysis.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(priority=1, description = "Create a Model with an invalid analysis")
    public void testCreateModelWithInvalidAnalysis() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.createModel(999, versionSetId);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    /**
     * Test creating a Model with an invalid version-set.
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(priority=1, description = "Create a Model with an invalid versionset")
    public void testCreateModelWithInvalidVersionset() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createModel(analysisId, 999);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}