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

package org.wso2.carbon.ml.dataset.test;

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
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to creating datasets
 */
@Test(groups = "createDatasets")
public class CreateDatasetTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws MLIntegrationBaseTestException {
        super.init();
        mlHttpclient = getMLHttpClient();
    }

    /**
     * Test creating a dataset from a valid csv file.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset from a CSV file")
    public void testCreateDatasetFromFile() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(
                MLIntegrationTestConstants.DATASET_NAME_DIABETES, "1.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Test creating a dataset from a valid DAS table.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
/*    @Test(description = "Create a dataset from a DAS table", dependsOnMethods = "testCreateDatasetFromFile")
    public void testCreateDatasetFromDAS() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromDAS(MLIntegrationTestConstants.DATASET_NAME_DAS,
                "1.0", MLIntegrationTestConstants.DAS_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }*/

    /**
     * Test Creating a new version of an existing dataset
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    /*@Test(description = "Create a new version of an existing dataset", dependsOnMethods = "testCreateDatasetFromFile")
    public void testCreateNewDatasetVersion() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(
                MLIntegrationTestConstants.DATASET_NAME_DIABETES, "2.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }*/

    /**
     * Test Creating a new version of an existing dataset
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a duplicate version of an existing dataset", dependsOnMethods = "testCreateDatasetFromFile")
    public void testCreateDuplicateDatasetVersion() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(
                MLIntegrationTestConstants.DATASET_NAME_DIABETES, "1.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test creating a dataset from a non-existing csv file.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    /*
     * @Test(description = "Create a dataset from a non-existing CSV file") public void
     * testCreateDatasetFromNonExistingFile() throws MLHttpClientException, IOException { CloseableHttpResponse response
     * = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_DIABETES, "1.0", "data/xxx.csv");
     * assertEquals("Unexpected response recieved", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
     * .getStatusLine().getStatusCode()); response.close(); }
     */

    /**
     * Test creating a dataset without name.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset without name")
    public void testCreateDatasetWithoutName() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(null, "1.0",
                MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test creating a dataset without version.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset without version")
    public void testCreateDatasetWithoutVersion() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_3",
                null, MLIntegrationTestConstants.DIABETES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.BAD_REQUEST.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test creating a dataset without data source.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset without datasource")
    public void testCreateDatasetWithoutDataSource() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV("SampleDataForCreateDatasetTestCase_4",
                "1.0", null);
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * Test creating a dataset for an invalid DAS table.
     * 
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset for an invalid DAS table")
    public void testCreateDatasetForInvalidDASTable() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromDAS("SampleDataForCreateDatasetTestCase_5",
                "1.0", "INVALID_TABLE");
        assertEquals("Unexpected response received", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response
                .getStatusLine().getStatusCode());
        response.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}
