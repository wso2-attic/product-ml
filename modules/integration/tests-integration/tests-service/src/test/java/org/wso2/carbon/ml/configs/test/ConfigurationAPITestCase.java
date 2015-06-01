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

package org.wso2.carbon.ml.configs.test;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * Class contains test cases related to retrieving ml configuration
 */
@Test(groups="getConfigs")
public class ConfigurationAPITestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    
    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
    }

    /**
     * Test retrieving configs of all algorithms.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Get all algorithms")
    public void testGetAllAlgorithmsConfig() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/configs/algorithms");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving an algorithm config by name.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Retrieve an algorithm")
    public void testGetAlgorithmConfig() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/configs/algorithms/LOGISTIC_REGRESSION");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving default hyper params of an algorithm.
     * 
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "Retrieve hyper params of an algorithm")
    public void testGetHyperParamsOfAlgorithm() throws MLHttpClientException, IOException  {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/configs/algorithms/LOGISTIC_REGRESSION/hyperParams");
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
    
    /**
     * Test retrieving a non-existing algorithm.
     * 
     * @throws MLHttpClientException 
     * @throws IOException
     */
    @Test(description = "Retrieve a non-existing algorithm")
    public void testGetNonExistingAnalysis() throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/configs/algorithms/" + "nonExistinfAlgo");
        assertEquals("Unexpected response received", Response.Status.NOT_FOUND.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        response.close();
    }
}