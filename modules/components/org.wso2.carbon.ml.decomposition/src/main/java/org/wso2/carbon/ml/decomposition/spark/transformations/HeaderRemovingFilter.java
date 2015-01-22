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
package org.wso2.carbon.ml.decomposition.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.wso2.carbon.ml.decomposition.exceptions.DecompositionException;

/**
 * This filter removes the header of a given dataset
 */
public class HeaderRemovingFilter implements Function<String, Boolean> {
    private String header;

    public HeaderRemovingFilter(String header) {
        this.header = header;
    }

    /**
     * This method check whether a given line is header or not
     *
     * @param line Row in the dataset
     * @return
     * @throws DecompositionException
     */
    @Override
    public Boolean call(String line) throws DecompositionException {
        try {
            Boolean isRow = true;
            if (line.equals(this.header)) {
                isRow = false;
            }
            return isRow;
        } catch (Exception e) {
            throw new DecompositionException("An error occurred while removing header row: " + e.getMessage(), e);
        }
    }
}
