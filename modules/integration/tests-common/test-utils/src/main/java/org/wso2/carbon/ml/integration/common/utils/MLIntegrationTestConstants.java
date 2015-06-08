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

/**
 * This class contains all the constants used in ML integration tests
 */
public class MLIntegrationTestConstants {
	public static final String ML_PRODUCT_GROUP = "ML";
	public static final String ML_UI = "/ml/";
	public static final String ML_UI_ELEMENT_MAPPER = "/mlUiMapper.properties";
	public static final String CARBON_UI_ELEMENT_MAPPER = "/carbonUiMapper.properties";

	public static final String CARBON_CLIENT_TRUSTSTORE = "/keystores/products/client-truststore.jks";
	public static final String CARBON_CLIENT_TRUSTSTORE_PASSWORD = "wso2carbon";
	public static final String JKS = "JKS";
	public static final String TLS = "TLS";

	public static final String HTTPS = "https";

	// Constants related to REST calls
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BASIC = "Basic ";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

	// Constants related to configuring models
	public static final String TRAIN_DATA_FRACTION_CONFIG = "trainDataFraction";
	public static final String RESPONSE = "responseVariable";
	public static final String ALGORITHM_NAME = "algorithmName";
	public static final String ALGORITHM_TYPE = "algorithmType";

	// The time constant
    public static final long THREAD_SLEEP_TIME_SMALL = 4000;
    public static final long THREAD_SLEEP_TIME_MEDIUM = 7500;
    public static final long THREAD_SLEEP_TIME_LARGE = 15000;

	// Constants for Test cases
	public static final String CLASSIFICATION = "Classification";
	public static final String NUMERICAL_PREDICTION = "Numerical_Prediction";
	public static final String CLUSTERING = "Clustering";
	public static final String TRAIN_DATA_FRACTION = "0.7";

	// Constants for locations of datasets - Happy scenario
	public static final String DIABETES_DATASET_SAMPLE = "data/pIndiansDiabetes.csv";
	public static final String YACHT_DATASET_SAMPLE = "data/yachtHydrodynamics.csv";
	public static final String BREAST_CANCER_DATASET_SAMPLE = "data/breastCancerWisconsin.csv";
	public static final String FOREST_FIRES_DATASET_SAMPLE = "data/forestfires.csv";
    public static final String GAMMA_TELESCOPE_DATASET_SAMPLE = "data/gammaTelescope.csv";

    // Datasets with missing values and categorical features
    public static final String AUTOMOBILE_DATASET_SAMPLE = "data/automobile.csv";
	public static final String AZURE_STREAMING_DATASET_SAMPLE = "data/azure-stream-analytics_entry.csv";

	public static final String DATASET_NAME_DIABETES = "Diabetes";
	public static final int DATASET_ID_DIABETES = 1;

	// Number the datasets starting from number 2 (number 1 reserved for Diabetes dataset)
	public static final String DATASET_NAME_YACHT = "Yacht_Hydrodynamics";
	public static int DATASET_ID_YACHT = 2;
	public static final String DATASET_NAME_BREAST_CANCER = "Breast_Cancer";
	public static int DATASET_ID_BREAST_CANCER = 3;
	public static final String DATASET_NAME_FOREST_FIRES = "Forest_Fires";
	public static int DATASET_ID_FOREST_FIRES = 4;
    public static final String DATASET_NAME_GAMMA_TELESCOPE = "Gamma_Telescope";
    public static int DATASET_ID_GAMMA_TELESCOPE = 5;
    public static final String DATASET_NAME_AUTOMOBILE = "Automobile";
    public static int DATASET_ID_AUTOMOBILE = 6;
    public static final String DATASET_NAME_AZURE_STREAMING = "Azure_Streaming";
    public static int DATASET_ID_AZURE_STREAMING = 7;

	public static final int VERSIONSET_ID = 1;

    // Response attributes for supervised learning
	public static final String RESPONSE_ATTRIBUTE_DIABETES = "Class";
	public static final String RESPONSE_ATTRIBUTE_YACHT = "ResiduaryResistance";
	public static final String RESPONSE_ATTRIBUTE_BREAST_CANCER = "Class";
	public static final String RESPONSE_ATTRIBUTE_FOREST_FIRES = "area";
    public static final String RESPONSE_ATTRIBUTE_GAMMA_TELESCOPE = "class";
	public static final String RESPONSE_ATTRIBUTE_AUTOMOBILE = "price";
    public static final String RESPONSE_ATTRIBUTE_AZURE_STREAMING = "price";

    // Projects
	public static final String PROJECT_NAME_DIABETES = "Diabetes_Project";
	public static final int PROJECT_ID_DIABETES = 1;

	public static final String PROJECT_NAME_YACHT = "Yacht_Hydrodynamics_Project";
	public static final String PROJECT_NAME_BREAST_CANCER = "Breast_Cancer_Project";
	public static final String PROJECT_NAME_FOREST_FIRES = "Forest_Fires_Project";
    public static final String PROJECT_NAME_GAMMA_TELESCOPE = "Gamma_Telescope_Project";
    public static final String PROJECT_NAME_AUTOMOBILE = "Automobile_Project";
    public static final String PROJECT_NAME_AZURE_STREAMING = "Azure_Streaming_Project";

    // Default analysis
	public static final String ANALYSIS_NAME = "Dummy_Analysis";
	public static final int ANALYSIS_ID = 1;

    // Default model
	public static String MODEL_NAME;
	public static final int MODEL_ID = 1;

    // Storage file location
	public static final String FILE_STORAGE_LOCATION = "Models/file-storage";

	// External datasets
	// You need to download these datasets explicitly (See README.txt)

    // Digit recognition dataset.
    // Rename the dataset to "digitRecognition.csv"
    public static final String DIGIT_RECOGNITION_DATASET_SAMPLE = "data/digitRecognition.csv";
    public static final String DATASET_NAME_DIGITS = "Digit_Recognition";
    public static final int DATASET_ID_DIGITS = 8;
    public static final String PROJECT_NAME_DIGITS = "Digit_recognition_Project";
	public static final String RESPONSE_ATTRIBUTE_DIGITS = "label";

}