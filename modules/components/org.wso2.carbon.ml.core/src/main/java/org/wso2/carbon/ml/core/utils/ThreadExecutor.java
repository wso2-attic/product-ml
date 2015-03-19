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
package org.wso2.carbon.ml.core.utils;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class can be used to create a thread pool, and hand over new jobs to the pool.
 */
public class ThreadExecutor {
    private ExecutorService executor;

    public ThreadExecutor(Properties configuration) {
        String poolSizeStr = configuration.getProperty(MLConstants.ML_THREAD_POOL_SIZE);
        int poolSize = 50;
        if (poolSizeStr != null) {
            try {
                poolSize = Integer.parseInt(poolSizeStr);
            } catch (Exception ignore) {
                // use the default
            }
        }
        executor = Executors.newFixedThreadPool(poolSize);
    }

    public void execute(Runnable job) {
        executor.execute(job);
    }

    public void executeAll(Runnable[] jobs) {
        for (Runnable job : jobs) {

            executor.execute(job);
        }
    }

    public void shutdown() {
        executor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
