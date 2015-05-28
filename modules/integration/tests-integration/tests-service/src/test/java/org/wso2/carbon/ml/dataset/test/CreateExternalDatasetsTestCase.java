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

package org.wso2.carbon.ml.dataset.test;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ml.integration.common.utils.MLBaseTest;
import org.wso2.carbon.ml.integration.common.utils.MLHttpClient;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationTestConstants;
import org.wso2.carbon.ml.integration.common.utils.exception.MLHttpClientException;

/**
 * Contains test cases related to creating external datasets
 * Needs to download and store the data files explicitly in "resources/data/"
 */

@Test(groups="createExternalDatasets", dependsOnGroups = "createAdditionalDatasets")
public class CreateExternalDatasetsTestCase extends MLBaseTest{

    private MLHttpClient mlHttpclient;
    private CloseableHttpResponse response;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        mlHttpclient = new MLHttpClient(instance, userInfo);
    }

    /**
     * Creates external dataset - Digit recognition
     * @throws MLHttpClientException
     * @throws IOException
     */
    @Test(description = "Create a dataset of Digit recognition data from a CSV file")
    public void testCreateDatasetDigits() throws IOException {
        try {
            response = mlHttpclient.uploadDatasetFromCSV(MLIntegrationTestConstants.DATASET_NAME_DIGITS,
                    "1.0", MLIntegrationTestConstants.DIGIT_RECOGNITION_DATASET_SAMPLE);
        } catch ( MLHttpClientException e) {
            // Skip test if dataset is not available in the given location
            throw new SkipException("Skipping tests because dataset with name: " + MLIntegrationTestConstants.DATASET_NAME_DIGITS
                    + " is not available at the location" + MLIntegrationTestConstants.DIGIT_RECOGNITION_DATASET_SAMPLE);
        }
    }

}
