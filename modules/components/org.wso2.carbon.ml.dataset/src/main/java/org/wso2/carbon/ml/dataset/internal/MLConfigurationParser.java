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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.dataset.internal;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.ml.dataset.exceptions.MLConfigurationParserException;
import org.wso2.carbon.ml.dataset.internal.constants.DatasetConfigurations;
import org.wso2.carbon.ml.dataset.internal.dto.DataUploadSettings;
import org.wso2.carbon.ml.dataset.internal.dto.SummaryStatisticsSettings;

/**
 * Class contains methods for parsing configurations from ml-config XML file.
 */
public class MLConfigurationParser {

    private static final Log logger = LogFactory.getLog(MLConfigurationParser.class);
    private Document document;

    MLConfigurationParser() throws MLConfigurationParserException {
        try {
            File xmlFile = new File(DatasetConfigurations.ML_CONFIG_XML);
            if (xmlFile.exists()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder;
                dBuilder = dbFactory.newDocumentBuilder();
                this.document = dBuilder.parse(xmlFile);
            }
        } catch (Exception e) {
            throw new MLConfigurationParserException("An error occurred while parsing " +
                DatasetConfigurations.ML_CONFIG_XML + " : " + e.getMessage(), e);
        }
    }

    /**
     * Parse and return default file uploading settings from ml-config.xml.
     *
     * @return Data upload settings
     * @throws MLConfigurationParserException
     */
    protected DataUploadSettings getDataUploadSettings() throws MLConfigurationParserException {
        DataUploadSettings dataUploadSettings = new DataUploadSettings();
        try {
            NodeList nodes = this.document.getElementsByTagName(DatasetConfigurations
                .UPLOAD_SETTINGS).item(0) .getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(DatasetConfigurations.UPLOAD_LOCATION)) {
                    dataUploadSettings.setUploadLocation(nodes.item(i).getTextContent());
                } else if (nodes.item(i).getNodeName().equals(DatasetConfigurations
                    .IN_MEMORY_THRESHOLD)) {
                        dataUploadSettings.setInMemoryThreshold(Integer.parseInt(nodes.item(i)
                            .getTextContent()));
                } else if (nodes.item(i).getNodeName().equals(DatasetConfigurations.UPLOAD_LIMIT)) {
                    dataUploadSettings.setUploadLimit(Long .parseLong(nodes.item(i)
                        .getTextContent()));
                }
            }
            if(logger.isDebugEnabled()){
                logger.info("Successfully parsed data uploading settings.");
            }
        } catch (Exception e) {
            throw new MLConfigurationParserException( "An error occurred while retrieving data " +
                "upload settings: " + e.getMessage(), e);
        }
        return dataUploadSettings;
    }

    /**
     * Parse default summary statistics generation settings from ml-config.xml.
     *
     * @return Summary statistics settings
     * @throws MLConfigurationParserException
     */
    protected SummaryStatisticsSettings getSummaryStatisticsSettings() 
            throws MLConfigurationParserException {
        SummaryStatisticsSettings summaryStatisticsSettings = new SummaryStatisticsSettings();
        try {
            NodeList nodes = this.document .getElementsByTagName(DatasetConfigurations
                .SUMMARY_STATISTICS_SETTINGS).item(0).getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeName().equals(DatasetConfigurations.HISTOGRAM_BINS)) {
                    summaryStatisticsSettings.setHistogramBins(Integer.parseInt(nodes.item(i)
                        .getTextContent()));
                } else if (nodes.item(i).getNodeName()
                        .equals(DatasetConfigurations.CATEGORICAL_THRESHOLD)) {
                    summaryStatisticsSettings.setCategoricalThreshold(Integer.parseInt(nodes
                        .item(i).getTextContent()));
                } else if (nodes.item(i).getNodeName().equals(DatasetConfigurations.SAMPLE_SIZE)) {
                    summaryStatisticsSettings.setSampleSize(Integer.parseInt(nodes.item(i)
                        .getTextContent()));
                }
            }
            if(logger.isDebugEnabled()){
                logger.info("Successfully parsed summary statistics settings.");
            }
        } catch (Exception e) {
            throw new MLConfigurationParserException( "An error occurred while retrieving " +
                "summary statistics settings: " + e.getMessage(), e);
        }
        return summaryStatisticsSettings;
    }
    
    /**
     * Parse the JNDI lookup name of the ML database from the ml-config.xml file
     * @return JNDI lookup name of the ML database
     * @throws MLConfigurationParserException 
     */
    protected String getDatabaseName() throws MLConfigurationParserException{
        try{
            return this.document.getElementsByTagName(DatasetConfigurations.DATABASE).item(0)
                    .getTextContent();
        } catch(Exception e){
            throw new MLConfigurationParserException( "An error occurred while retrieving ML " +
                "database name: " + e.getMessage(), e);
        }
    }
}
