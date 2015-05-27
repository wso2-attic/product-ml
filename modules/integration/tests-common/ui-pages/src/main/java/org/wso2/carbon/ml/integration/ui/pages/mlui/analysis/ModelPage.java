package org.wso2.carbon.ml.integration.ui.pages.mlui.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLUIPage;

public class ModelPage extends MLUIPage {

    private static final Log logger = LogFactory.getLog(ModelPage.class);

    /**
     * Creates a Model page
     *
     * @param driver instance of the web driver
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException
     */
    public ModelPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }

    /**
     * Create AnalysisPage
     * @return
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException
     */
    public AnalysisPage next() throws InvalidPageException {
        try {
            driver.findElement(By.xpath(mlUIElementMapper.getElement("model.run.button"))).click();
            return new AnalysisPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create Analysis Page: ", e);
        }
    }
}
