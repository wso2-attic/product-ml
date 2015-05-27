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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * Contains test cases related to creating additional datasets for different algorithms
 */

@Test(groups="createAdditionalDatasets", dependsOnGroups = "createDatasets")
public class CreateAdditionalDatasetsTestCase extends MLBaseTest{

    private MLHttpClient mlHttpclient;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
    }

    /**
     * Creates dataset for numerical prediction tests - Forest Fires
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of Forest fires data from a CSV file")
    public void testCreateDatasetForestFires() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_FOREST_FIRES,
                "1.0", MLIntegrationTestConstants.FOREST_FIRES_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates dataset for classification and clustering tests - Breast Cancer
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of breast cancer Wisconsin data from a CSV file")
    public void testCreateDatasetBreastCancer() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_BREAST_CANCER,
                "1.0", MLIntegrationTestConstants.BREAST_CANCER_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates dataset for classification and clustering tests tests - Gamma telescope
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of protein tertiary structure data from a CSV file")
    public void testCreateDatasetGammaTelescope() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_GAMMA_TELESCOPE,
                "1.0", MLIntegrationTestConstants.GAMMA_TELESCOPE_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates dataset for numerical prediction tests - Yacht hydrodynamics
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of yacht hydrodynamics data from a CSV file")
    public void testCreateDatasetYachtHydrodynamics() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_YACHT,
                "1.0", MLIntegrationTestConstants.YACHT_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates dataset for Missing values and categorical values tests - Azure streaming
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of azure streaming data from a CSV file")
    public void testCreateDatasetAzureStreaming() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_AZURE_STREAMING,
                "1.0", MLIntegrationTestConstants.AZURE_STREAMING_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates dataset for Missing values and categorical values tests - Titanic
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of titanic data from a CSV file")
    public void testCreateDatasetTitanic() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_TITANIC,
                "1.0", MLIntegrationTestConstants.TITANIC_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }

    /**
     * Creates dataset for Missing values and categorical values tests - Automobile
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of automobile data from a CSV file")
    public void testCreateDatasetAutomobile() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_AUTOMOBILE,
                "1.0", MLIntegrationTestConstants.AUTOMOBILE_DATASET_SAMPLE);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}
