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
import org.wso2.carbon.ml.integration.ui.pages.mlui.*;
import org.wso2.carbon.ml.integration.ui.test.dto.MLProject;
import org.wso2.carbon.ml.integration.ui.test.exceptions.ImportDataTestException;

import java.io.File;

/**
 * Test case for login and logout of ml UI.
 */
public class ImportDataTestCase extends MLIntegrationUiBaseTest {

    private static final Log logger = LogFactory.getLog(ImportDataTestCase.class);
    
    private MLUIHomePage mlUiHomePage;
    private DataImportPage dataImportPage;
    private DatasetSummaryPage datasetSummaryPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getMLUiUrl());
    }

    /**
     * Test login to the ml UI using user credentials
     * 
     * @throws ImportDataTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify login to ML UI")
    public void testLoginToMLUI() throws ImportDataTestException {
        try {
            MLUILoginPage mlUiLoginPage = new MLUILoginPage(driver);
            // Check whether its the correct page
            Assert.assertTrue(mlUiLoginPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("login.title"))),
                    "This is not the login page.");
            mlUiHomePage = mlUiLoginPage.loginAs(userInfo.getUserName(),userInfo.getPassword());
            // Check whether it redirects to the home page
            Assert.assertTrue(mlUiHomePage.isElementPresent(By.xpath(mlUIElementMapper.getElement("home.page.projects"))),
                    "Did not redirect to home page.");
        } catch (InvalidPageException e) {
            throw new ImportDataTestException("Login to ML UI failed: ", e);
        } catch (MLUIPageCreationException e) {
            throw new ImportDataTestException("Failed to create a login page: ", e);
        }
    }

    /**
     * Test the Datasets button in homepage
     *
     * @throws ImportDataTestException
     */
    @Test(groups = "wso2.ml.ui", description = "redirect to datasets page", dependsOnMethods = "testLoginToMLUI")
    public void testRedirectToDatasetsPage() throws ImportDataTestException {
        try {
            dataImportPage = mlUiHomePage.createDataset();
            // Check whether its the correct page
            Assert.assertTrue(dataImportPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("create.new.dataset"))),
                    "Did not redirect to datasets page.");
        }  catch (InvalidPageException e) {
            throw new ImportDataTestException("Failed to create dataset: ", e);
        }
    }

    /**
     * Test importing a data-set without the project name.
     * 
     * @throws ImportDataTestException 
     */
   @Test(groups = "wso2.ml.ui", description = "verify importing a data-set without the project name",
            dependsOnMethods = "testRedirectToDatasetsPage")
    public void testImportDataWithoutProjectName() throws ImportDataTestException {
        /*try {
            File dataFile = new File(ImportDataTestCase.class.getResource(MLProject.getDatasetUrl()).toString());
            datasetSummaryPage = dataImportPage.importData(dataFile, MLProject.getProjectName(),
                    MLProject.getProjectDescription(), MLProject.getWorkflowName());
            // Check whether its the correct page
            Assert.assertTrue(datasetSummaryPage.isElementPresent(By.xpath(mlUIElementMapper
                    .getElement("data.import.div"))), "Did not remain in the import data page.");
            //TODO : Check whether the error message is printed
        } catch (InvalidPageException e) {
            throw new ImportDataTestException("Failed to import data without project name: ", e);
        }*/
    }
    
    /**
     * Test importing a data-set with all fields filled.
     * 
     * @throws ImportDataTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify importing a data-set with all fields filled",
            dependsOnMethods = "testImportDataWithoutProjectName")
    public void testImportData() throws ImportDataTestException {
        /*try {
            File dataFile = new File(ImportDataTestCase.class.getResource(MLProject.getDatasetUrl()).toString());
            datasetSummaryPage = dataImportPage.importData(dataFile, MLProject.getProjectName(),
                    MLProject.getProjectDescription(), MLProject.getWorkflowName());
            // Check whether its the page.
            Assert.assertTrue(datasetSummaryPage.isElementPresent(By.id(mlUIElementMapper
                    .getElement("dataset.summary.table"))), "Did not redirect to Data summary page");
            //Check whether the table is populated
            Assert.assertTrue(datasetSummaryPage.getElementCount(By.className(mlUIElementMapper
                    .getElement("feature.rows"))) > 0, "Data view table is not populated");
        } catch (InvalidPageException e) {
            throw new ImportDataTestException("Failed to import data: ", e);
        }*/
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }
}
