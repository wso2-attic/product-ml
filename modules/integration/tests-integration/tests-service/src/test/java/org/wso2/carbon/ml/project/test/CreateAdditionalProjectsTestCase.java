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

package org.wso2.carbon.ml.project.test;

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
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Creating test cases related to additional projects for different datasets
 */
@Test(groups="createAdditionalProjects", dependsOnGroups = "createProjects")
public class CreateAdditionalProjectsTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private CloseableHttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        //Check whether the datasets exists.
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_YACHT);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_YACHT
                    + " is not available");
        }
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_BREAST_CANCER);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_BREAST_CANCER
                    + " is not available");
        }
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_FOREST_FIRES);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_FOREST_FIRES
                    + " is not available");
        }
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_GAMMA_TELESCOPE);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_GAMMA_TELESCOPE
                    + " is not available");
        }
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_AZURE_STREAMING);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_GAMMA_TELESCOPE
                    + " is not available");
        }
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_TITANIC);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_GAMMA_TELESCOPE
                    + " is not available");
        }
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_AUTOMOBILE);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_GAMMA_TELESCOPE
                    + " is not available");
        }
    }

    /**
     * Creates a test case for creating a project for forest fires data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for forest fires dataset")
    public void testCreateProjectForestFires() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_FOREST_FIRES,
                MLIntegrationTestConstants.DATASET_NAME_FOREST_FIRES);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for breast cancer dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for breast cancer dataset")
    public void testCreateProjectBreastCancer() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_BREAST_CANCER,
                MLIntegrationTestConstants.DATASET_NAME_BREAST_CANCER);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for gamma telescope dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project gamma telescope dataset")
    public void testCreateProjectGammaTelescope() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_GAMMA_TELESCOPE,
                MLIntegrationTestConstants.DATASET_NAME_GAMMA_TELESCOPE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for Yacht hydrodynamics data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for yacht hydrodynamics dataset")
    public void testCreateProjectYachtHydrodynamics() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_YACHT,
                MLIntegrationTestConstants.DATASET_NAME_YACHT);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for Azure streaming data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for azure streaming dataset")
    public void testCreateProjectAzureStreaming() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_AZURE_STREAMING,
                MLIntegrationTestConstants.DATASET_NAME_AZURE_STREAMING);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for Titanic data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for titanic dataset")
    public void testCreateProjectTitanic() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_TITANIC,
                MLIntegrationTestConstants.DATASET_NAME_TITANIC);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates a test case for creating a project for Automobile data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for automobile dataset")
    public void testCreateProjectAutomobile() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_AUTOMOBILE,
                MLIntegrationTestConstants.DATASET_NAME_AUTOMOBILE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}
