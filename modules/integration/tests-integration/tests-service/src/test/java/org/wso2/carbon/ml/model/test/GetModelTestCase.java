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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * Contains test cases related to retrieving models
 */
@Test(groups="getModels", dependsOnGroups="createModels")
public class GetModelTestCase extends MLBaseTest {

    private MLHttpClient mlHttpclient;
    private int projectId;
    private int analysisId;
    private int versionSetId;
    private int modelId;
    private String modelName;
    
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
        modelName = createModel(analysisId, versionSetId);
        modelId = getModelId(modelName);
    }

    /**
     * Get model from name
     * @throws MLHttpClientException 
     * @throws IOException 
     */
    @Test(description = "retrieve a model")
    public void testGetModel() throws MLHttpClientException, IOException, JSONException {
        CloseableHttpResponse response = mlHttpclient.doHttpGet("/api/models/" + modelName);
        assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                .getStatusCode());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        bufferedReader.close();
        response.close();
        //Check whether the correct model is retrieved
        assertEquals("Incorrect model retrieved.", modelId,responseJson.getInt("id"));
    }
    
    @AfterClass(alwaysRun = true)
    public void tearDown() throws MLHttpClientException {
        super.destroy();
    }
}