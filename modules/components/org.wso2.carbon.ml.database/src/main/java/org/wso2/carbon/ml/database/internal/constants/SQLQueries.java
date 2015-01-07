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

package org.wso2.carbon.ml.database.internal.constants;

/**
 * A utility class to store SQL prepared statements
 */
public class SQLQueries {

    public static final String INSERT_ML_MODEL_SETTINGS =
            "INSERT INTO ML_MODEL_SETTINGS(MODEL_SETTINGS_ID,WORKFLOW_ID,ALGORITHM_CLASS," +
            "ALGORITHM_NAME,RESPONSE,TRAIN_DATA_FRACTION,HYPER_PARAMETERS) VALUES(?,?,?,?,?,?,?)";

    public static final String UPDATE_ML_MODEL = "UPDATE ML_MODEL SET MODEL=?," +
                                                 "MODEL_SUMMARY=?," +
                                                 "MODEL_EXECUTION_END_TIME=?"
                                                 + "WHERE MODEL_ID=?";

    public static final String INSERT_ML_MODEL = "INSERT INTO ML_MODEL(MODEL_ID,WORKFLOW_ID," +
                                                 "MODEL_EXECUTION_START_TIME) VALUES(?,?,?)";

    public static final String GET_MODEL_SUMMARY = "SELECT MODEL_SUMMARY FROM ML_MODEL WHERE "
                                                   + "MODEL_ID=?";

    public static final String GET_ML_MODEL_SETTINGS =
            "SELECT ALGORITHM_CLASS,ALGORITHM_NAME,RESPONSE,TRAIN_DATA_FRACTION,HYPER_PARAMETERS " +
            "FROM ML_MODEL_SETTINGS WHERE "
            + "WORKFLOW_ID=?";

    public static final String GET_ML_FEATURE_SETTINGS = "SELECT FEATURE_NAME,FEATURE_INDEX, TYPE," +
                                                         "IMPUTE_METHOD, INCLUDE FROM ML_FEATURE_SETTINGS WHERE " +
                                                         "WORKFLOW_ID=?";

    public static final String GET_DATASET_LOCATION = "SELECT DATASET_URL FROM ML_DATASET WHERE "
                                                      + "DATASET_ID IN (SELECT DATASET_ID FROM " +
                                                      "ML_WORKFLOW WHERE WORKFLOW_ID=?)";

    public static final String GET_MODEL_EXE_END_TIME = " SELECT MODEL_EXECUTION_END_TIME FROM " +
                                                        "ML_MODEL WHERE MODEL_ID =  ?";

    public static final String GET_MODEL_EXE_START_TIME = " SELECT MODEL_EXECUTION_START_TIME " +
                                                          "FROM " +
                                                          "ML_MODEL WHERE MODEL_ID =  ?";

    /*
     * private Constructor to prevent any other class from instantiating.
     */
    private SQLQueries() {
    }
}
