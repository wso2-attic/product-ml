package org.wso2.carbon.ml.integration.ui.pages.mlui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException;
import org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException;

public class MLDatasetsPage extends MLUIPage {
    private static final Log logger = LogFactory.getLog(MLDatasetsPage.class);

    /**
     * Creates a MLDatasets page
     *
     * @param driver instance of the web driver
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.MLUIPageCreationException
     */
    public MLDatasetsPage(WebDriver driver) throws MLUIPageCreationException {
        super(driver);
    }

    /**
     * Create a new DataImportPage
     * @return
     * @throws org.wso2.carbon.ml.integration.ui.pages.exceptions.InvalidPageException
     */
    public DataImportPage createDataset() throws InvalidPageException {
        try {
            driver.findElement(By.xpath(mlUIElementMapper.getElement("create.new.dataset"))).click();
            return new DataImportPage(driver);
        } catch (MLUIPageCreationException e) {
            throw new InvalidPageException("Failed to create Datasets Page: ", e);
        }
    }
}
