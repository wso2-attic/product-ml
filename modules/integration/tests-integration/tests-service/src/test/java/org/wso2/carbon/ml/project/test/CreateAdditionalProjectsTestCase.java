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
@Test(groups="createAdditionalProjects")
public class CreateAdditionalProjectsTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private CloseableHttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
        //Check whether the datasets exists.
        response = mlHttpclient.doHttpGet("/api/datasets/" + MLIntegrationTestConstants
                .DATASET_ID_CONCRETE_SLUMP);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_CONCRETE_SLUMP
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
                .DATASET_ID_PROTEIN_TERTIARY_STRUCTURE);
        if (Response.Status.OK.getStatusCode() != response.getStatusLine().getStatusCode()) {
            throw new SkipException("Skipping tests because dataset with ID: " + MLIntegrationTestConstants.DATASET_ID_PROTEIN_TERTIARY_STRUCTURE
                    + " is not available");
        }
    }

    /**
     * Creates a test case for creating a project for concrete slump data set
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project for concrete slump dataset")
    public void testCreateProjectConcreteSlump() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_CONCRETE_SLUMP,
                MLIntegrationTestConstants.DATASET_NAME_CONCRETE_SLUMP);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
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
     * Creates a test case for creating a project for protein tertiary structure dataset
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a project protein tertiary structure dataset")
    public void testCreateProjectProteinTertiaryStructure() throws MLHttpClientException, IOException {
        response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_PROTEIN_TERTIARY_STRUCTURE,
                MLIntegrationTestConstants.DATASET_NAME_PROTEIN_TERTIARY_STRUCTURE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}
