package org.wso2.carbon.ml.integration.ui.pages.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

public class PreprocessPage  extends MLUIPage {

    private static final Log logger = LogFactory.getLog(PreprocessPage.class);

    /**
     * Creates a Preprocess page
     *
     * @param driver instance of the web driver
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException
     */
    public PreprocessPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }
}
