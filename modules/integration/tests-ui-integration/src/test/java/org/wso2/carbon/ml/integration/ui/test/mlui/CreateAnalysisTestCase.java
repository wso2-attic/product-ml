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
import org.wso2.carbon.ml.integration.ui.pages.mlui.analysis.*;
import org.wso2.carbon.ml.integration.ui.test.dto.MLAnalysis;
import org.wso2.carbon.ml.integration.ui.test.dto.MLDataset;
import org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException;

public class CreateAnalysisTestCase extends MLIntegrationUiBaseTest {

    private static final Log logger = LogFactory.getLog(CreateAnalysisTestCase.class);

    private MLUIHomePage mlUiHomePage;
    private MLProjectsPage mlProjectsPage;
    private PreprocessPage preprocessPage;
    private ExplorePage explorePage;
    private AlgorithmPage algorithmPage;
    private ParametersPage parametersPage;
    private ModelPage modelPage;
    private AnalysisPage analysisPage;

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
            mlProjectsPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("create.new.analysis")));
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
            mlProjectsPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("analysis.input.validation.message")));
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
            preprocessPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("preprocess.page.title")));
            Assert.assertTrue(preprocessPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("preprocess.page.title"))),
                    "Did not redirect to Preprocess page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to create analysis: ", e);
        }
    }

    /**
     * Test next from Preporcess page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "navigate to explore page from preprocess page", dependsOnMethods = "testCreateAnalysis")
    public void testPreprocess() throws CreateAnalysisTestException {
        try {
            explorePage = preprocessPage.next();
            explorePage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("explore.scatter.plot")));
            Assert.assertTrue(explorePage.isElementPresent(By.xpath(mlUIElementMapper.getElement("explore.scatter.plot"))),
                    "Did not redirect to Explore page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Explore Page: ", e);
        }
    }

    /**
     * Test next from Explore page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "navigate to algorithms page from explore page", dependsOnMethods = "testPreprocess")
    public void testExplore() throws CreateAnalysisTestException {
        try {
            algorithmPage = explorePage.next();
            algorithmPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("algorithm.page.title")));
            Assert.assertTrue(algorithmPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("algorithm.page.title"))),
                    "Did not redirect to Algorithms page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Algorithm Page: ", e);
        }
    }

    /**
     * Test next from Algorithm page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "navigate to parameters page from algorithm page", dependsOnMethods = "testExplore")
    public void testAlgorithm() throws CreateAnalysisTestException {
        try {
            parametersPage = algorithmPage.next();
            parametersPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("parameters.page.title")));
            Assert.assertTrue(parametersPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("parameters.page.title"))),
                    "Did not redirect to Parameters page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Parameters Page: ", e);
        }
    }

    /**
     * Test next from Parameters page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "navigate to model page from parameters page", dependsOnMethods = "testAlgorithm")
    public void testParameters() throws CreateAnalysisTestException {
        try {
            modelPage = parametersPage.next();
            modelPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("model.page.title")));
            Assert.assertTrue(modelPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("model.page.title"))),
                    "Did not redirect to Model page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Model Page: ", e);
        }
    }

    /**
     * Test next from Model page
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "navigate to analysis page from model page", dependsOnMethods = "testParameters")
    public void testModelPage() throws CreateAnalysisTestException {
        try {
            analysisPage = modelPage.next(MLDataset.getDatasetName() + "-" + MLDataset.getVersion());
            analysisPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("create.model")));
            Assert.assertTrue(analysisPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("create.model"))),
                    "Did not redirect to Analysis page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Analysis Page: ", e);
        }
    }

    /**
     * Test create model from an existing analysis
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "create model from an existing analysis", dependsOnMethods = "testModelPage")
    public void testCreateModelFromExistingAnalysis() throws CreateAnalysisTestException {
        try {
            modelPage = analysisPage.createModel();
            modelPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("model.page.title")));
            Assert.assertTrue(modelPage.isElementPresent(By.xpath(mlUIElementMapper.getElement("model.page.title"))),
                    "Did not redirect to Model page");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Model Page: ", e);
        }
    }

    /**
     * Test for disable input fields when a model is created from an existing analysis
     *
     * @throws org.wso2.carbon.ml.integration.ui.test.exceptions.CreateAnalysisTestException
     */
    @Test(groups = "wso2.ml.ui", description = "disable input fields when a model is created from an existing analysis",
            dependsOnMethods = "testCreateModelFromExistingAnalysis")
    public void testForDisabledInputFields() throws CreateAnalysisTestException {
        try {
            parametersPage = modelPage.previous();
            parametersPage.sleepTillPageFound(By.xpath(mlUIElementMapper.getElement("parameters.page.title")));
            Assert.assertFalse(parametersPage.isEnabled(By.xpath(mlUIElementMapper.getElement("parameters.iterations"))),
                    "Parameters-Iterations input field is not disabled");
            Assert.assertFalse(parametersPage.isEnabled(By.xpath(mlUIElementMapper.getElement("parameters.learning.rate"))),
                    "Parameters-Learning-rate input field is not disabled");
            Assert.assertFalse(parametersPage.isEnabled(By.xpath(mlUIElementMapper.getElement("parameters.data.fraction"))),
                    "Parameters-Data-fraction field is not disabled");
        }  catch (InvalidPageException e) {
            throw new CreateAnalysisTestException("Failed to navigate to Parameters Page: ", e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }

}
