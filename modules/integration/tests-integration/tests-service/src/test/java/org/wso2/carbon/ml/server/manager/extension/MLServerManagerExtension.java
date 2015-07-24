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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.server.manager.extension;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;

/**
 * This class is responsible for starting ML server after doing the initial configurations and also stopping the ML
 * server. This class is referenced from automation.xml.
 */
public class MLServerManagerExtension extends ExecutionListenerExtension {
    private static final Log log = LogFactory.getLog(MLServerManagerExtension.class);
    private static TestServerManager mlServerManager;

    @Override
    public void initiate() throws AutomationFrameworkException {

        AutomationContext context;
        try {
            context = new AutomationContext("ML", TestUserMode.SUPER_TENANT_ADMIN);
        } catch (XPathExpressionException e) {
            throw new AutomationFrameworkException("Error Initiating Server Information", e);
        }
        // if port offset is not set, setting it to 0
        if (getParameters().get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND) == null) {
            getParameters().put(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND, "0");
        }
        Map<String, String> parameters = getParameters();
        // Informing DAS components not to initialize SparkContext
        parameters.put("-DdisableAnalyticsSparkCtx", "true");

        mlServerManager = new TestServerManager(context, null, parameters) {
            public void configureServer() throws AutomationFrameworkException {

                // path to DAS DBs
                String resourcePath = TestConfigurationProvider.getResourceLocation("ML") + File.separator + "das"
                        + File.separator;
                String targetPath = mlServerManager.getCarbonHome() + File.separator + "repository" + File.separator
                        + "database" + File.separator;
                // copying DAS DBs
                try {
                    FileUtils.copyFileToDirectory(new File(resourcePath + "ANALYTICS_EVENT_STORE.h2.db"), new File(
                            targetPath));
                    FileUtils.copyFileToDirectory(new File(resourcePath + "ANALYTICS_PROCESSED_DATA_STORE.h2.db"),
                            new File(targetPath));
                    FileUtils.copyFileToDirectory(new File(resourcePath + "ANALYTICS_FS_DB.h2.db"),
                            new File(targetPath));
                    log.info("Successfully copied DAS databases");
                } catch (IOException e) {
                    String msg = "Failed to copy DAS databases.";
                    log.error(msg, e);
                    throw new AutomationFrameworkException(msg, e);
                }
            }
        };

    }

    @Override
    public void onExecutionFinish() throws AutomationFrameworkException {
        mlServerManager.stopServer();
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {
        try {
            mlServerManager.startServer();
        } catch (IOException e) {
            throw new AutomationFrameworkException("Error while starting server", e);
        }
    }

}
