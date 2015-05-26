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

import java.io.File;

public class DataImportPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(DataImportPage.class);

    /**
     * Creates a data import page
     *
     * @param driver    Instance of the web driver
     * @throws          MLUIPageCreationException 
     */
    public DataImportPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }

    /**
     * Import dataset
     * @param dataFile
     * @param datasetName
     * @param version
     * @param description
     * @param sourceType
     * @param dataFormat
     * @param columnHeader
     * @param destinationType
     * @return
     * @throws InvalidPageException
     */
    public MLDatasetsPage importData(File dataFile, String datasetName, String version,
            String description, String sourceType, String dataFormat, String columnHeader, String destinationType) throws InvalidPageException {
        try {

            WebElement datasetNameElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.name")));
            WebElement datasetVerionElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.version")));
            WebElement descriptionElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.description")));
            WebElement sourceTypeElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.source.type")));
            WebElement pathElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.path")));
            WebElement dataFormatElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.data.format")));
            WebElement columnHeaderElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.column.header")));
            WebElement destinationTypeElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("dataset.destination.type")));

//            datasetNameElement.clear();
//            datasetVerionElement.clear();
//            descriptionElement.clear();

            datasetNameElement.sendKeys(datasetName);
            datasetVerionElement.sendKeys(version);
            descriptionElement.sendKeys(description);
            sourceTypeElement.sendKeys(sourceType);
            pathElement.sendKeys(dataFile.getPath());
            dataFormatElement.sendKeys(dataFormat);
            columnHeaderElement.sendKeys(columnHeader);
            destinationTypeElement.sendKeys(destinationType);

            driver.findElement(By.xpath(mlUIElementMapper.getElement("import.dataset.button"))).click();
            return new MLDatasetsPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Error occurred while importing dataset: ", e);
        }
    }
}