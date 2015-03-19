/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.database.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ml.commons.constants.MLConstants;
import org.wso2.carbon.ml.commons.domain.*;
import org.wso2.carbon.ml.commons.domain.config.HyperParameter;
import org.wso2.carbon.ml.database.DatabaseService;
import org.wso2.carbon.ml.database.exceptions.DatabaseHandlerException;
import org.wso2.carbon.ml.database.internal.constants.SQLQueries;
import org.wso2.carbon.ml.database.internal.ds.LocalDatabaseCreator;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class MLDatabaseService implements DatabaseService {

    private static final Log logger = LogFactory.getLog(MLDatabaseService.class);
    private MLDataSource dbh;
    private static final String DB_CHECK_SQL = "SELECT * FROM ML_PROJECT";

    public MLDatabaseService() {
        try {
            dbh = new MLDataSource();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        String value = System.getProperty("setup");
        if (value != null) {
            LocalDatabaseCreator databaseCreator = new LocalDatabaseCreator(dbh.getDataSource());
            try {
                if (!databaseCreator.isDatabaseStructureCreated(DB_CHECK_SQL)) {
                    databaseCreator.createRegistryDatabase();
                } else {
                    logger.info("Machine Learner database already exists. Not creating a new database.");
                }
            } catch (Exception e) {
                String msg = "Error in creating the Machine Learner database";
                throw new RuntimeException(msg, e);
            }
        }

    }

    @Override
    public void insertDatasetSchema(MLDataset dataset) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET_SCHEMA);
            insertStatement.setString(1, dataset.getName());
            insertStatement.setInt(2, dataset.getTenantId());
            insertStatement.setString(3, dataset.getUserName());
            insertStatement.setString(4, dataset.getComments());
            insertStatement.setString(5, dataset.getDataSourceType());
            insertStatement.setString(6, dataset.getDataTargetType());
            insertStatement.setString(7, dataset.getDataType());
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of data set");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting details of dataset "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertDatasetVersion(MLDatasetVersion mlVersionSet) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_VALUE_SET);  // todo change query
            insertStatement.setLong(1, mlVersionSet.getDatasetId());
            insertStatement.setString(2, mlVersionSet.getName());
            insertStatement.setString(3, mlVersionSet.getVersion());
            insertStatement.setInt(4, mlVersionSet.getTenantId());
            insertStatement.setString(5, mlVersionSet.getUserName());
            insertStatement.setString(6, mlVersionSet.getTargetPath().getPath());
            insertStatement.setObject(7, mlVersionSet.getSamplePoints());
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the value set");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting value set " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }

    }

    @Override
    public void insertProject(MLProject project) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement createProjectStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            createProjectStatement = connection.prepareStatement(SQLQueries.INSERT_PROJECT);
            createProjectStatement.setString(1, project.getName());
            createProjectStatement.setString(2, project.getDescription());
            createProjectStatement.setLong(3, project.getDatasetId());
            createProjectStatement.setInt(4, project.getTenantId());
            createProjectStatement.setString(5, project.getUserName());
            createProjectStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted details of project: " + project.getName());
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while inserting details of project: " + project.getName()
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, createProjectStatement);
        }
    }

    @Override
    public void insertAnalysis(MLAnalysis analysis) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the analysis to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ANALYSIS);
            insertStatement.setLong(1, analysis.getProjectId());
            insertStatement.setString(2, analysis.getName());
            insertStatement.setInt(3, analysis.getTenantId());
            insertStatement.setString(4, analysis.getUserName());
            insertStatement.setString(5, analysis.getComments());
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the analysis");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting analysis " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertModel(MLModelNew model) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the model to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_MODEL);
            insertStatement.setString(1, model.getName());
            insertStatement.setLong(2, model.getAnalysisId());
            insertStatement.setLong(3, model.getValueSetId());
            insertStatement.setInt(4, model.getTenantId());
            insertStatement.setString(5, model.getUserName());
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the model");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting model " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * Retrieves the path of the value-set having the given ID, from the database.
     *
     * @param datasetVersionId Unique Identifier of the value-set
     * @return Absolute path of a given value-set
     * @throws DatabaseHandlerException
     */
    public String getDatasetVersionUri(long datasetVersionId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getStatement = connection.prepareStatement(SQLQueries.GET_DATASET_VERSION_LOCATION);
            getStatement.setLong(1, datasetVersionId);
            result = getStatement.executeQuery();
            if (result.first()) {
                return result.getNString(1);
            } else {
                logger.error("Invalid value set ID: " + datasetVersionId);
                throw new DatabaseHandlerException("Invalid value set ID: " + datasetVersionId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading the Value set " + datasetVersionId
                    + " from the database: " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    /**
     * @param name
     * @param tenantID
     * @param username
     * @param comments
     * @param sourceType
     * @param targetType
     * @param dataType
     * @throws DatabaseHandlerException
     */
    public void insertDatasetDetails(String name, int tenantID, String username, String comments, String sourceType,
            String targetType, String dataType) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET_SCHEMA);
            insertStatement.setString(1, name);
            insertStatement.setInt(2, tenantID);
            insertStatement.setString(3, username);
            insertStatement.setString(4, comments);
            insertStatement.setString(5, sourceType);
            insertStatement.setString(6, targetType);
            insertStatement.setString(7, dataType);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of data set");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting details of dataset "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public long getDatasetId(String datasetName, int tenantId, String userName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_ID);
            statement.setString(1, datasetName);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting dataset name: " + datasetName
                    + " and tenant id:" + tenantId, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
    
    @Override
    public long getDatasetVersionId(long datasetId, String version, int tenantId, String userName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASETVERSION_ID);
            statement.setLong(1, datasetId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            statement.setString(4, version);
            
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(String.format(" An error has occurred while extracting dataset version id of [dataset] %s [version] %s [tenant] %s [user] %s", datasetId, version, tenantId, userName), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public long getValueSetId(String valueSetName, int tenantId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_VALUESET_ID);
            statement.setString(1, valueSetName);
            statement.setInt(2, tenantId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No value-set id associated with value-set name: " + valueSetName
                        + " and tenant id:" + tenantId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting value-set name: "
                    + valueSetName + " and tenant id:" + tenantId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public long getDatasetVersionIdOfModel(long modelId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.SELECT_DATASET_VERSION_ID_OF_MODEL);
            statement.setLong(1, modelId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No dataset-version id associated with the model id: " + modelId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting dataset-version for model: "
                    + modelId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public void insertDatasetVersionDetails(long datasetId, int tenantId, String username, String version)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set-version details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATASET_VERSION);
            insertStatement.setLong(1, datasetId);
            insertStatement.setInt(2, tenantId);
            insertStatement.setString(3, username);
            insertStatement.setString(4, version);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of data set version");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting details of dataset version "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertFeatureDefaults(long datasetVersionId, String featureName, String type, int featureIndex,
            String summary) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the data-set details to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_DEFAULTS);
            insertStatement.setLong(1, datasetVersionId);
            insertStatement.setString(2, featureName);
            insertStatement.setString(3, type);
            insertStatement.setInt(4, featureIndex);
            insertStatement.setString(5, summary);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the details of feature defaults");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting details of feature defaults "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertValueSet(long datasetVersionId, String name, int tenantId, String username, String uri,
            SamplePoints samplePoints) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_VALUE_SET);
            insertStatement.setLong(1, datasetVersionId);
            insertStatement.setString(2, name);
            insertStatement.setInt(3, tenantId);
            insertStatement.setString(4, username);
            insertStatement.setString(5, uri);
            insertStatement.setObject(6, samplePoints);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the value set");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting value set " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertDataSource(long valuesetId, int tenantId, String username, String key, String value)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_DATA_SOURCE);
            insertStatement.setLong(1, valuesetId);
            insertStatement.setInt(2, tenantId);
            insertStatement.setString(3, username);
            insertStatement.setString(4, key);
            insertStatement.setString(6, value);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the data source");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting data source " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public boolean isDatasetNameExist(int tenantId, String userName, String datasetName)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_USING_NAME);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            statement.setString(3, datasetName);
            result = statement.executeQuery();
            return result.first();
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while searching for dataset of tenant id: "
                    + tenantId + ", user: " + userName + " and dataset name:" + datasetName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public boolean isDatasetVersionExist(int tenantId, String datasetName, String datasetVersion)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_VERSION);
            statement.setString(1, datasetName);
            statement.setInt(2, tenantId);
            statement.setString(3, datasetVersion);
            result = statement.executeQuery();
            return result.first();
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while searching for dataset version of tenant id: " + tenantId
                            + ", dataset name:" + datasetName + " and dataset version:" + datasetVersion);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public long getDatasetVersionId(long datasetId, String datasetVersion) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_VERSION_ID);
            statement.setLong(1, datasetId);
            statement.setString(2, datasetVersion);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No dataset id associated with dataset id: " + datasetId
                        + " and version:" + datasetVersion);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting dataset id: " + datasetId
                    + " and version:" + datasetVersion, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
    
    @Override
    public List<MLDatasetVersion> getValuesetOfVersion(int tenantId, String userName, long versionsetId)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLDatasetVersion> valuesets = new ArrayList<MLDatasetVersion>();

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_VALUESET_OF_VERSION);
            statement.setLong(1, versionsetId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLDatasetVersion valueset = new MLDatasetVersion();
                valueset.setId(result.getLong(1));
                valueset.setName(result.getString(3));
                valueset.setTargetPath(new URI(result.getString(6)));
                valueset.setSamplePoints((SamplePoints)result.getObject(7));
                valueset.setTenantId(tenantId);
                valueset.setUserName(userName);
                valuesets.add(valueset);
            }
            return valuesets;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(String.format(" An error has occurred while extracting all value sets of version: %s , tenant: %s and user: %s ", versionsetId, tenantId, userName), e);
        } catch (URISyntaxException e) {
            throw new DatabaseHandlerException(String.format(" An error has occurred while extracting all value sets of version: %s , tenant: %s and user: %s ", versionsetId, tenantId, userName), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
    
    @Override
    public List<MLDatasetVersion> getValuesetOfDataset(int tenantId, String userName, long datasetId)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLDatasetVersion> valuesets = new ArrayList<MLDatasetVersion>();

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_VALUESET_OF_DATASET);
            statement.setLong(1, datasetId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLDatasetVersion valueset = new MLDatasetVersion();
                valueset.setId(result.getLong(1));
                valueset.setName(result.getString(3));
                valueset.setTargetPath(new URI(result.getString(6)));
                valueset.setSamplePoints((SamplePoints)result.getObject(7));
                valueset.setTenantId(tenantId);
                valueset.setUserName(userName);
                valuesets.add(valueset);
            }
            return valuesets;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting value sets for dataset id: "
                    + datasetId, e);
        } catch (URISyntaxException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting value sets for dataset id: "
                    + datasetId, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
    
    @Override
    public MLDatasetVersion getValueset(int tenantId, String userName, long valuesetId)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_VALUESET);
            statement.setLong(1, valuesetId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.next()) {
                MLDatasetVersion valueset = new MLDatasetVersion();
                valueset.setId(result.getLong(1));
                valueset.setName(result.getString(3));
                valueset.setTargetPath(new URI(result.getString(6)));
                valueset.setSamplePoints((SamplePoints)result.getObject(7));
                valueset.setTenantId(tenantId);
                valueset.setUserName(userName);
                return valueset;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting valueset of valueset id: "
                    + valuesetId, e);
        } catch (URISyntaxException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting valueset of valueset id: "
                    + valuesetId, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
    
    @Override
    public List<MLDatasetVersion> getAllValuesets(int tenantId, String userName)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLDatasetVersion> valuesets = new ArrayList<MLDatasetVersion>();

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ALL_VALUESETS);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLDatasetVersion valueset = new MLDatasetVersion();
                valueset.setId(result.getLong(1));
                valueset.setName(result.getString(3));
                valueset.setTargetPath(new URI(result.getString(6)));
                valueset.setSamplePoints((SamplePoints)result.getObject(7));
                valueset.setTenantId(tenantId);
                valueset.setUserName(userName);
                valuesets.add(valueset);
            }
            return valuesets;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(String.format(" An error has occurred while extracting all value sets of tenant: %s and user: %s ", tenantId, userName), e);
        } catch (URISyntaxException e) {
            throw new DatabaseHandlerException(String.format(" An error has occurred while extracting all value sets of tenant: %s and user: %s ", tenantId, userName), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public List<MLDataset> getVersionsetOfDataset(int tenantId, String userName, long datasetId)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLDataset> versionsets = new ArrayList<MLDataset>();

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_VERSIONSET_OF_DATASET);
            statement.setLong(1, datasetId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLDataset versionset = new MLDataset();
                versionset.setId(result.getLong(1));
                versionset.setName(result.getString(2));
                versionset.setComments(MLDatabaseUtils.toString(result.getClob(3)));
                versionset.setDataSourceType(result.getString(4));
                versionset.setDataTargetType(result.getString(5));
                versionset.setDataType(result.getString(6));
                versionset.setVersion(result.getString(7));
                versionset.setTenantId(tenantId);
                versionset.setUserName(userName);
                versionsets.add(versionset);
            }
            return versionsets;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting version sets for dataset id: "
                    + datasetId, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public MLDataset getVersionset(int tenantId, String userName, long datasetId, long versionsetId)
            throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_VERSIONSET_USING_ID);
            statement.setLong(1, datasetId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            statement.setLong(4, versionsetId);
            result = statement.executeQuery();
            if (result.first()) {
                MLDataset versionset = new MLDataset();
                versionset.setId(versionsetId);
                versionset.setName(result.getString(1));
                versionset.setComments(MLDatabaseUtils.toString(result.getClob(2)));
                versionset.setDataSourceType(result.getString(3));
                versionset.setDataTargetType(result.getString(4));
                versionset.setDataType(result.getString(5));
                versionset.setVersion(result.getString(6));
                versionset.setTenantId(tenantId);
                versionset.setUserName(userName);
                return versionset;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(String.format(
                    "An error occurred while extracting version set for dataset id: %s versionset id: %s ", datasetId,
                    versionsetId), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public List<MLDataset> getAllDatasets(int tenantId, String userName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLDataset> datasets = new ArrayList<MLDataset>();

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ALL_DATASETS);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLDataset dataset = new MLDataset();
                dataset.setId(result.getLong(1));
                dataset.setName(result.getString(2));
                dataset.setComments(MLDatabaseUtils.toString(result.getClob(3)));
                dataset.setDataSourceType(result.getString(4));
                dataset.setDataTargetType(result.getString(5));
                dataset.setDataType(result.getString(6));
                dataset.setTenantId(tenantId);
                dataset.setUserName(userName);
                datasets.add(dataset);
            }
            return datasets;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting datasets of tenant: "
                    + tenantId + " , user: " + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public MLDataset getDataset(int tenantId, String userName, long datasetId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;

        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_USING_ID);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            statement.setLong(3, datasetId);
            result = statement.executeQuery();
            if (result.first()) {
                MLDataset dataset = new MLDataset();
                dataset.setId(result.getLong(1));
                dataset.setName(result.getString(2));
                dataset.setComments(MLDatabaseUtils.toString(result.getClob(3)));
                dataset.setDataSourceType(result.getString(4));
                dataset.setDataTargetType(result.getString(5));
                dataset.setDataType(result.getString(6));
                dataset.setTenantId(tenantId);
                dataset.setUserName(userName);
                return dataset;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting datasets of tenant: "
                    + tenantId + " , user: " + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public long getDatasetVersionId(long valuesetId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_VERSION_ID_FROM_VALUESET);
            statement.setLong(1, valuesetId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No dataset version id associated with valueset id: " + valuesetId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while extracting dataset version id for valueset id: " + valuesetId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public long getDatasetId(long datasetVersionId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_ID_FROM_DATASET_VERSION);
            statement.setLong(1, datasetVersionId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No dataset id is associated with dataset version id: "
                        + datasetVersionId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while extracting dataset id for dataset version id: " + datasetVersionId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public String getDataTypeOfModel(long modelId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATA_TYPE_OF_MODEL);
            statement.setLong(1, modelId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getString(1);
            } else {
                throw new DatabaseHandlerException("No data type is associated with model id: " + modelId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting data type for model id: "
                    + modelId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    /**
     * Update the value-set table with a value-set sample.
     *
     * @param valueSet Unique Identifier of the value-set
     * @param valueSetSample SamplePoints object of the value-set
     * @throws DatabaseHandlerException
     */
    @Override
    public void updateValueSetSample(long valueSet, SamplePoints valueSetSample) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SAMPLE_POINTS);
            updateStatement.setObject(1, valueSetSample);
            updateStatement.setLong(2, valueSet);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the sample of value set " + valueSet);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the sample " + "points of value set "
                    + valueSet + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Update the model summary
     */
    @Override
    public void updateModelSummary(long modelId, ModelSummary modelSummary) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_MODEL_SUMMARY);
            updateStatement.setObject(1, modelSummary);
            updateStatement.setLong(2, modelId);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the model summary of model: " + modelId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the model summary " + "of model "
                    + modelId + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Update the model storage
     */
    @Override
    public void updateModelStorage(long modelId, String storageType, String location) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_MODEL_STORAGE);
            updateStatement.setObject(1, storageType);
            updateStatement.setObject(2, location);
            updateStatement.setLong(3, modelId);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the model storage of model: " + modelId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the model storage " + "of model "
                    + modelId + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Returns data points of the selected sample as coordinates of three features, needed for the scatter plot.
     *
     * @param valueSetId Unique Identifier of the value-set
     * @param xAxisFeature Name of the feature to use as the x-axis
     * @param yAxisFeature Name of the feature to use as the y-axis
     * @param groupByFeature Name of the feature to be grouped by (color code)
     * @return A JSON array of data points
     * @throws DatabaseHandlerException
     */
    public JSONArray getScatterPlotPoints(long valueSetId, String xAxisFeature, String yAxisFeature,
            String groupByFeature) throws DatabaseHandlerException {

        // Get the sample from the database.
        SamplePoints sample = getValueSetSample(valueSetId);

        // Converts the sample to a JSON array.
        List<List<String>> columnData = sample.getSamplePoints();
        Map<String, Integer> dataHeaders = sample.getHeader();
        JSONArray samplePointsArray = new JSONArray();
        int firstFeatureColumn = dataHeaders.get(xAxisFeature);
        int secondFeatureColumn = dataHeaders.get(yAxisFeature);
        int thirdFeatureColumn = dataHeaders.get(groupByFeature);
        for (int row = 0; row < columnData.get(thirdFeatureColumn).size(); row++) {
            if (!columnData.get(firstFeatureColumn).get(row).isEmpty()
                    && !columnData.get(secondFeatureColumn).get(row).isEmpty()
                    && !columnData.get(thirdFeatureColumn).get(row).isEmpty()) {
                JSONArray point = new JSONArray();
                point.put(Double.parseDouble(columnData.get(firstFeatureColumn).get(row)));
                point.put(Double.parseDouble(columnData.get(secondFeatureColumn).get(row)));
                point.put(columnData.get(thirdFeatureColumn).get(row));
                samplePointsArray.put(point);
            }
        }

        return samplePointsArray;
    }

    /**
     * Returns sample data for selected features
     *
     * @param valueSetId Unique Identifier of the value-set
     * @param featureListString String containing feature name list
     * @return A JSON array of data points
     * @throws DatabaseHandlerException
     */
    public JSONArray getChartSamplePoints(long valueSetId, String featureListString) throws DatabaseHandlerException {

        // Get the sample from the database.
        SamplePoints sample = getValueSetSample(valueSetId);

        // Converts the sample to a JSON array.
        List<List<String>> columnData = sample.getSamplePoints();
        Map<String, Integer> dataHeaders = sample.getHeader();
        JSONArray samplePointsArray = new JSONArray();

        // split categoricalFeatureListString String into a String array
        String[] featureList = featureListString.split(",");

        // for each row in a selected categorical feature, iterate through all features
        for (int row = 0; row < columnData.get(dataHeaders.get(featureList[0])).size(); row++) {

            JSONObject point = new JSONObject();
            // for each categorical feature in same row put value into a point(JSONObject)
            // {"Soil_Type1":"0","Soil_Type11":"0","Soil_Type10":"0","Cover_Type":"4"}
            for (int featureCount = 0; featureCount < featureList.length; featureCount++) {
                point.put(featureList[featureCount], columnData.get(dataHeaders.get(featureList[featureCount]))
                        .get(row));
            }
            samplePointsArray.put(point);
        }
        return samplePointsArray;
    }

    /**
     * Retrieve the SamplePoints object for a given value-set.
     *
     * @param valueSetId Unique Identifier of the value-set
     * @return SamplePoints object of the value-set
     * @throws DatabaseHandlerException
     */
    private SamplePoints getValueSetSample(long valueSetId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        ResultSet result = null;
        SamplePoints samplePoints = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            updateStatement = connection.prepareStatement(SQLQueries.GET_SAMPLE_POINTS);
            updateStatement.setLong(1, valueSetId);
            result = updateStatement.executeQuery();
            if (result.first()) {
                samplePoints = (SamplePoints) result.getObject(1);
            }
            return samplePoints;
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while retrieving the sample of " + "value set "
                    + valueSetId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement, result);
        }
    }

    /**
     * Returns a set of features in a given range, from the alphabetically ordered set of features, of a data-set.
     *
     * @param datasetID Unique Identifier of the data-set
     * @param startIndex Starting index of the set of features needed
     * @param numberOfFeatures Number of features needed, from the starting index
     * @return A list of Feature objects
     * @throws DatabaseHandlerException
     */
    public List<FeatureSummary> getFeatures(String datasetID, String modelId, int startIndex, int numberOfFeatures)
            throws DatabaseHandlerException {
        List<FeatureSummary> features = new ArrayList<FeatureSummary>();
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        try {
            // Create a prepared statement and retrieve data-set configurations.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURES);
            getFeatues.setString(1, datasetID);
            getFeatues.setString(2, datasetID);
            getFeatues.setString(3, modelId);
            getFeatues.setInt(4, numberOfFeatures);
            getFeatues.setInt(5, startIndex);
            result = getFeatues.executeQuery();
            while (result.next()) {
                String featureType = FeatureType.NUMERICAL;
                if (FeatureType.CATEGORICAL.toString().equalsIgnoreCase(result.getString(3))) {
                    featureType = FeatureType.CATEGORICAL;
                }
                // Set the impute option
                String imputeOperation = ImputeOption.DISCARD;
                if (ImputeOption.REPLACE_WTH_MEAN.equalsIgnoreCase(result.getString(5))) {
                    imputeOperation = ImputeOption.REPLACE_WTH_MEAN;
                } else if (ImputeOption.REGRESSION_IMPUTATION.equalsIgnoreCase(result.getString(5))) {
                    imputeOperation = ImputeOption.REGRESSION_IMPUTATION;
                }
                String featureName = result.getString(1);
                boolean isImportantFeature = result.getBoolean(4);
                String summaryStat = result.getString(2);
                int index = result.getInt(6);

                features.add(new FeatureSummary(featureName, isImportantFeature, featureType, imputeOperation,
                        summaryStat, index));
            }
            return features;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving features of " + "the data set: "
                    + datasetID + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

    /**
     * This method extracts and returns default features available in a given dataset version
     *
     * @param datasetVersionId The dataset version id
     * @return A list of FeatureSummary
     * @throws DatabaseHandlerException
     */
    @Override
    public List<FeatureSummary> getDefaultFeatures(long datasetVersionId, int startIndex, int numberOfFeatures)
            throws DatabaseHandlerException {
        List<FeatureSummary> features = new ArrayList<FeatureSummary>();
        Connection connection = null;
        PreparedStatement getDefaultFeatues = null;
        ResultSet result = null;

        try {
            connection = dbh.getDataSource().getConnection();
            getDefaultFeatues = connection.prepareStatement(SQLQueries.GET_DEFAULT_FEATURES);

            getDefaultFeatues.setLong(1, datasetVersionId);
            getDefaultFeatues.setInt(2, numberOfFeatures);
            getDefaultFeatues.setInt(3, startIndex);

            result = getDefaultFeatues.executeQuery();

            while (result.next()) {
                String featureType = FeatureType.NUMERICAL;
                if (FeatureType.CATEGORICAL.equalsIgnoreCase(result.getString(3))) {
                    featureType = FeatureType.CATEGORICAL;
                }
                // Set the impute option
                String imputeOperation = ImputeOption.REPLACE_WTH_MEAN;

                String featureName = result.getString(1);

                // Setting up default include flag
                boolean isImportantFeature = true;

                String summaryStat = result.getString(2);
                int index = result.getInt(4);

                features.add(new FeatureSummary(featureName, isImportantFeature, featureType, imputeOperation,
                        summaryStat, index));
            }
            return features;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving features of "
                    + "the data set version: " + datasetVersionId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getDefaultFeatues, result);
        }
    }

    /**
     * Returns the names of the features of the model
     *
     * @param modelId Unique identifier of the current model
     * @return A list of feature names
     * @throws DatabaseHandlerException
     */
    public List<String> getFeatureNames(long modelId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement getFeatureNamesStatement = null;
        ResultSet result = null;
        List<String> featureNames = new ArrayList<String>();
        try {
            connection = dbh.getDataSource().getConnection();

            // Create a prepared statement and retrieve model configurations
            getFeatureNamesStatement = connection.prepareStatement(SQLQueries.GET_FEATURE_NAMES);
            getFeatureNamesStatement.setLong(1, modelId);

            result = getFeatureNamesStatement.executeQuery();
            // Convert the result in to a string array to e returned.
            while (result.next()) {
                featureNames.add(result.getString(1));
            }
            return featureNames;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving feature "
                    + "names of the dataset for model: " + modelId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatureNamesStatement, result);
        }
    }

    /**
     * Retrieve and returns the Summary statistics for a given feature of a given data-set version, from the database.
     *
     * @param datasetVersionId Unique identifier of the data-set version
     * @param featureName Name of the feature of which summary statistics are needed
     * @return JSON string containing the summary statistics
     * @throws DatabaseHandlerException
     */
    public String getSummaryStats(long datasetVersionId, String featureName) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement getSummaryStatement = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getSummaryStatement = connection.prepareStatement(SQLQueries.GET_SUMMARY_STATS);
            getSummaryStatement.setString(1, featureName);
            getSummaryStatement.setLong(2, datasetVersionId);
            result = getSummaryStatement.executeQuery();
            result.first();
            return result.getString(1);
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving summary "
                    + "statistics for the feature \"" + featureName + "\" of the data set version " + datasetVersionId
                    + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getSummaryStatement, result);
        }
    }

    /**
     * Returns the number of features of a given data-set version
     *
     * @param datasetVersionId Unique identifier of the data-set version
     * @return Number of features in the data-set version
     * @throws DatabaseHandlerException
     */
    public int getFeatureCount(long datasetVersionId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        int featureCount = 0;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            // Create a prepared statement and extract data-set configurations.
            getFeatues = connection.prepareStatement(SQLQueries.GET_FEATURE_COUNT);
            getFeatues.setLong(1, datasetVersionId);
            result = getFeatues.executeQuery();
            if (result.first()) {
                featureCount = result.getInt(1);
            }
            return featureCount;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    "An error occurred while retrieving feature count of the dataset version " + datasetVersionId
                            + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

    @Override
    public void createProject(String projectName, String description, int tenantId, String username)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement createProjectStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            createProjectStatement = connection.prepareStatement(SQLQueries.INSERT_PROJECT);
            createProjectStatement.setString(1, projectName);
            createProjectStatement.setString(2, description);
            createProjectStatement.setInt(3, tenantId);
            createProjectStatement.setString(4, username);
            createProjectStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted details of project: " + projectName);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while inserting details of project: " + projectName
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, createProjectStatement);
        }
    }

    @Override
    public long getProjectId(int tenantId, String userName, String projectName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_PROJECT_ID);
            statement.setString(1, projectName);
            statement.setInt(2, tenantId);
            statement.setString(2, userName);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No project id associated with project name: " + projectName
                        + ", tenant Id:" + tenantId + " and username:" + userName);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting project id for project name:"
                    + projectName + ", tenant Id:" + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public MLProject getProject(int tenantId, String userName, String projectName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_PROJECT);
            statement.setString(1, projectName);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                MLProject project = new MLProject();
                project.setName(projectName);
                project.setId(result.getLong(1));
                project.setDescription(result.getString(2));
                project.setTenantId(tenantId);
                project.setUserName(userName);
                project.setCreatedTime(result.getString(3));
                return project;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting project for project name:"
                    + projectName + ", tenant Id:" + tenantId + " and username:" + userName, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public List<MLProject> getAllProjects(int tenantId, String userName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLProject> projects = new ArrayList<MLProject>();
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ALL_PROJECTS);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLProject project = new MLProject();
                project.setName(result.getString(1));
                project.setId(result.getLong(2));
                project.setDescription(result.getString(3));
                project.setTenantId(tenantId);
                project.setUserName(userName);
                project.setCreatedTime(result.getString(4));
                projects.add(project);
            }
            return projects;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting all projects of"
                    + " tenant Id:" + tenantId + " and username:" + userName, e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public void deleteProject(int tenantId, String userName, String projectName) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT);
            preparedStatement.setString(1, projectName);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, userName);
            preparedStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully deleted the project: " + projectName);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while deleting the project: " + projectName + ": "
                    + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, preparedStatement);
        }
    }

    /**
     * Delete details of a given project from the database.
     *
     * @param projectId Unique identifier for the project
     * @throws DatabaseHandlerException
     */
    public void deleteProject(String projectId) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement deleteProjectStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            deleteProjectStatement = connection.prepareStatement(SQLQueries.DELETE_PROJECT_GIVEN_ID);
            deleteProjectStatement.setString(1, projectId);
            deleteProjectStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully deleted the project: " + projectId);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while deleting the project: " + projectId + ": "
                    + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, deleteProjectStatement);
        }
    }

    @Override
    public void insertAnalysis(long projectId, String name, int tenantId, String username, String comments)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the analysis to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_ANALYSIS);
            insertStatement.setLong(1, projectId);
            insertStatement.setString(2, name);
            insertStatement.setInt(3, tenantId);
            insertStatement.setString(4, username);
            insertStatement.setString(5, comments);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the analysis");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting analysis " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public long getAnalysisId(int tenantId, String userName, String analysisName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ANALYSIS_ID);
            statement.setString(1, analysisName);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No analysis id associated with analysis name: " + analysisName
                        + ", tenant id:" + tenantId + " and username:" + userName);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while extracting analysis id for analysis name: " + analysisName
                            + ", tenant id:" + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public MLAnalysis getAnalysis(int tenantId, String userName, String analysisName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ANALYSIS);
            statement.setString(1, analysisName);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                MLAnalysis analysis = new MLAnalysis();
                analysis.setId(result.getLong(1));
                analysis.setProjectId(result.getLong(2));
                analysis.setComments(MLDatabaseUtils.toString(result.getClob(3)));
                analysis.setName(analysisName);
                analysis.setTenantId(tenantId);
                analysis.setUserName(userName);
                return analysis;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting analysis for analysis name: "
                    + analysisName + ", tenant id:" + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public List<MLAnalysis> getAllAnalyses(int tenantId, String userName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLAnalysis> analyses = new ArrayList<MLAnalysis>();
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ALL_ANALYSES);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLAnalysis analysis = new MLAnalysis();
                analysis.setId(result.getLong(1));
                analysis.setProjectId(result.getLong(2));
                analysis.setComments(MLDatabaseUtils.toString(result.getClob(3)));
                analysis.setName(result.getString(4));
                analysis.setTenantId(tenantId);
                analysis.setUserName(userName);
                analyses.add(analysis);
            }
            return analyses;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting analyses for tenant id:"
                    + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public long getModelId(int tenantId, String userName, String modelName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ML_MODEL_ID);
            statement.setString(1, modelName);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                throw new DatabaseHandlerException("No model id associated with model name: " + modelName
                        + ", tenant id:" + tenantId + " and username:" + userName);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting model id for model name: "
                    + modelName + ", tenant id:" + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public MLModelNew getModel(int tenantId, String userName, String modelName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ML_MODEL);
            statement.setString(1, modelName);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                MLModelNew model = new MLModelNew();
                model.setId(result.getLong(1));
                model.setAnalysisId(result.getLong(2));
                model.setValueSetId(result.getLong(3));
                model.setCreatedTime(result.getString(4));
                model.setStorageType(result.getString(5));
                model.setStorageDirectory(result.getString(6));
                model.setName(modelName);
                model.setTenantId(tenantId);
                model.setUserName(userName);
                return model;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting the model with model name: "
                    + modelName + ", tenant id:" + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public List<MLModelNew> getAllModels(int tenantId, String userName) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        List<MLModelNew> models = new ArrayList<MLModelNew>();
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ALL_ML_MODELS);
            statement.setInt(1, tenantId);
            statement.setString(2, userName);
            result = statement.executeQuery();
            while (result.next()) {
                MLModelNew model = new MLModelNew();
                model.setId(result.getLong(1));
                model.setAnalysisId(result.getLong(2));
                model.setValueSetId(result.getLong(3));
                model.setCreatedTime(result.getString(4));
                model.setStorageType(result.getString(5));
                model.setStorageDirectory(result.getString(6));
                model.setName(result.getString(7));
                model.setTenantId(tenantId);
                model.setUserName(userName);
                models.add(model);
            }
            return models;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting all the models of tenant id:"
                    + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public MLStorage getModelStorage(long modelId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        MLStorage storage = new MLStorage();
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_MODEL_STORAGE);
            statement.setLong(1, modelId);
            result = statement.executeQuery();
            if (result.first()) {
                storage.setType(result.getString(1));
                storage.setLocation(result.getString(2));
                return storage;
            } else {
                return storage;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting model storage for model id: "
                    + modelId);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public boolean isValidModelId(int tenantId, String userName, long modelId) throws DatabaseHandlerException {

        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_ML_MODEL_NAME);
            statement.setLong(1, modelId);
            statement.setInt(2, tenantId);
            statement.setString(3, userName);
            result = statement.executeQuery();
            if (result.first()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(" An error has occurred while extracting model name for model id: "
                    + modelId + ", tenant id:" + tenantId + " and username:" + userName);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

    @Override
    public void deleteAnalysis(int tenantId, String userName, String analysisName) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(SQLQueries.DELETE_ANALYSIS);
            preparedStatement.setString(1, analysisName);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, userName);
            preparedStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully deleted the analysis: " + analysisName);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while deleting the analysis: " + analysisName + ": "
                    + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, preparedStatement);
        }
    }

    @Override
    public void insertModel(String name, long analysisId, long datasetVersionId, int tenantId, String username)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the model to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_MODEL);
            insertStatement.setString(1, name);
            insertStatement.setLong(2, analysisId);
            insertStatement.setLong(3, datasetVersionId);
            insertStatement.setInt(4, tenantId);
            insertStatement.setString(5, username);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the model");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting model " + " to the database: "
                    + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertModelConfiguration(long modelId, String key, String value)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the model configuration to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_MODEL_CONFIGURATION);
            insertStatement.setLong(1, modelId);
            insertStatement.setString(2, key);
            insertStatement.setString(3, value);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the model configuration");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting model configuration "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertModelConfigurations(long analysisId, List<MLModelConfiguration> modelConfigs)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the model configuration to the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);

            for (MLModelConfiguration mlModelConfiguration : modelConfigs) {
                String key = mlModelConfiguration.getKey();
                String value = mlModelConfiguration.getValue();

                insertStatement = connection.prepareStatement(SQLQueries.INSERT_MODEL_CONFIGURATION);
                insertStatement.setLong(1, analysisId);
                insertStatement.setString(2, key);
                insertStatement.setString(3, value);
                insertStatement.execute();
            }
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the model configuration");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting model configuration "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * retrieve a model configuration
     *
     * @param modelId Unique identifier of the current model
     * @param name config name
     * @return config value
     * @throws DatabaseHandlerException
     */
    @Override
    public String getModelConfigurationValue(long modelId, String name) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement getModelConfigurationValueStatement = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();

            // Create a prepared statement and retrieve model configurations
            getModelConfigurationValueStatement = connection.prepareStatement(SQLQueries.GET_A_MODEL_CONFIGURATION);
            getModelConfigurationValueStatement.setLong(1, modelId);
            getModelConfigurationValueStatement.setString(2, name);

            result = getModelConfigurationValueStatement.executeQuery();
            // Convert the result in to a string array to e returned.
            while (result.next()) {
                return result.getString(1);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving a model "
                    + " configuration for the model: " + modelId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getModelConfigurationValueStatement, result);
        }
    }

    @Override
    public void insertHyperParameters(long analysisId, List<MLHyperParameter> hyperParameters)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the hyper parameter to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);

            for (MLHyperParameter mlHyperParameter : hyperParameters) {
                String name = mlHyperParameter.getKey();
                String value = mlHyperParameter.getValue();

                insertStatement = connection.prepareStatement(SQLQueries.INSERT_HYPER_PARAMETER);
                insertStatement.setLong(1, analysisId);
                insertStatement.setString(2, name);
                insertStatement.setString(3, value);
                insertStatement.execute();
            }

            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the hyper parameter");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting hyper parameter "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public List<MLHyperParameter> getHyperParametersOfModel(long modelId) throws DatabaseHandlerException {
        List<MLHyperParameter> hyperParams = new ArrayList<MLHyperParameter>();
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        try {
            // Create a prepared statement and retrieve data-set configurations.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getFeatues = connection.prepareStatement(SQLQueries.GET_HYPER_PARAMETERS_OF_MODEL);
            getFeatues.setLong(1, modelId);
            result = getFeatues.executeQuery();
            while (result.next()) {
                MLHyperParameter param = new MLHyperParameter();
                param.setKey(result.getString(1));
                param.setValue(result.getString(2));
                hyperParams.add(param);
            }
            return hyperParams;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving hyper parameters of "
                    + "the model: " + modelId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

    @Override
    public Map<String, String> getHyperParametersOfModelAsMap(long modelId) throws DatabaseHandlerException {
        Map<String, String> hyperParams = new HashMap<String, String>();
        Connection connection = null;
        PreparedStatement getFeatues = null;
        ResultSet result = null;
        try {
            // Create a prepared statement and retrieve data-set configurations.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getFeatues = connection.prepareStatement(SQLQueries.GET_HYPER_PARAMETERS_OF_MODEL);
            getFeatues.setLong(1, modelId);
            result = getFeatues.executeQuery();
            while (result.next()) {
                hyperParams.put(result.getString(1), result.getString(2));
            }
            return hyperParams;
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while retrieving hyper parameters of "
                    + "the model: " + modelId + ": " + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getFeatues, result);
        }
    }

    @Override
    public void insertFeatureCustomized(long modelId, int tenantId, String featureName, String type,
            String imputeOption, boolean inclusion, String lastModifiedUser) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the feature-customized to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_CUSTOMIZED);
            insertStatement.setLong(1, modelId);
            insertStatement.setInt(2, tenantId);
            insertStatement.setString(3, featureName);
            insertStatement.setString(4, type);
            insertStatement.setString(5, imputeOption);
            insertStatement.setBoolean(6, inclusion);
            insertStatement.setString(7, lastModifiedUser);
            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the feature-customized");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting feature-customized "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }
    
    @Override
    public long getDatasetSchemaIdFromAnalysisId(long analysisId) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(SQLQueries.GET_DATASET_SCHEMA_ID_FROM_MODEL);
            statement.setLong(1, analysisId);
            result = statement.executeQuery();
            if (result.first()) {
                return result.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException(String.format(" An error has occurred while extracting dataset id of [analysis] %s ", analysisId), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }
    
    @Override
    public void insertDefaultsIntoFeatureCustomized(long analysisId, MLCustomizedFeature customizedValues) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement insertStatement = null;
        
        long datasetSchemaId = getDatasetSchemaIdFromAnalysisId(analysisId);
        try {
            // Insert the feature-customized to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);

                int tenantId = customizedValues.getTenantId();
                String imputeOption = customizedValues.getImputeOption();
                boolean inclusion = customizedValues.isInclude();
                String lastModifiedUser = customizedValues.getLastModifiedUser();
                String userName = customizedValues.getUserName();

                insertStatement = connection.prepareStatement(SQLQueries.INSERT_DEFAULTS_INTO_FEATURE_CUSTOMIZED);
                insertStatement.setLong(1, analysisId);
                insertStatement.setInt(2, tenantId);
                insertStatement.setString(3, imputeOption);
                insertStatement.setBoolean(4, inclusion);
                insertStatement.setString(5, lastModifiedUser);
                insertStatement.setString(6, userName);
                insertStatement.setLong(7, datasetSchemaId);

                insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the feature-customized");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting feature-customized "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertFeatureCustomized(long analysisId, List<MLCustomizedFeature> customizedFeatures)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the feature-customized to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);

            for (MLCustomizedFeature mlCustomizedFeature : customizedFeatures) {
                int tenantId = mlCustomizedFeature.getTenantId();
                String featureName = mlCustomizedFeature.getName();
                String type = mlCustomizedFeature.getType();
                String imputeOption = mlCustomizedFeature.getImputeOption();
                boolean inclusion = mlCustomizedFeature.isInclude();
                String lastModifiedUser = mlCustomizedFeature.getLastModifiedUser();
                String userName = mlCustomizedFeature.getUserName();

                insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_CUSTOMIZED);
                insertStatement.setLong(1, analysisId);
                insertStatement.setInt(2, tenantId);
                insertStatement.setString(3, featureName);
                insertStatement.setString(4, type);
                insertStatement.setString(5, imputeOption);
                insertStatement.setBoolean(6, inclusion);
                insertStatement.setString(7, lastModifiedUser);
                insertStatement.setString(8, userName);

                insertStatement.execute();
            }
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the feature-customized");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting feature-customized "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    @Override
    public void insertFeatureCustomized(long analysisId, MLCustomizedFeature customizedFeature)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            // Insert the feature-customized to the database
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);

            int tenantId = customizedFeature.getTenantId();
            String featureName = customizedFeature.getName();
            String type = customizedFeature.getType();
            String imputeOption = customizedFeature.getImputeOption();
            boolean inclusion = customizedFeature.isInclude();
            String lastModifiedUser = customizedFeature.getLastModifiedUser();
            String userName = customizedFeature.getUserName();

            insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_CUSTOMIZED);
            insertStatement.setLong(1, analysisId);
            insertStatement.setInt(2, tenantId);
            insertStatement.setString(3, featureName);
            insertStatement.setString(4, type);
            insertStatement.setString(5, imputeOption);
            insertStatement.setBoolean(6, inclusion);
            insertStatement.setString(7, lastModifiedUser);
            insertStatement.setString(8, userName);

            insertStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted the feature-customized");
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while inserting feature-customized "
                    + " to the database: " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement);
        }
    }

    /**
     * Assign a tenant to a given project.
     *
     * @param tenantID Unique identifier for the current tenant.
     * @param projectID Unique identifier for the project.
     * @throws DatabaseHandlerException
     */
    public void addTenantToProject(int tenantID, String projectID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement addTenantStatement = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            addTenantStatement = connection.prepareStatement(SQLQueries.ADD_TENANT_TO_PROJECT);
            addTenantStatement.setInt(1, tenantID);
            addTenantStatement.setString(2, projectID);
            addTenantStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully added the tenant: " + tenantID + " to the project: " + projectID);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while adding the tenant " + tenantID
                    + " to the project " + projectID + ": " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, addTenantStatement);
        }
    }

    /**
     * Get the project names and created dates, that a tenant is assigned to.
     *
     * @param tenantID Unique identifier for the tenant.
     * @return An array of project ID, Name and the created date of the projects associated with a given tenant.
     * @throws DatabaseHandlerException
     */
    public String[][] getTenantProjects(int tenantID) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement getTenantProjectsStatement = null;
        ResultSet result = null;
        String[][] projects = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            getTenantProjectsStatement = connection.prepareStatement(SQLQueries.GET_TENANT_PROJECTS,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            getTenantProjectsStatement.setInt(1, tenantID);
            result = getTenantProjectsStatement.executeQuery();
            // create a 2-d string array having the size of the result set
            result.last();
            int size = result.getRow();
            if (size > 0) {
                projects = new String[size][4];
                result.beforeFirst();
                // put the result set to the string array
                for (int i = 0; i < size; i++) {
                    result.next();
                    projects[i][0] = result.getObject(1).toString();
                    projects[i][1] = result.getString(2);
                    projects[i][2] = result.getDate(3).toString();
                    projects[i][3] = result.getString(4);
                }
            }
            return projects;
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while retrieving the projects of user " + tenantID
                    + ": " + e.getMessage(), e);
        } finally {
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, getTenantProjectsStatement, result);
        }
    }

    /**
     * Retrieve Details of a Project (Name, Description, Tenant-id, Username, Created-time)
     *
     * @param projectId Unique identifier of the project
     * @return DatabaseHandlerException
     */
    @Override
    public String[] getProject(String projectId) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement deleteProjectStatement = null;
        ResultSet result = null;
        String ProjectDetails[] = new String[3];
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            deleteProjectStatement = connection.prepareStatement(SQLQueries.GET_PROJECT);
            deleteProjectStatement.setString(1, projectId);
            result = deleteProjectStatement.executeQuery();
            if (result.first()) {
                ProjectDetails[0] = result.getString(1);
                ProjectDetails[1] = result.getString(2);
                ProjectDetails[2] = result.getString(3);
                ProjectDetails[3] = result.getString(4);
                ProjectDetails[4] = result.getString(5);
                return ProjectDetails;
            } else {
                throw new DatabaseHandlerException("Invalid project Id: " + projectId);
            }
        } catch (SQLException e) {
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("Error occurred while retrieving the project: " + projectId + ": "
                    + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, deleteProjectStatement, result);
        }
    }

    /**
     * Update the database with all the summary statistics of the sample.
     *
     * @param datasetVersionId Unique Identifier of the data-set
     * @param headerMap Array of names of features
     * @param type Array of data-types of each feature
     * @param graphFrequencies List of Maps containing frequencies for graphs, of each feature
     * @param missing Array of Number of missing values in each feature
     * @param unique Array of Number of unique values in each feature
     * @param descriptiveStats Array of descriptiveStats object of each feature
     * @param include Default value to set for the flag indicating the feature is an input or not
     * @throws DatabaseHandlerException
     */
    public void updateSummaryStatistics(long datasetVersionId, Map<String, Integer> headerMap, String[] type,
            List<SortedMap<?, Integer>> graphFrequencies, int[] missing, int[] unique,
            List<DescriptiveStatistics> descriptiveStats, Boolean include) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            JSONArray summaryStat;
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            int columnIndex;
            for (Map.Entry<String, Integer> columnNameMapping : headerMap.entrySet()) {
                columnIndex = columnNameMapping.getValue();
                // Get the JSON representation of the column summary.
                summaryStat = createJson(type[columnIndex], graphFrequencies.get(columnIndex), missing[columnIndex],
                        unique[columnIndex], descriptiveStats.get(columnIndex));
                // Put the values to the database table. If the feature already exists, updates
                // the row. If not, inserts as a new row.
                // updateStatement = connection.prepareStatement(SQLQueries.UPDATE_SUMMARY_STATS);
                updateStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_DEFAULTS);
                updateStatement.setLong(1, datasetVersionId);
                updateStatement.setString(2, columnNameMapping.getKey());
                updateStatement.setString(3, type[columnIndex].toString());
                updateStatement.setInt(4, columnIndex);
                updateStatement.setString(5, summaryStat.toString());
                updateStatement.execute();
            }
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the summary statistics for dataset version " + datasetVersionId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the database "
                    + "with summary statistics of the dataset " + datasetVersionId + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    @Override
    public void updateSummaryStatistics(long datasetSchemaId, long datasetVersionId, SummaryStats summaryStats)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        ResultSet result;
        try {
            JSONArray summaryStatJson;
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            int columnIndex;
            for (Map.Entry<String, Integer> columnNameMapping : summaryStats.getHeaderMap().entrySet()) {
                columnIndex = columnNameMapping.getValue();
                // Get the JSON representation of the column summary.
                summaryStatJson = createJson(summaryStats.getType()[columnIndex], summaryStats.getGraphFrequencies()
                        .get(columnIndex), summaryStats.getMissing()[columnIndex],
                        summaryStats.getUnique()[columnIndex], summaryStats.getDescriptiveStats().get(columnIndex));

                updateStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_DEFAULTS);
                updateStatement.setLong(1, datasetSchemaId);
                updateStatement.setString(2, columnNameMapping.getKey());
                updateStatement.setString(3, summaryStats.getType()[columnIndex]);
                updateStatement.setInt(4, columnIndex);
                updateStatement.execute();

                // Get feature id
                updateStatement = connection.prepareStatement(SQLQueries.GET_FEATURE_ID);
                updateStatement.setLong(1, datasetSchemaId);
                updateStatement.setString(2, columnNameMapping.getKey());
                result = updateStatement.executeQuery();
                long featureId = -1;
                if(result.first()) {
                   featureId  = result.getLong(1);
                }

                updateStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_SUMMARY);
                updateStatement.setLong(1, featureId);
                updateStatement.setString(2, columnNameMapping.getKey());
                updateStatement.setLong(3, datasetVersionId);
                updateStatement.setString(4, summaryStatJson.toString());
                updateStatement.execute();
            }
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the summary statistics for dataset version " + datasetVersionId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the database "
                    + "with summary statistics of the dataset " + datasetVersionId + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Create the JSON string with summary statistics for a column.
     *
     * @param type Data-type of the column
     * @param graphFrequencies Bin frequencies of the column
     * @param missing Number of missing values in the column
     * @param unique Number of unique values in the column
     * @param descriptiveStats DescriptiveStats object of the column
     * @return JSON representation of the summary statistics of the column
     */
    private JSONArray createJson(String type, SortedMap<?, Integer> graphFrequencies, int missing, int unique,
            DescriptiveStatistics descriptiveStats) {

        JSONObject json = new JSONObject();
        JSONArray freqs = new JSONArray();
        Object[] categoryNames = graphFrequencies.keySet().toArray();
        // Create an array with intervals/categories and their frequencies.
        for (int i = 0; i < graphFrequencies.size(); i++) {
            JSONArray temp = new JSONArray();
            temp.put(categoryNames[i].toString());
            temp.put(graphFrequencies.get(categoryNames[i]));
            freqs.put(temp);
        }
        // Put the statistics to a json object
        json.put("unique", unique);
        json.put("missing", missing);

        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        if (descriptiveStats.getN() != 0) {
            json.put("mean", decimalFormat.format(descriptiveStats.getMean()));
            json.put("median", decimalFormat.format(descriptiveStats.getPercentile(50)));
            json.put("std", decimalFormat.format(descriptiveStats.getStandardDeviation()));
            if (type.equalsIgnoreCase(FeatureType.NUMERICAL)) {
                json.put("skewness", decimalFormat.format(descriptiveStats.getSkewness()));
            }
        }
        json.put("values", freqs);
        json.put("bar", true);
        json.put("key", "Frequency");
        JSONArray summaryStatArray = new JSONArray();
        summaryStatArray.put(json);
        return summaryStatArray;
    }

    /**
     * Set the default values for feature properties of a given model
     *
     * @param datasetVersionId Unique identifier of the data-set-version
     * @param modelId Unique identifier of the current model
     * @throws DatabaseHandlerException
     */
    public void setDefaultFeatureSettings(long datasetVersionId, long modelId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        PreparedStatement getDefaultFeatureSettings = null;
        ResultSet result = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(true);
            // read default feature settings from feature_defaults table
            getDefaultFeatureSettings = connection.prepareStatement(SQLQueries.GET_DEFAULT_FEATURE_SETTINGS);
            getDefaultFeatureSettings.setLong(1, datasetVersionId);
            result = getDefaultFeatureSettings.executeQuery();
            // insert default feature settings into feature_customized table
            connection.setAutoCommit(false);
            while (result.next()) {
                insertStatement = connection.prepareStatement(SQLQueries.INSERT_FEATURE_CUSTOMIZED);
                insertStatement.setLong(1, modelId);
                insertStatement.setString(2, result.getString(1));
                insertStatement.setString(3, result.getString(2));
                insertStatement.setString(4, result.getString(4));
                insertStatement.setString(5, ImputeOption.REPLACE_WTH_MEAN);
                insertStatement.setBoolean(6, true); // TODO inclusion
                insertStatement.setString(7, ""); // TODO last modified user
                insertStatement.setDate(8, null); // TODO last modified time
                insertStatement.execute();
                connection.commit();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully inserted feature defaults of dataset version: " + datasetVersionId
                        + " of the workflow: " + datasetVersionId);
            }
        } catch (SQLException e) {
            // rollback the changes
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while setting details of dataset version "
                    + datasetVersionId + " of the model " + modelId + " to the database:" + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement, result);
            MLDatabaseUtils.closeDatabaseResources(getDefaultFeatureSettings);
        }
    }

    /**
     * Retrieves the type of a feature.
     *
     * @param modelId Unique identifier of the model
     * @param featureName Name of the feature
     * @return Type of the feature (Categorical/Numerical)
     * @throws DatabaseHandlerException
     */
    @Override
    public String getFeatureType(long modelId, String featureName) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement insertStatement = null;
        PreparedStatement getDefaultFeatureSettings = null;
        ResultSet result = null;
        try {
            MLDataSource dbh = new MLDataSource();
            connection = dbh.getDataSource().getConnection();
            getDefaultFeatureSettings = connection.prepareStatement(SQLQueries.GET_FEATURE_TYPE);
            getDefaultFeatureSettings.setLong(1, modelId);
            getDefaultFeatureSettings.setString(2, featureName);
            result = getDefaultFeatureSettings.executeQuery();
            if (result.first()) {
                return result.getString(1);
            } else {
                throw new DatabaseHandlerException("Invalid model Id: " + modelId);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading type of feature: " + featureName
                    + " of model Id: " + modelId + e.getMessage(), e);
        } finally {
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, insertStatement, result);
        }
    }

    /**
     * Change whether a feature should be included as an input or not.
     *
     * @param featureName Name of the feature to be updated
     * @param modelId Unique identifier of the current model
     * @param isInput Boolean value indicating whether the feature is an input or not
     * @throws DatabaseHandlerException
     */
    public void updateFeatureInclusion(String featureName, long modelId, boolean isInput)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_FEATURE_INCLUSION);
            updateStatement.setBoolean(1, isInput);
            updateStatement.setString(2, featureName);
            updateStatement.setLong(3, modelId);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the include-option of feature" + featureName + "of model " + modelId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException(
                    "An error occurred while updating the feature included option of feature \"" + featureName
                            + "\" of model " + modelId + ": " + e, e);
        } finally {
            // Enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Update the impute method option of a given feature.
     *
     * @param featureName Name of the feature to be updated
     * @param modelId Unique identifier of the current model
     * @param imputeOption Updated impute option of the feature
     * @throws DatabaseHandlerException
     */
    public void updateImputeOption(String featureName, long modelId, String imputeOption)
            throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            // Update the database.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_IMPUTE_METHOD);
            updateStatement.setString(1, imputeOption);
            updateStatement.setString(2, featureName);
            updateStatement.setLong(3, modelId);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the impute-option of feature" + featureName + " of model " + modelId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the feature \"" + featureName
                    + "\" of model " + modelId + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    /**
     * Update the data type of a given feature.
     *
     * @param featureName Name of the feature to be updated
     * @param modelId Unique identifier of the current model
     * @param featureType Updated type of the feature
     * @throws DatabaseHandlerException
     */
    public void updateDataType(String featureName, long modelId, String featureType) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement updateStatement = null;
        try {
            // Update the database with data type.
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(SQLQueries.UPDATE_DATA_TYPE);
            updateStatement.setString(1, featureType);
            updateStatement.setString(2, featureName);
            updateStatement.setLong(3, modelId);
            updateStatement.execute();
            connection.commit();
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully updated the data-type of feature" + featureName + " of model " + modelId);
            }
        } catch (SQLException e) {
            // Roll-back the changes.
            MLDatabaseUtils.rollBack(connection);
            throw new DatabaseHandlerException("An error occurred while updating the data type of feature \""
                    + featureName + "\" of model " + modelId + ": " + e.getMessage(), e);
        } finally {
            // Enable auto commit.
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, updateStatement);
        }
    }

    // TODO
    @Override
    public void createNewWorkflow(String workflowID, String parentWorkflowID, String projectID, String datasetID,
            String workflowName) throws DatabaseHandlerException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO
    @Override
    public void createWorkflow(String workflowID, String projectID, String datasetID, String workflowName)
            throws DatabaseHandlerException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO
    @Override
    public void deleteWorkflow(String workflowID) throws DatabaseHandlerException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO
    @Override
    public String[][] getProjectWorkflows(String projectId) throws DatabaseHandlerException {
        return new String[0][]; // To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO
    @Override
    public void updateWorkdflowName(String workflowId, String name) throws DatabaseHandlerException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO
    @Override
    public String getdatasetID(String projectId) throws DatabaseHandlerException {
        return "1234"; // To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO
    @Override
    public String getDatasetId(String projectId) throws DatabaseHandlerException {
        return "1234";
    }

    // TODO
    public String getModelId(String workflowId) throws DatabaseHandlerException {

        Connection connection = null;
        PreparedStatement model = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();
            model = connection.prepareStatement(SQLQueries.GET_MODEL_ID);
            model.setString(1, workflowId);
            result = model.executeQuery();
            if (!result.first()) {
                // need to query ML_MODEL table, just before model building process is started
                // to overcome building same model two (or more) times.
                // hence, null will be checked in UI.
                return null;
            }
            return result.getString(1);
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred white retrieving model associated with workflow id "
                    + workflowId + ":" + e.getMessage(), e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, model, result);
        }
    }

    @Override
    public Workflow getWorkflow(long modelId) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            Workflow mlWorkflow = new Workflow();
            mlWorkflow.setWorkflowID(modelId);
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            // getStatement = connection.prepareStatement(SQLQueries.GET_WORKFLOW_DATASET_LOCATION);
            // getStatement.setString(1, workflowID);
            // result = getStatement.executeQuery();
            // if (result.first()) {
            // mlWorkflow.setDatasetURL(result.getString(1));
            // }
            List<Feature> mlFeatures = new ArrayList<Feature>();
            getStatement = connection.prepareStatement(SQLQueries.GET_CUSTOMIZED_FEATURES);
            getStatement.setLong(1, modelId);
            result = getStatement.executeQuery();
            while (result.next()) {
                // check whether to include the feature or not
                if (result.getBoolean(5) == true) {
                    Feature mlFeature = new Feature();
                    mlFeature.setName(result.getString(1));
                    mlFeature.setIndex(result.getInt(2));
                    mlFeature.setType(result.getString(3));
                    mlFeature.setImputeOption(result.getString(4));
                    mlFeature.setInclude(result.getBoolean(5));
                    mlFeatures.add(mlFeature);
                }
            }
            mlWorkflow.setFeatures(mlFeatures);
            // set model configs
            mlWorkflow.setAlgorithmName(getAStringModelConfiguration(modelId, MLConstants.ALGORITHM_NAME));
            mlWorkflow.setAlgorithmClass(getAStringModelConfiguration(modelId, MLConstants.ALGORITHM_TYPE));
            mlWorkflow.setResponseVariable(getAStringModelConfiguration(modelId, MLConstants.RESPONSE));
            mlWorkflow.setTrainDataFraction(Double.valueOf(getAStringModelConfiguration(modelId, MLConstants.TRAIN_DATA_FRACTION)));

            // set hyper parameters
            mlWorkflow.setHyperParameters(getHyperParametersOfModelAsMap(modelId));
            // result = getStatement.executeQuery();
            // if (result.first()) {
            // mlWorkflow.setAlgorithmClass(result.getString(1));
            // mlWorkflow.setAlgorithmName(result.getString(2));
            // mlWorkflow.setResponseVariable(result.getString(3));
            // mlWorkflow.setTrainDataFraction(result.getDouble(4));
            // List<HyperParameter> hyperParameters = (List<HyperParameter>) result.getObject(5);
            // mlWorkflow.setHyperParameters(MLDatabaseUtils.getHyperParamsAsAMap(hyperParameters));
            // }
            return mlWorkflow;
        } catch (SQLException e) {
            throw new DatabaseHandlerException(e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    @Override
    public String getAStringModelConfiguration(long analysisId, String configKey) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement model = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();
            model = connection.prepareStatement(SQLQueries.GET_A_MODEL_CONFIGURATION);
            model.setLong(1, analysisId);
            model.setString(2, configKey);
            result = model.executeQuery();
            if (result.first()) {
                return result.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            String msg = String.format(
                    "An error occurred white retrieving [model config] %s  associated with [model id] %s : %s",
                    configKey, analysisId, e.getMessage());
            throw new DatabaseHandlerException(msg, e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, model, result);
        }
    }

    @Override
    public double getADoubleModelConfiguration(long modelId, String configKey) throws DatabaseHandlerException {
        Connection connection = null;
        PreparedStatement model = null;
        ResultSet result = null;
        try {
            connection = dbh.getDataSource().getConnection();
            model = connection.prepareStatement(SQLQueries.GET_A_MODEL_CONFIGURATION);
            model.setLong(1, modelId);
            model.setString(2, configKey);
            result = model.executeQuery();
            if (result.first()) {
                return result.getDouble(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            String msg = String.format(
                    "An error occurred white retrieving [model config] %s  associated with [model id] %s : %s",
                    configKey, modelId, e.getMessage());
            throw new DatabaseHandlerException(msg, e);
        } finally {
            // Close the database resources
            MLDatabaseUtils.closeDatabaseResources(connection, model, result);
        }
    }

    // TODO
    public ModelSummary getModelSummary(String modelID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_MODEL_SUMMARY);
            getStatement.setString(1, modelID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return (ModelSummary) result.getObject(1);
            } else {
                throw new DatabaseHandlerException("Invalid model ID: " + modelID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading model summary for " + modelID
                    + " from the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    // TODO
    public void insertModel(String modelID, String workflowID, Time executionStartTime) throws DatabaseHandlerException {

    }

    // TODO
    public void updateModel(String modelID, MLModel model, ModelSummary modelSummary, Time executionEndTime)
            throws DatabaseHandlerException {

    }

    // TODO
    public MLModel getModel(String modelID) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement getStatement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            connection.setAutoCommit(false);
            getStatement = connection.prepareStatement(SQLQueries.GET_MODEL);
            getStatement.setString(1, modelID);
            result = getStatement.executeQuery();
            if (result.first()) {
                return (MLModel) result.getObject(1);
            } else {
                throw new DatabaseHandlerException("Invalid model ID: " + modelID);
            }
        } catch (SQLException e) {
            throw new DatabaseHandlerException("An error occurred while reading model for " + modelID
                    + " from the database: " + e.getMessage(), e);
        } finally {
            // enable auto commit
            MLDatabaseUtils.enableAutoCommit(connection);
            // Close the database resources.
            MLDatabaseUtils.closeDatabaseResources(connection, getStatement, result);
        }
    }

    // TODO
    public void insertModelSettings(String modelSettingsID, String workflowID, String algorithmName,
            String algorithmClass, String response, double trainDataFraction, List<HyperParameter> hyperparameters)
            throws DatabaseHandlerException {

    }

    // TODO
    public long getModelExecutionEndTime(String modelId) throws DatabaseHandlerException {
        return getModelExecutionTime(modelId, SQLQueries.GET_MODEL_EXE_END_TIME);
    }

    // TODO
    public long getModelExecutionStartTime(String modelId) throws DatabaseHandlerException {
        return getModelExecutionTime(modelId, SQLQueries.GET_MODEL_EXE_START_TIME);
    }

    // TODO
    /**
     * This helper class is used to extract model execution start/end time
     *
     * @param modelId
     * @param query
     * @return
     * @throws DatabaseHandlerException
     */
    public long getModelExecutionTime(String modelId, String query) throws DatabaseHandlerException {
        Connection connection = null;
        ResultSet result = null;
        PreparedStatement statement = null;
        try {
            connection = dbh.getDataSource().getConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, modelId);
            result = statement.executeQuery();
            if (result.first()) {
                Timestamp time = result.getTimestamp(1);
                if (time != null) {
                    return time.getTime();
                }
                return 0;
            } else {
                throw new DatabaseHandlerException("No timestamp data associated with model id: " + modelId);
            }

        } catch (SQLException e) {
            throw new DatabaseHandlerException(
                    " An error has occurred while reading execution time from the database: " + e.getMessage(), e);
        } finally {
            // closing database resources
            MLDatabaseUtils.closeDatabaseResources(connection, statement, result);
        }
    }

}
