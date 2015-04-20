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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ml.commons.domain.ClusterPoint;
import org.wso2.carbon.ml.commons.domain.MLDataset;
import org.wso2.carbon.ml.commons.domain.MLDatasetVersion;
import org.wso2.carbon.ml.commons.domain.ScatterPlotPoints;
import org.wso2.carbon.ml.core.exceptions.MLDataProcessingException;
import org.wso2.carbon.ml.core.impl.MLDatasetProcessor;
import org.wso2.carbon.ml.core.impl.MLModelHandler;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.rest.api.model.MLDatasetBean;
import org.wso2.carbon.ml.rest.api.model.MLVersionBean;

/**
 * This class defines the ML dataset API.
 */
@Path("/datasets")
public class DatasetApiV10 extends MLRestAPI {

    private static final Log logger = LogFactory.getLog(DatasetApiV10.class);
    /*
     * Delegates all the dataset related operations.
     */
    private MLDatasetProcessor datasetProcessor;
    private MLModelHandler mlModelHandler;

    public DatasetApiV10() {
        datasetProcessor = new MLDatasetProcessor();
        mlModelHandler = new MLModelHandler();
    }

    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }

    /**
     * Upload a new data-set.
     */
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public Response uploadDataset(MLDataset dataset) {
        if (dataset.getName() == null || dataset.getName().isEmpty() || dataset.getSourcePath() == null
                || dataset.getVersion() == null || dataset.getDataSourceType() == null
                || dataset.getDataSourceType().isEmpty() || dataset.getDataType() == null
                || dataset.getDataType().isEmpty()) {
            logger.error("Required parameters are missing: " + dataset);
            return Response.status(Response.Status.BAD_REQUEST).entity("Required parameters missing").build();
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            dataset.setTenantId(tenantId);
            dataset.setUserName(userName);
            datasetProcessor.process(dataset);
            return Response.ok(dataset).build();
        } catch (MLDataProcessingException e) {
            logger.error("Error occurred while uploading a dataset : " + dataset, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get all datasets of this tenant and user. This doesn't return version sets.
     */
    @GET
    @Produces("application/json")
    public Response getAllDatasets() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLDataset> datasets = datasetProcessor.getAllDatasets(tenantId, userName);
            return Response.ok(datasets).build();
        } catch (MLDataProcessingException e) {
            logger.error(String.format("Error occurred while retrieving all datasets of tenant [id] %s [user] %s ",
                    tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get all datasets and their versions of a given user.
     */
    @GET
    @Path("/versions")
    @Produces("application/json")
    public Response getAllDatasetVersions() {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLDatasetBean> datasetBeans = new ArrayList<MLDatasetBean>();
            List<MLDataset> datasets = datasetProcessor.getAllDatasets(tenantId, userName);
            for (MLDataset mlDataset : datasets) {
                MLDatasetBean datasetBean = new MLDatasetBean();
                long datasetId = mlDataset.getId();
                datasetBean.setId(datasetId);
                datasetBean.setName(mlDataset.getName());
                datasetBean.setComments(mlDataset.getComments());
                List<MLVersionBean> versionBeans = new ArrayList<MLVersionBean>();
                List<MLDatasetVersion> versions = datasetProcessor.getAllVersionsetsOfDataset(tenantId, userName,
                        datasetId);
                for (MLDatasetVersion mlDatasetVersion : versions) {
                    MLVersionBean versionBean = new MLVersionBean();
                    versionBean.setId(mlDatasetVersion.getId());
                    versionBean.setVersion(mlDatasetVersion.getVersion());
                    versionBeans.add(versionBean);
                }
                datasetBean.setVersions(versionBeans);
                datasetBeans.add(datasetBean);
            }
            return Response.ok(datasetBeans).build();
        } catch (MLDataProcessingException e) {
            logger.error(String.format(
                    "Error occurred while retrieving all dataset versions of tenant [id] %s [user] %s ", tenantId,
                    userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get the dataset corresponds to a given dataset id.
     */
    @GET
    @Path("/{datasetId}")
    @Produces("application/json")
    public Response getDataset(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLDataset dataset = datasetProcessor.getDataset(tenantId, userName, datasetId);
            if (dataset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(dataset).build();
        } catch (MLDataProcessingException e) {
            logger.error(String.format(
                    "Error occurred while retrieving the dataset with the [id] %s of tenant [id] %s [user] %s ",
                    datasetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get all versions of a dataset with a given id.
     */
    @GET
    @Path("/{datasetId}/versions")
    @Produces("application/json")
    public Response getAllVersionsets(@PathParam("datasetId") long datasetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<MLDatasetVersion> versionsets = datasetProcessor.getAllDatasetVersions(tenantId, userName, datasetId);
            return Response.ok(versionsets).build();
        } catch (MLDataProcessingException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving all versions of a dataset with the [id] %s of tenant [id] %s [user] %s ",
                            datasetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get a dataset version with a given id.
     */
    @GET
    @Path("/versions/{versionsetId}")
    @Produces("application/json")
    public Response getVersionset(@PathParam("versionsetId") long versionsetId) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            MLDatasetVersion versionset = datasetProcessor.getVersionset(tenantId, userName, versionsetId);
            if (versionset == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(versionset).build();
        } catch (MLDataProcessingException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving the dataset version with the [id] %s of tenant [id] %s [user] %s ",
                            versionsetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get scatter plot points of a dataset version.
     */
    @POST
    @Path("/{datasetId}/scatter")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getScatterPlotPointsOfLatestVersion(@PathParam("datasetId") long datasetId,
            ScatterPlotPoints scatterPlotPoints) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            scatterPlotPoints.setTenantId(tenantId);
            scatterPlotPoints.setUser(userName);
            List<Object> points = datasetProcessor.getScatterPlotPointsOfLatestVersion(datasetId, scatterPlotPoints);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving scatter plot points of dataset [id] %s of tenant [id] %s [user] %s ",
                            datasetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * Get scatter plot points of a dataset version.
     */
    @POST
    @Path("/versions/{versionsetId}/scatter")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getScatterPlotPoints(@PathParam("versionsetId") long versionsetId,
            ScatterPlotPoints scatterPlotPoints) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            scatterPlotPoints.setTenantId(tenantId);
            scatterPlotPoints.setUser(userName);
            scatterPlotPoints.setVersionsetId(versionsetId);
            List<Object> points = datasetProcessor.getScatterPlotPoints(scatterPlotPoints);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving scatter plot points of dataset version [id] %s of tenant [id] %s [user] %s ",
                            versionsetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
    
    /**
     * Get chart sample points of a dataset version for a feature list.
     */
    @GET
    @Path("/{datasetId}/charts")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getChartSamplePointsOfLatestVersion(@PathParam("datasetId") long datasetId,
            @QueryParam("features") String featureListString) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<Object> points = datasetProcessor.getChartSamplePoints(tenantId, userName, datasetId,
                    featureListString);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving chart sample points of dataset version [id] %s of tenant [id] %s [user] %s ",
                            datasetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    /**
     * Get chart sample points of a dataset version for a feature list.
     */
    @GET
    @Path("/versions/{versionsetId}/charts")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getChartSamplePoints(@PathParam("versionsetId") long versionsetId,
            @QueryParam("features") String featureListString) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<Object> points = datasetProcessor.getChartSamplePoints(tenantId, userName, versionsetId,
                    featureListString);
            return Response.ok(points).build();
        } catch (MLDataProcessingException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving chart sample points of dataset version [id] %s of tenant [id] %s [user] %s ",
                            versionsetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    /**
     * Get cluster points of a dataset for a feature list.
     */
    @GET
    @Path("/{datasetId}/cluster")
    @Produces("application/json")
    @Consumes("application/json")
    public Response getClusterPoints(@PathParam("datasetId") long datasetId,
                                     @QueryParam("features") String featureListString, @QueryParam("noOfClusters") int noOfClusters) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = carbonContext.getTenantId();
        String userName = carbonContext.getUsername();
        try {
            List<ClusterPoint> points = mlModelHandler.getClusterPoints(tenantId, userName, datasetId, featureListString, noOfClusters);
            return Response.ok(points).build();
        } catch (DatabaseHandlerException e) {
            logger.error(
                    String.format(
                            "Error occurred while retrieving cluster points of dataset version [id] %s of tenant [id] %s [user] %s ",
                            datasetId, tenantId, userName), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
