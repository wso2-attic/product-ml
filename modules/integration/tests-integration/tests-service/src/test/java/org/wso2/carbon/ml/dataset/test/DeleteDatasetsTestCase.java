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
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * Contains test cases related to deletion of datasets
 */
@Test(groups="deleteDatasets", dependsOnGroups="getDatasets")
public class DeleteDatasetsTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = getMLHttpClient();
    }
    
    /**
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Delete a dataset with a known ID")
    public void testDeleteVersionSet() throws MLHttpClientException, IOException, JSONException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/datasets/versions/" + MLIntegrationTestConstants
                    .VERSIONSET_ID);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
        response.close();
    }

    /**
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Delete a dataset with a known ID")
    public void testDeleteDataset() throws MLHttpClientException, IOException, JSONException {
        CloseableHttpResponse response = mlHttpclient.doHttpDelete("/api/datasets/" + MLIntegrationTestConstants
                    .DATASET_ID_DIABETES);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
        response.close();
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}