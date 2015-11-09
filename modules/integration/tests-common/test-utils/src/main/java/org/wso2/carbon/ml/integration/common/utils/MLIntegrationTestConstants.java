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
	public static final String NORMAL_LABELS_CONFIG = "normalLabels";
	public static final String NEW_NORMAL_LABEL_CONFIG = "newNormalLabel";
	public static final String NEW_ANOMALY_LABEL_CONFIG = "newAnomalyLabel";
	public static final String NORMALIZATION_CONFIG = "normalization";
	public static final String RESPONSE_ATTRIBUTE_DEFAULT = "";
	public static final String TRAIN_DATA_FRACTION_DEFAULT = "1.0";
	public static final String NORMAL_LABELS_DEFAULT = "";

	// The time constant
    public static final long THREAD_SLEEP_TIME_LARGE = 20000;

	// Constants for Test cases
	public static final String CLASSIFICATION = "Classification";
	public static final String NUMERICAL_PREDICTION = "Numerical_Prediction";
	public static final String CLUSTERING = "Clustering";
	public static final String ANOMALY_DETECTION = "Anomaly_Detection";
	public static final String TRAIN_DATA_FRACTION = "0.7";
	public static final String NORMAL_LABELS = "0";
	public static final String NEW_NORMAL_LABEL = "normal";
	public static final String NEW_ANOMALY_LABEL = "anomaly";
	public static final boolean NORMALIZATION = false;

	// Constants for locations of datasets - Happy scenario
	public static final String DATASETS_PATH = "artifacts/ML/data/";
	public static final String DAS_DATASET_SAMPLE = "STREAMING_DATA";
	public static final String DIABETES_DATASET_SAMPLE = DATASETS_PATH+"pIndiansDiabetes.csv";
	public static final String DIABETES_DATASET_TEST = DATASETS_PATH+"pIndiansDiabetesTest.csv";
	public static final String YACHT_DATASET_SAMPLE = DATASETS_PATH+"yachtHydrodynamics.csv";
	public static final String BREAST_CANCER_DATASET_SAMPLE = DATASETS_PATH+"breastCancerWisconsin.csv";
	public static final String FOREST_FIRES_DATASET_SAMPLE = DATASETS_PATH+"forestfires.csv";
    public static final String GAMMA_TELESCOPE_DATASET_SAMPLE = DATASETS_PATH+"gammaTelescope.csv";
    public static final String ABALONE_DATASET_SAMPLE = DATASETS_PATH+"abalone.csv";
    public static final String TITANIC_DATASET_SAMPLE = DATASETS_PATH + "titanic.csv";

    // Datasets with missing values and categorical features
    public static final String AUTOMOBILE_DATASET_SAMPLE = DATASETS_PATH+"automobile.csv";
	public static final String AZURE_STREAMING_DATASET_SAMPLE = DATASETS_PATH+"azure-stream-analytics_entry.csv";

	public static final String DATASET_NAME_DIABETES = "Diabetes";
	public static final int DATASET_ID_DIABETES = 1;
	public static final int DATASET_ID_DAS = 2;

	public static final String DATASET_NAME_DAS = "das_data";
	public static final String DATASET_NAME_YACHT = "Yacht_Hydrodynamics";
	public static final String DATASET_NAME_BREAST_CANCER = "Breast_Cancer";
	public static final String DATASET_NAME_FOREST_FIRES = "Forest_Fires";
    public static final String DATASET_NAME_GAMMA_TELESCOPE = "Gamma_Telescope";
    public static final String DATASET_NAME_AUTOMOBILE = "Automobile";
    public static final String DATASET_NAME_AZURE_STREAMING = "Azure_Streaming";
    public static final String DATASET_NAME_ABALONE = "Abalone";
    public static final String DATASET_NAME_TITANIC = "Titanic";

	public static final int VERSIONSET_ID = 1;

    // Response attributes for supervised learning
	public static final String RESPONSE_ATTRIBUTE_DIABETES = "Class";
	public static final String RESPONSE_ATTRIBUTE_DAS = "value";
	public static final String RESPONSE_ATTRIBUTE_YACHT = "ResiduaryResistance";
	public static final String RESPONSE_ATTRIBUTE_BREAST_CANCER = "Class";
	public static final String RESPONSE_ATTRIBUTE_FOREST_FIRES = "area";
    public static final String RESPONSE_ATTRIBUTE_GAMMA_TELESCOPE = "class";
	public static final String RESPONSE_ATTRIBUTE_AUTOMOBILE = "price";
    public static final String RESPONSE_ATTRIBUTE_AZURE_STREAMING = "price";
    public static final String RESPONSE_ATTRIBUTE_ABALONE = "Sex";
    public static final String RESPONSE_ATTRIBUTE_TITANIC = "Survived";

    // Projects
	public static final String PROJECT_NAME_DIABETES = "Diabetes_Project";
	public static final int PROJECT_ID_DIABETES = 1;

	public static final String PROJECT_NAME_DAS = "DAS_Project";
	public static final String PROJECT_NAME_YACHT = "Yacht_Hydrodynamics_Project";
	public static final String PROJECT_NAME_BREAST_CANCER = "Breast_Cancer_Project";
	public static final String PROJECT_NAME_FOREST_FIRES = "Forest_Fires_Project";
    public static final String PROJECT_NAME_GAMMA_TELESCOPE = "Gamma_Telescope_Project";
    public static final String PROJECT_NAME_AUTOMOBILE = "Automobile_Project";
    public static final String PROJECT_NAME_AZURE_STREAMING = "Azure_Streaming_Project";
    public static final String PROJECT_NAME_ABALONE = "Abalone_Project";
    public static final String PROJECT_NAME_TITANIC = "Titanic_Project";

    // Default analysis
	public static final String ANALYSIS_NAME = "Dummy_Analysis";
	public static final String ANALYSIS_NAME_2 = "Dummy_Analysis_2";

    // Default model
	public static String MODEL_NAME;
	public static final int MODEL_ID = 1;

    // Storage file location
	public static final String FILE_STORAGE_LOCATION = "Models/file-storage";

	// External datasets
	// You need to download these datasets explicitly (See README.txt)

    // Digit recognition dataset.
    // Rename the dataset to "digitRecognition.csv"
    public static final String DIGIT_RECOGNITION_DATASET_SAMPLE = DATASETS_PATH+"digitRecognition.csv";
    public static final String DATASET_NAME_DIGITS = "Digit_Recognition";
    public static final String PROJECT_NAME_DIGITS = "Digit_recognition_Project";
	public static final String RESPONSE_ATTRIBUTE_DIGITS = "label";

}