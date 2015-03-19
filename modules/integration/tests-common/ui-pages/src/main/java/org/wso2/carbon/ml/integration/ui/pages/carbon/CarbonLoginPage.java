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

package org.wso2.carbon.ml.integration.ui.pages.carbon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.CarbonUIPageCreationException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;

public class CarbonLoginPage extends CarbonPage {
    private static final Log logger = LogFactory.getLog(CarbonLoginPage.class);

    public CarbonLoginPage(WebDriver driver) throws CarbonUIPageCreationException {
        super(driver);
    }

    /**
     * Logs into the Carbon Console using user credentials.
     *
     * @param userName  Login user name
     * @param password  Login password
     * @return          Reference to Home page
     * @throws          CarbonPageException
     */
    public CarbonHomePage loginAs(String userName, String password) throws InvalidPageException {
        try {
            WebElement userNameField = driver.findElement(By.xpath(carbonUIElementMapper.getElement("login.username")));
            WebElement passwordField = driver.findElement(By.xpath(carbonUIElementMapper.getElement("login.password")));
            userNameField.sendKeys(userName);
            passwordField.sendKeys(password);
            driver.findElement(By.xpath(carbonUIElementMapper.getElement("login.sign.in.button"))).click();
            return new CarbonHomePage(driver);
        } catch (CarbonUIPageCreationException e) {
            throw new InvalidPageException("An error occured while creating a Carbon Home Page: " + e.getMessage(), e);
        }
    }
}
