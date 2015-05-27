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
import org.wso2.carbon.ml.integration.ui.pages.mlui.NewProjectPage;
import org.wso2.carbon.ml.integration.ui.test.dto.MLProject;
import org.wso2.carbon.ml.integration.ui.test.exceptions.CreateProjectTestException;
import org.wso2.carbon.ml.integration.ui.test.exceptions.ImportDataTestException;

public class CreateProjectTestCase extends MLIntegrationUiBaseTest {

    private static final Log logger = LogFactory.getLog(CreateProjectTestCase.class);

    private MLUIHomePage mlUiHomePage;
    private MLProjectsPage mlProjectsPage;
    private NewProjectPage newProjectPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getMLUiUrl());
    }

    /**
     * Test login to the ml UI using user credentials
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateProjectTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify login to ML UI")
    public void testLoginToMLUI() throws CreateProjectTestException {
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
            throw new CreateProjectTestException("Login to ML UI failed: ", e);
        } catch (MLUIPageCreationException e) {
            throw new CreateProjectTestException("Failed to create a login page: ", e);
        }
    }

    /**
     * Test the Projects button in homepage
     *
     * @throws ImportDataTestException
     */
    @Test(groups = "wso2.ml.ui", description = "redirect to projects page", dependsOnMethods = "testLoginToMLUI")
    public void testRedirectToProjectsPage() throws CreateProjectTestException {
        try {
            mlProjectsPage = mlUiHomePage.createProject();
            // Check whether its the correct page
            Assert.assertTrue(mlProjectsPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("create.new.project"))),
                    "Did not redirect to projects page.");
        }  catch (InvalidPageException e) {
            throw new CreateProjectTestException("Failed to create project: ", e);
        }
    }

    /**
     * Test the Create Project button in Projects Page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateProjectTestException
     */
    @Test(groups = "wso2.ml.ui", description = "redirect to create project page", dependsOnMethods = "testRedirectToProjectsPage")
    public void testRedirectToCreateProject() throws CreateProjectTestException {
        try {
            newProjectPage = mlProjectsPage.createProject();
            Assert.assertTrue(newProjectPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("save.project.button"))),
                    "Did not redirect to Create Project page.");
        }  catch (InvalidPageException e) {
            throw new CreateProjectTestException("Failed to create project: ", e);
        }
    }

    /**
     * Test create new project with empty fields
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateProjectTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify input validation for empty fields",
            dependsOnMethods = "testRedirectToCreateProject")
    public void testCreateProjectWithEmptyName() throws CreateProjectTestException {
        try {
            newProjectPage.createNewProject("", "", MLProject.getDatasetName());
            Assert.assertTrue(newProjectPage.isElementPresent(By.xpath(mlUIElementMapper
                    .getElement("project.name.error"))), "No validation for project name field");
            Assert.assertTrue(newProjectPage.isElementPresent(By.xpath(mlUIElementMapper
                    .getElement("project.description.error"))), "No validation for project description field");
        } catch (InvalidPageException e) {
            throw new CreateProjectTestException("Failed to validate input fields: ", e);
        }
    }

    /**
     * Test create new project with all fields filled
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateProjectTestException
     */
    @Test(groups = "wso2.ml.ui", description = "verify create new project with all fields filled",
            dependsOnMethods = "testCreateProjectWithEmptyName")
    public void testCreateProject() throws CreateProjectTestException {
        try {
            mlProjectsPage = newProjectPage.createNewProject(MLProject.getProjectName(),
                    MLProject.getProjectDescription(), MLProject.getDatasetName());
            // Check whether redirects to the projects page
            Assert.assertTrue(mlProjectsPage.isElementPresent(By.xpath(mlUIElementMapper
                    .getElement("create.new.project"))), "Did not redirect to Projects page");
            //Check whether the page is populated with the added project
            Assert.assertTrue(mlProjectsPage.getElementCount((By.xpath(mlUIElementMapper
                    .getElement("projects.table")))) > 0, "Project view table is not populated");
        } catch (InvalidPageException e) {
            throw new CreateProjectTestException("Failed to create project: ", e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }
}
