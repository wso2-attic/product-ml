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

package org.wso2.carbon.ml.integration.ui.pages.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

public class MLDatasetsPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(MLDatasetsPage.class);

    /**
     * Creates a MLDatasets page
     *
     * @param driver instance of the web driver
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException
     */
    public MLDatasetsPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }

    /**
     * Create a new DataImportPage
     * @return
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException
     */
    public DataImportPage createDataset() throws InvalidPageException {
        try {
            driver.findElement(By.xpath(mlUIElementMapper.getElement("create.new.dataset"))).click();
            return new DataImportPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create Datasets Page: ", e);
        }
    }

    /**
     * Expand the versions of the dataset
     * @return
     */
    public void expandDatasetVersions() {
        driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.row"))).click();
    }

    /**
     * Create a new DatasetVersionPage
     * @return
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException
     */
    public DatasetVersionPage createDatasetVersion(String datasetVersion) throws InvalidPageException {
        try {
            WebElement datasetVersionElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.version.input")));
            datasetVersionElement.sendKeys(datasetVersion);
            driver.findElement(By.xpath(mlUIElementMapper.getElement("create.dataset.version"))).click();
            return new DatasetVersionPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create Dataset Version Page: ", e);
        }
    }
}
