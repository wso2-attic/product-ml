/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ml.model.spark.transformations;

import org.apache.spark.api.java.function.Function;
import org.wso2.carbon.ml.model.exceptions.ModelServiceException;
import org.wso2.carbon.ml.model.internal.constants.MLModelConstants;

import java.util.List;

/**
 * A filter to remove discarded rows - Impute Option: Discard
 */
public class DiscardedRowsFilter implements Function<String[], Boolean> {

    private List<Integer> indices;

    public DiscardedRowsFilter(List<Integer> discardIndices) {
        this.indices = discardIndices;
    }

    @Override
    public Boolean call(String[] tokens) throws Exception {
        try {
            Boolean keep = true;
            for (Integer index : indices) {
                if (MLModelConstants.EMPTY.equals(tokens[index]) || MLModelConstants.NA.equals
                        (tokens[index])) {
                    keep = false;
                    break;
                }
            }
            return keep;
        } catch (Exception e) {
            throw new ModelServiceException("An error occured while removing discarded rows: " + e.getMessage(), e);
        }
    }
}
