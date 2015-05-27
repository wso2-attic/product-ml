package org.wso2.carbon.ml.integration.ui.pages.mlui.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;
import org.wso2.carbon.ml.integration.ui.pages.mlui.MLUIPage;

public class AnalysisPage extends MLUIPage {

    private static final Log logger = LogFactory.getLog(AnalysisPage.class);

    /**
     * Creates a Model page
     *
     * @param driver instance of the web driver
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException
     */
    public AnalysisPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }
}
