/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.project.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.project.mgt.ProjectManagementService;
import org.wso2.carbon.ml.project.mgt.constant.ProjectMgtConstants;
import org.wso2.carbon.ml.project.mgt.dto.Project;
import org.wso2.carbon.ml.project.mgt.dto.Workflow;
import org.wso2.carbon.ml.project.mgt.exceptions.MLEmailNotificationSenderException;
import org.wso2.carbon.ml.project.mgt.exceptions.MLProjectManagementServiceException;
import org.wso2.carbon.ml.project.mgt.internal.ds.MLProjectManagementServiceValueHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class contains services related to project and work-flow management
 */
public class MLProjectManagementService implements ProjectManagementService{
	private static final Log logger = LogFactory.getLog(MLProjectManagementService.class);

	/**
     * Creates a new project.
     *
     * @param projectName      Name of the project.
     * @param description      Description of the project.
     * @throws                 MLProjectManagementServiceException
     */
	@Override
	public String createProject(String projectName, String description, String datasetUri)
            throws MLProjectManagementServiceException {
	    try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String projectId = tenantDomain+"."+projectName;
            dbService.createProject(projectName, description, tenantId, username);
            logger.info("Successfully created a ML project: "+projectId);
            return projectId;
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to create the project: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to create the project: " + e.getMessage(),e);
        }
    }
	
	/**
     * Creates a new project.
     *
     * @param projectName      Name of the project.
     * @param description      Description of the project.
     * @return project id.
     * @throws                 MLProjectManagementServiceException
     */
    @Override
    public String createProject(String projectName, String description)
            throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            String projectId = tenantDomain+"."+projectName;
            dbService.createProject(projectName, description, tenantId, username);
            logger.info("Successfully created a ML project: "+projectId);
            return projectId;
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to create the project: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to create the project: " + e.getMessage(),e);
        }
    }
	
    /**
     * Delete details of a given project.
     *
     * @param projectId    Unique identifier of the project
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public void deleteProject(String projectId) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            dbService.deleteProject(projectId);
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to delete the project: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to delete the project: " + e.getMessage(),e);
        }
    }

    /**
     * Get the project names and created dates, that a tenant is assigned to.
     *
     * @param tenantID     Unique identifier of the tenant
     * @return             An array of project ID, Name and the created date of the projects associated with a given tenant.
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public String[][] getTenantProjects(String tenantID) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            return dbService.getTenantProjects(Integer.parseInt(tenantID));
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to get projects of tenant: " + tenantID + " : " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to get projects of tenant: " + tenantID + " : " +
                    e.getMessage(),e);
        }
    }

    /**
     * Returns the ID of the data-set associated with the project.
     *
     * @param projectId    Unique identifier of the project
     * @return             Unique identifier of the data-set associated with the project
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public String getdatasetID(String projectId) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            return dbService.getdatasetID(projectId);
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to return dataset ID. " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to return dataset ID: " + e.getMessage(),e);
        }
    }

    /**
     * Create a new machine learning workflow.
     *
     * @param workflowID           Unique identifier for the new workflow.
     * @param parentWorkflowID     Unique identifier for the workflow from which the current workflow is inherited from.
     * @param projectID            Unique identifier for the project for which the workflow is created.
     * @param workflowName         Name of the project
     * @throws                     MLProjectManagementServiceException
     */
	@Override
	@Deprecated
    public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID,
                                  String workflowName) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            String datasetID = getdatasetID(projectID);
            dbService.createNewWorkflow(workflowID, parentWorkflowID, projectID, datasetID,
                                        workflowName);
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to create the workflow: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to create the workflow: " + e.getMessage(),e);
        }
    }
	
	/**
     * Create a new machine learning work-flow and set the default settings.
     *
     * @param projectID            Unique identifier for the project for which the work-flow is created.
     * @param workflowName         Name of the work-flow
     * @throws                     MLProjectManagementServiceException
     */
    @Override
    public String createWorkflowAndSetDefaultSettings (String projectID, String workflowName) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            String datasetID = getdatasetID(projectID);
            String workflowID = projectID+"."+workflowName;
            dbService.createWorkflow(workflowID, projectID, datasetID,
                                        workflowName);
            // TODO create model and insert default feature settings
            //dbService.setDefaultFeatureSettings(datasetID, workflowID);
            return workflowID;
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to create the workflow: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to create the workflow: " + e.getMessage(),e);
        }
    }

    /**
     * This method update the workflow name associated with given workflowID
     * 
     * @param workflowID   Unique Identifier of this workflow
     * @param name         Updated name of the workflow
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public void updateWorkflowName(String workflowID, String name)
                                    throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            dbService.updateWorkdflowName(workflowID, name);

        } catch (DatabaseHandlerException e) {
            throw new MLProjectManagementServiceException("An error has occurred while updating workflow: " + workflowID
                + " error message: " + e.getMessage(),e);
        }
    }

    /**
     * Delete an existing workflow.
     *
     * @param workflowID   Unique identifier of the workflow to be deleted
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public void deleteWorkflow(String workflowID) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            dbService.deleteWorkflow(workflowID);
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to delete the workflow: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to delete the workflow: " + e.getMessage(),e);
        }
    }

    /**
     * Get the array of workflows in a project.
     *
     * @param projectId    Unique identifier for the project for which the wokflows are needed
     * @return             An array of workflow ID's and Names
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public String[][] getProjectWorkflows(String projectId)
            throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            return dbService.getProjectWorkflows(projectId);
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to get workflows of the project " + projectId + ": " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to get workflows of the project " + projectId + ": " +
                    e.getMessage(),e);
        }
    }

    /**
     * Set the default values for feature properties, of a given workflow.
     *
     * @param workflowID   Unique Identifier of the new workflow
     * @param datasetID    Unique Identifier of the data-set associated with the workflow
     * @throws             MLProjectManagementServiceException
     */
	@Override
	@Deprecated
    public void setDefaultFeatureSettings(String projectID, String workflowID)
            throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            String datasetID = getdatasetID(projectID);
            // TODO create model and set default settings
            //dbService.setDefaultFeatureSettings(datasetID, workflowID);
            dbService.setDefaultFeatureSettings(1, 1);
        } catch (DatabaseHandlerException e) {
            logger.error("Failed to set default feature settings: " + e.getMessage(), e);
            throw new MLProjectManagementServiceException("Failed to set default feature settings: " + e.getMessage(), e);
        }
    }

	/**
     * Get all the project associated with a tenant.
     * 
     * @param tenantId     Unique identifier of the tenant.
     * @return             List of procets.
     * @throws             MLProjectManagementServiceException
     */
	@Override
    public List<Project> getAllProjects(String tenantId) throws MLProjectManagementServiceException {
        try {
            List<Project> projectsOfThisTenant = new ArrayList<Project>();
            String[][] projects = this.getTenantProjects(tenantId);
            if( projects == null){
                return projectsOfThisTenant;
            }
            for (String[] project : projects) {
                if(project == null){
                    continue;
                }
                String id = project[0];
                String name = project[1];
                Date createdDate = new SimpleDateFormat("yyyy-MM-dd").parse(project[2]);
                String description = project[3];

                List<Workflow> workflowsOfThisProject = new ArrayList<Workflow>();
                String[][] workflows = getProjectWorkflows(id);
                if(workflows != null) {
                    for (String[] workflow : workflows) {
                        if (workflow == null) {
                            continue;
                        }
                        String currentWorkflowId = workflow[0];
                        String currentWorkflowName = workflow[1];

                        Workflow currentWorkflow = new Workflow(currentWorkflowId, currentWorkflowName);
                        workflowsOfThisProject.add(currentWorkflow);
                    }
                }
                Project currentProject = new Project(id, name, description, workflowsOfThisProject, createdDate);
                projectsOfThisTenant.add(currentProject);
            }
            return projectsOfThisTenant;
        } catch (ParseException ex) {
            throw new MLProjectManagementServiceException("An error has occurred while converting project creating date" +
                " of tenant: " + tenantId + " : " + ex.getMessage(), ex);
        } catch (MLProjectManagementServiceException e) {
            throw new MLProjectManagementServiceException("An error has occurred while extracting projects of tenant: "
                    + tenantId +  " : " + e.getMessage(), e);
        }
    }
	
    /**
     * Send email notification indicating model building has been successfully completed.
     *
     * @param emailAddress Email address to sent the mail
     * @param redirectUrl Redirect link to be included in the mail
     * @throws MLProjectManagementServiceException
     */
    @Override
    public void sendModelBuildingCompleteNotification(String emailAddress, String[] emailParameters)
            throws MLProjectManagementServiceException {
        try {
            EmailNotificationSender emailNotificationSender = new EmailNotificationSender();
            emailNotificationSender.send(ProjectMgtConstants.MODEL_BUILDING_COMPLETE_NOTIFICATION, emailAddress,
                    emailParameters);
        } catch (MLEmailNotificationSenderException e) {
            throw new MLProjectManagementServiceException("Failed to send the notification to: " + emailAddress + " : "
                    + e.getMessage(), e);
        }
    }

    /**
     * Send email notification indicating model building has been failed.
     *
     * @param emailAddress Email address to sent the mail
     * @param redirectUrl Redirect link to be included in the mail
     * @throws MLProjectManagementServiceException
     */
    @Override
    public void sendModelBuildingFailedNotification(String emailAddress, String[] emailParameters)
            throws MLProjectManagementServiceException {
        try {
            EmailNotificationSender emailNotificationSender = new EmailNotificationSender();
            emailNotificationSender.send(ProjectMgtConstants.MODEL_BUILDING_FAILED_NOTIFICATION, emailAddress,
                    emailParameters);
        } catch (MLEmailNotificationSenderException e) {
            throw new MLProjectManagementServiceException("Failed to send the notification to: " + emailAddress + " : "
                    + e.getMessage(), e);
        }
    }

    /**
     * Retrieve details of a given project.
     *
     * @param projectId Unique identifier of the project
     * @throws MLProjectManagementServiceException
     */
    @Override
    public String[] getProject(String projectId) throws MLProjectManagementServiceException {
        try {
            DatabaseService dbService = MLProjectManagementServiceValueHolder.getDatabaseService();
            return dbService.getProject(projectId);
        } catch (DatabaseHandlerException e) {
            throw new MLProjectManagementServiceException("Failed to retrieve the project: " + projectId + " : "
                    + e.getMessage(), e);
        }
    }
}
