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
 * Java bean class to hold details of ML Analysis
 */
public class MLAnalysis {

    private static final String ANALYSIS_NAME ="test-analysis";
    private static final String ALGORITHM_NAME ="LOGISTIC REGRESSION";
    private static final String RESPONSE_VARIABLE ="Class";

    public static String getAnalysisName() {
        return ANALYSIS_NAME;
    }
    public static String getAlgorithmName() {
        return ALGORITHM_NAME;
    }
    public static String getResponseVariable() {
        return RESPONSE_VARIABLE;
    }
}
