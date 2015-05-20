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
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * Creates tests related to creating analyses in projects for different datasets.
 */
@Test(groups="createAdditionalAnalyses")
public class CreateAdditionalAnalysesTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;

    @BeforeClass(alwaysRun = true, groups = "wso2.ml.integration")
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        // Check whether the projects exists.
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_YACHT);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_BREAST_CANCER);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_FOREST_FIRES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
        response = mlHttpclient.doHttpGet("/api/projects/" + MLIntegrationTestConstants
                .PROJECT_NAME_GAMMA_TELESCOPE);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because a project is not available");
        }
    }


    /**
     * Test creating an analysis for forest fires dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(groups = "createAnalysisForestFires", description = "Create an analysis for forest fires dataset")
    public void testCreateAnalysisForestFires() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES));
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test creating an analysis for Yacht hydrodynamics dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(groups = "createAnalysisYachtHydrodynamics", description = "Create an analysis for yacht hydrodynamics dataset")
    public void testCreateAnalysisYachtHydrodynamics() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_YACHT));
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test creating an analysis for breast cancer dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(groups = "createAnalysisBreastCancer", description = "Create an analysis for breast cancer dataset")
    public void testCreateAnalysisBreastCancer() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER));
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test creating an analysis for gamma telescope dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(groups = "createAnalysisGammaTelescope", description = "Create an analysis for gamma telescope dataset")
    public void testCreateAnalysisProteinTertiaryStructure() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createAnalysis(MLIntegrationTestConstants.ANALYSIS_NAME,
                mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_GAMMA_TELESCOPE));
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}
