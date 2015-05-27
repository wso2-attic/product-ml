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

public class MLProjectsPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(MLProjectsPage.class);

    /**
     * Creates a MLProjects page
     *
     * @param driver instance of the web driver
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException
     */
    public MLProjectsPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }

    /**
     * Create a NewProjectPage
     * @return
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException
     */
    public NewProjectPage createProject() throws InvalidPageException {
        try {
            driver.findElement(By.xpath(mlUIElementMapper.getElement("create.new.project"))).click();
            return new NewProjectPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create NewProject Page: ", e);
        }
    }

    /**
     * Create a new analysis and return the Preprocess page
     * @param analysisName name of the analysis
     * @return
     * @throws InvalidPageException
     */
    public PreprocessPage createAnalysis(String analysisName) throws InvalidPageException {
        try {
            WebElement datasetNameElement = driver.findElement(By.xpath(mlUIElementMapper.getElement("analysis.name")));
            datasetNameElement.sendKeys(analysisName);
            driver.findElement(By.xpath(mlUIElementMapper.getElement("create.new.analysis"))).click();
            return new PreprocessPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create Preprocess Page: ", e);
        }
    }
}
