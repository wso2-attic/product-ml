/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ml.rest.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.MLAnalysis;
import org.wso2.carbon.ml.commons.domain.MLCustomizedFeature;
import org.wso2.carbon.ml.commons.domain.MLHyperParameter;
import org.wso2.carbon.ml.commons.domain.MLModelConfiguration;
import org.wso2.carbon.ml.core.exceptions.MLAnalysisHandlerException;
import org.wso2.carbon.ml.core.impl.MLAnalysisHandler;

/**
 * This class is to handle REST verbs GET , POST and DELETE.
 */
@Path("/analyses")
public class AnalysisApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(AnalysisApiV10.class);
    private MLAnalysisHandler mlAnalysisHandler;
    
    public AnalysisApiV10() {
        mlAnalysisHandler = new MLAnalysisHandler();
    }

    /**
     * Create a new Analysis of a project.
     */
    @POST
    @Produces("application/json")
    public Response createAnalysis(MLAnalysis analysis) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        try {
            int tenantId = carbonContext.getTenantId();
            String userName = carbonContext.getUsername();
            analysis.setTenantId(tenantId);
            analysis.setUserName(userName);
            mlAnalysisHandler.createAnalysis(analysis);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error("Error occured while creating an analysis : " + analysis+ " : " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{analysisId}/features")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addCustomizedFeatures(@PathParam("analysisId") long analysisId, List<MLCustomizedFeature> customizedFeatures) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addCustomizedFeatures(analysisId, customizedFeatures);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding customized features for the analysis [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{analysisId}/features/defaults")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addDefaultsIntoCustomizedFeatures(@PathParam("analysisId") long analysisId, MLCustomizedFeature customizedValues) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            customizedValues.setTenantId(tenantId);
            customizedValues.setUserName(userName);
            customizedValues.setLastModifiedUser(userName);
            
            mlAnalysisHandler.addDefaultsIntoCustomizedFeatures(analysisId, customizedValues);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding customized features for the analysis [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{analysisId}/configurations")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addModelConfiguration(@PathParam("analysisId") long analysisId, List<MLModelConfiguration> modelConfigs) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addModelConfigurations(analysisId, modelConfigs);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding model configurations for the analysis [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{analysisId}/hyperParams")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addHyperParameters(@PathParam("analysisId") long analysisId, List<MLHyperParameter> hyperParameters) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addHyperParameters(analysisId, hyperParameters);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding hyper parameters for the analysis [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/{analysisId}/hyperParams/defaults")
    @Produces("application/json")
    @Consumes("application/json")
    public Response addDefaultsIntoHyperParameters(@PathParam("analysisId") long analysisId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.addDefaultsIntoHyperParameters(analysisId);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while adding hyper parameters for the analysis [id] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisId, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/{analysisName}")
    @Produces("application/json")
    public Response getAnalysis(@PathParam("analysisName") String analysisName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLAnalysis analysis = mlAnalysisHandler.getAnalysis(tenantId, userName, analysisName);
            if (analysis == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(analysis).build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while retrieving an analysis [name] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisName, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Produces("application/json")
    public Response getAllAnalyses() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLAnalysis> analyses = mlAnalysisHandler.getAnalyses(tenantId, userName);
            return Response.ok(analyses).build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while retrieving all analyses of tenant [id] %s and [user] %s . Cause: %s",
                    tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    @DELETE
    @Path("/{analysisName}")
    @Produces("application/json")
    public Response deleteAnalysis(@PathParam("analysisName") String analysisName) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            mlAnalysisHandler.deleteAnalysis(tenantId, userName, analysisName);
            return Response.ok().build();
        } catch (MLAnalysisHandlerException e) {
            logger.error(String.format(
                    "Error occured while deleting an analysis [name] %s of tenant [id] %s and [user] %s . Cause: %s",
                    analysisName, tenantId, userName, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
