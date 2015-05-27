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
public class MLProject {

    private static final String PROJECT_NAME="test-project-name";
    private static final String PROJECT_DESCRIPTION="test-project-description";
    private static final String DATASET_NAME ="test-dataset";
    
    private MLProject() {
    }
    
    public static String getProjectName() {
        return PROJECT_NAME;
    }
    public static String getProjectDescription() {
        return PROJECT_DESCRIPTION;
    }
    public static String getDatasetName() {
        return DATASET_NAME;
    }
    
}
