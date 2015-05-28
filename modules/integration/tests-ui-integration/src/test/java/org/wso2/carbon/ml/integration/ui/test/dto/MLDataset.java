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

package org.wso2.carbon.ml.integration.ui.test.dto;

/**
 * Java bean class to hold details of ML Projects
 */
public class MLDataset {

    private static final String DATASET_URL = "/data/pIndiansDiabetes.csv";;
    private static final String DATASET_NAME = "test-dataset";
    private static final String VERSION = "1.0.0";
    private static final String VERSION_2 = "2.0.0";
    private static final String DESCRIPTION = "test-dataset-description";
    private static final String SOURCE_TYPE = "file";
    private static final String DATA_FORMAT = "csv";
    private static final String COLUMN_HEADER = "yes";
    private static final String DESTINATION_TYPE = "file";

    public static String getDatasetUrl() {
        return DATASET_URL;
    }

    public static String getDatasetName() {
        return DATASET_NAME;
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getDescription() {
        return DESCRIPTION;
    }

    public static String getSourceType() {
        return SOURCE_TYPE;
    }

    public static String getDataFormat() {
        return DATA_FORMAT;
    }

    public static String getColumnHeader() {
        return COLUMN_HEADER;
    }

    public static String getDestinationType() {
        return DESTINATION_TYPE;
    }

    public static String getVersion2() {
        return VERSION_2;
    }
}
