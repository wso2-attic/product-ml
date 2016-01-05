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

package org.wso2.carbon.ml.integration.common.utils;

import static org.testng.AssertJUnit.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Instance;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.test.utils.common.HomePageGenerator;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;
import org.wso2.carbon.ml.integration.common.utils.exception.MLIntegrationBaseTestException;

/**
 * This is the base class for all Integration tests. Provides functions that are common 
 * to all the integration tests in ML.
 */
public abstract class MLBaseTest {

    protected AutomationContext mlAutomationContext;
    protected Tenant tenantInfo;
    protected User userInfo;
    protected TestUserMode userMode;
    protected Instance instance;
    private MLHttpClient mlHttpclient;
    private List<Integer> datasetIds;
    private List<Integer> datasetVersionIds;
    private List<Integer> projectIds;
    private List<Integer> analysisIds;
    private List<Integer> modelIds;

    protected void init() throws MLIntegrationBaseTestException {
        try {
            this.mlAutomationContext = new AutomationContext(MLIntegrationTestConstants.ML_PRODUCT_GROUP, 
                    TestUserMode.SUPER_TENANT_ADMIN);
            //get the current tenant as the userType(TestUserMode)
            this.tenantInfo = this.mlAutomationContext.getContextTenant();
            //get the user information initialized with the system
            this.userInfo = this.tenantInfo.getContextUser();
            this.instance = mlAutomationContext.getInstance();
            this.mlHttpclient = new MLHttpClient(instance, userInfo);
            this.datasetIds = new ArrayList<Integer>();
            this.projectIds = new ArrayList<Integer>();
            this.analysisIds = new ArrayList<Integer>();
            this.datasetVersionIds = new ArrayList<Integer>();
            this.modelIds = new ArrayList<Integer>();
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("Failed to get the ML automation context: ", e);
        } 
    }
    
    protected MLHttpClient getMLHttpClient() {
        return this.mlHttpclient;
    }
    
    /**
     * Creates a dataset with given specification and keep a reference to the id.
     */
    protected int createDataset(String name, String version, String dataset) throws MLHttpClientException, IOException, JSONException {
        CloseableHttpResponse response = null;
        try {
            response = mlHttpclient.uploadDatasetFromCSV(name, version, dataset);
            assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
            int id = getId(response);
            datasetIds.add(id);
            int versionSetId = getVersionSetId(id, version);
            datasetVersionIds.add(versionSetId);
            return id;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    /**
     * Creates a dataset with given specification and keep a reference to the id.
     */
    protected int createDatasetFromDASTable(String name, String version, String table) throws MLHttpClientException, IOException, JSONException {
        CloseableHttpResponse response = null;
        try {
            response = mlHttpclient.uploadDatasetFromDAS(name, version, table);
            assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
            int id = getId(response);
            datasetIds.add(id);
            int versionSetId = getVersionSetId(id, version);
            datasetVersionIds.add(versionSetId);
            return id;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    /**
     * Creates a project with given specification and keep a reference to the id.
     */
    protected int createProject(String name, String datasetName) throws MLHttpClientException, IOException {
        CloseableHttpResponse response = null;
        try {
            response = mlHttpclient.createProject(name, datasetName);
            assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
            int id = mlHttpclient.getProjectId(name);
            projectIds.add(id);
            return id;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    /**
     * Creates an analysis with given specification and keep a reference to the id.
     */
    protected int createAnalysis(String name, int projectId) throws MLHttpClientException, IOException {
        CloseableHttpResponse response = null;
        try {
             response = mlHttpclient.createAnalysis(name, projectId);
            assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
            int id = mlHttpclient.getAnalysisId(projectId, name);
            analysisIds.add(id);
            return id;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    /**
     * Creates a model with given specification and keep a reference to the id.
     */
    protected String createModel(int analysisId, int versionSetId) throws MLHttpClientException, IOException {
        CloseableHttpResponse response = null;
        try {
            response = mlHttpclient.createModel(analysisId, versionSetId);
            assertEquals("Unexpected response received", Response.Status.OK.getStatusCode(), response.getStatusLine()
                    .getStatusCode());
            String modelName = mlHttpclient.getModelName(response);
            int id = mlHttpclient.getModelId(modelName);
            modelIds.add(id);
            return modelName;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    protected boolean isDatasetProcessed(int versionSetId, long timeout, int frequency) {
        try {
            return mlHttpclient.checkDatasetStatus(versionSetId, timeout, frequency);
        } catch (MLHttpClientException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    protected int getModelId(String modelName) throws MLHttpClientException {
        return mlHttpclient.getModelId(modelName);
    }
    
    protected int getVersionSetId(int datasetId, String version) throws MLHttpClientException {
            int id = mlHttpclient.getVersionSetIdOfDataset(datasetId, version);
            return id;
    }
    
    /**
     * Extracts the value of key: "id" from a response
     * @param response
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private static int getId(CloseableHttpResponse response) throws IOException, JSONException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
        JSONObject responseJson = new JSONObject(bufferedReader.readLine());
        bufferedReader.close();
        response.close();

        // Gets the ID of the dataset.
        int id = responseJson.getInt("id");
        return id;
    }
    
    
    /**
     * Get the non-secured URL of a given service.
     * 
     * @param serviceName   Name of the service of which URL is needed.
     * @return              Non-secured URL of the service.
     * @throws              MLIntegrationBaseTestException
     */
    protected String getServiceUrlHttp(String serviceName) throws MLIntegrationBaseTestException {
        String serviceUrl;
        try {
            serviceUrl = this.mlAutomationContext.getContextUrls().getServiceUrl() + "/" + serviceName;
            validateServiceUrl(serviceUrl, this.tenantInfo);
            return serviceUrl;
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the service (http) URL: ", e);
        }
        
    }

    
    /**
     * Get the secured URL of a given service.
     * 
     * @param serviceName   Name of the service of which URL is needed.
     * @return              Secured URL of the service.
     * @throws              MLIntegrationBaseTestException
     */
    protected String getServiceUrlHttps(String serviceName) throws MLIntegrationBaseTestException {
        String serviceUrl;
        try {
            serviceUrl = this.mlAutomationContext.getContextUrls().getSecureServiceUrl() + "/" + serviceName;
            validateServiceUrl(serviceUrl, this.tenantInfo);
            return serviceUrl;
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the secure service (https) "
            		+ "URL: ", e);
        }
    }

    /**
     * Get the URL of the carbon console login
     * 
     * @return  URL of the carbon console login
     * @throws  MLIntegrationBaseTestException
     */
    protected String getCarbonLoginURL() throws MLIntegrationBaseTestException {
        try {
            return HomePageGenerator.getProductHomeURL(this.mlAutomationContext);
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the Carbon login URL", e);
        }
    }

    /**
     * Get the web-app URL
     * 
     * @return  Web-app URL
     * @throws  MLIntegrationBaseTestException
     */
    protected String getMLUiUrl() throws MLIntegrationBaseTestException {
        try {
        	String mlWebAppUrl = UrlGenerationUtil.getWebAppURL(this.tenantInfo, this.mlAutomationContext.getInstance())
        			.split("\\/t\\/")[0] + MLIntegrationTestConstants.ML_UI;
        	return mlWebAppUrl;
        } catch (XPathExpressionException e) {
            throw new MLIntegrationBaseTestException("An error occured while retrieving the ML UI URL: ", e);
        }
    }

    /**
     * Checks whether the URl is a valid one for the tenant.
     * 
     * @param serviceUrl    URL to be validated.
     * @param tenant        logged in tenant.
     */
    protected void validateServiceUrl(String serviceUrl, Tenant tenant) {
        // if user mode is null can not validate the service url
        if (this.userMode != null) {
            if (this.userMode == TestUserMode.TENANT_ADMIN || userMode == TestUserMode.TENANT_USER) {
                Assert.assertTrue(serviceUrl.contains("/t/" + tenant.getDomain() + "/"), "invalid service url for"
                		+ " tenant. " + serviceUrl);
            } else {
                Assert.assertFalse(serviceUrl.contains("/t/"), "Invalid service url:" + serviceUrl + " for tenant: " + tenant);
            }
        }
    }
    
    /**
     * Retrieves the absolute path of the model storage directory
     * 
     * @return  Absolute path of the model storage directory
     */
    protected static String getModelStorageDirectory() {
        File modelFileStorage = new File(MLIntegrationTestConstants.FILE_STORAGE_LOCATION);
        if (!modelFileStorage.exists() || !modelFileStorage.isDirectory() ) {
            modelFileStorage.mkdirs();
        }
        return modelFileStorage.getAbsolutePath();
    }
    
    protected List<Integer> getVersionSetIds() {
        return datasetVersionIds;
    }
    
    protected void destroy() throws MLHttpClientException {
        Assert.assertNotNull(modelIds, "Test case is not properly initialized. Call super.init()");
        Assert.assertNotNull(analysisIds, "Test case is not properly initialized. Call super.init()");
        Assert.assertNotNull(projectIds, "Test case is not properly initialized. Call super.init()");
        Assert.assertNotNull(datasetVersionIds, "Test case is not properly initialized. Call super.init()");
        Assert.assertNotNull(datasetIds, "Test case is not properly initialized. Call super.init()");
        
        for (Integer id : modelIds) {
            mlHttpclient.doHttpDelete("/api/models/"+id);
        }
        
        for (Integer id : analysisIds) {
            mlHttpclient.doHttpDelete("/api/analyses/"+id);
        }
        
        for (Integer id : projectIds) {
            mlHttpclient.doHttpDelete("/api/projects/"+id);
        }
        
        for (Integer id : datasetVersionIds) {
            mlHttpclient.doHttpDelete("/api/datasets/versions/"+id);
        }
        
        for (Integer id : datasetIds) {
            mlHttpclient.doHttpDelete("/api/datasets/"+id);
        }
        
        modelIds = null;
        analysisIds = null;
        projectIds = null;
        datasetVersionIds = null;
        datasetIds = null;
    }

    /**
     * A test case for exporting a model in pmml format
     *
     * @throws MLHttpClientException
     */
    protected void testExportAsPMML(int modelId) throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.exportAsPMML(modelId);
        assertEquals("Pmml download has failed. Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }

    /**
     * A test case for publishing a model to registry in pmml format
     *
     * @throws MLHttpClientException
     */
    protected void testPublishAsPMML(int modelId) throws MLHttpClientException, IOException {
        CloseableHttpResponse response = mlHttpclient.doHttpPost("/api/models/" + modelId + "/publish?mode=pmml", null);
        assertEquals("Pmml publish has failed. Unexpected response received", Response.Status.OK.getStatusCode(),
                response.getStatusLine().getStatusCode());
        response.close();
    }
    
    
}