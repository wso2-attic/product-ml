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
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to creating projects
 */
@Test(groups = "ml-projects")
public class MLProjectsTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException, MLHttpClientException, IOException, JSONException {
        super.init();
        mlHttpclient = getMLHttpClient();
        createDataset(MLIntegrationTestConstants.DATASET_NAME_DIABETES, "1.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        isDatasetProcessed(getVersionSetIds().get(0), MLIntegrationTestConstants.THREAD_SLEEP_TIME_LARGE, 1000);
    }

    /**
     * Test creating a project.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 1, description = "Create a project")
    public void testCreateProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_DIABETES,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test creating a project with a duplicate project name.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Create a project with duplicate Name")
    public void testCreateProjectWithDuplicateName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject(MLIntegrationTestConstants.PROJECT_NAME_DIABETES,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test creating a project without the project name.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Create a project without name")
    public void testCreateProjectWithoutName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject(null,
                MLIntegrationTestConstants.DATASET_NAME_DIABETES);
        assertEquals("Unexpected response received", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test creating a project without the dataset name.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 2, description = "Create a project without a dataset")
    public void testCreateProjectWithoutDataset() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.createProject("TestProjectForCreatProjectTestCase-2", null);
        assertEquals("Unexpected response received", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test retrieving all projects.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 5, description = "Retrieve all projects")
    public void testGetAllProjects() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving all projects with analyses.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 5, description = "Retrieve all projects with analyses")
    public void testGetAllProjectsWithAnalyses() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/analyses");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test retrieving projects of a dataset with analyses.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 5, description = "Retrieve projects of a dataset with analyses")
    public void testGetProjectsOfDatasetWithAnalyses() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/analyses?datasetName="
                + MLIntegrationTestConstants.DATASET_NAME_DIABETES);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test retrieving a project.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 6, description = "Retrieve a project")
    public void testGetProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/"
                + MLIntegrationTestConstants.PROJECT_NAME_DIABETES);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 6, description = "Retrieve a non-existing project")
    public void testGetNonExistingProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/projects/"
                + "NON_EXISTING_PROJECT");
        assertEquals("Unexpected response received", Response.Status.NOT_FOUND.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test deleting a project.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 7, description = "Delete an exsisting project")
    public void testDeleteProject() throws MLHttpClientException, IOException {
        int projectId = mlHttpclient.getProjectId(MLIntegrationTestConstants.PROJECT_NAME_DIABETES);
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/projects/" + projectId);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test deleting a non-existing project.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(priority = 8, description = "Delete an exsisting project")
    public void testDeleteNonExistingProject() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/projects/" + 999);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}
