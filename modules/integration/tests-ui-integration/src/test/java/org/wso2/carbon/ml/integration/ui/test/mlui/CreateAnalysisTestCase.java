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

package org.wso2.carbon.ml.integration.ui.test.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.ml.integration.common.utils.MLIntegrationUiBaseTest;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLProjectsPage;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLUIHomePage;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLUILoginPage;
import org.wso2.carbon.ml.integration.ui.pages.mlui.PreprocessPage;
import org.wso2.carbon.ml.integration.ui.test.dto.MLAnalysis;
import org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException;
import org.wso2.carbon.ml.integration.ui.test.exceptions.CreateProjectTestException;

public class CreateAnalysisTestCase extends MLIntegrationUiBaseTest {

    private static final Log logger = LogFactory.getLog(CreateAnalysisTestCase.class);

    private MLUIHomePage mlUiHomePage;
    private MLProjectsPage mlProjectsPage;
    private PreprocessPage preprocessPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getMLUiUrl());
    }

    /**
     * Test whether the previously created project is in the projects page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "projects page with previously created project")
    public void testRedirectToProjectsPage() throws CreateAnalysisTestException {
        try {
            MLUILoginPage mlUiLoginPage = new MLUILoginPage(driver);
            mlUiHomePage = mlUiLoginPage.loginAs(userInfo.getUserName(),userInfo.getPassword());
            mlProjectsPage = mlUiHomePage.createProject();
            // Check whether the previously created project is there and allow create analysis
            Assert.assertTrue(mlProjectsPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("create.new.analysis"))),
                    "Previously created project not found");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to create analysis: ", e);
        } catch (MLUIPageCreationException e) {
            throw new CreateAnalysisTestException("Failed to create analysis: ", e);
        }
    }

    /**
     * Test input validation for analysis name field
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "create analysis with empty analysis-name", dependsOnMethods = "testRedirectToProjectsPage")
    public void testCreateAnalysisEmptyName() throws CreateAnalysisTestException {
        try {
            mlProjectsPage.createAnalysis("");
            Assert.assertTrue(mlProjectsPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("analysis.input.validation.message"))),
                    "No input validation for Analysis Name field");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to validate analysis name field: ", e);
        }
    }

    /**
     * Create Analysis and redirect to Preprocess page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "create analysis and redirect to preprocess page", dependsOnMethods = "testCreateAnalysisEmptyName")
    public void testCreateAnalysis() throws CreateAnalysisTestException {
        try {
            preprocessPage = mlProjectsPage.createAnalysis(MLAnalysis.getAnalysisName());
            Assert.assertTrue(preprocessPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("preprocess.page.title"))),
                    "Did not redirect to Preprocess page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to create analysis: ", e);
        }
    }


    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }

}
