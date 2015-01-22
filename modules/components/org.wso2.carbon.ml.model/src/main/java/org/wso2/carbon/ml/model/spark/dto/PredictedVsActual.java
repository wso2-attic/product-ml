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

package org.wso2.carbon.ml.model.spark.dto;

import java.io.Serializable;

/**
 * DTO class to store predicted vs. actual value
 */
public class PredictedVsActual implements Serializable {
    private double predicted;
    private double actual;

    /**
     * @return Returns predicted value
     */
    public double getPredicted() {
        return predicted;
    }

    /**
     * @param predicted Sets predicted value
     */
    public void setPredicted(double predicted) {
        this.predicted = predicted;
    }

    /**
     * @return Returns actual value
     */
    public double getActual() {
        return actual;
    }

    /**
     * @param actual Sets actual value
     */
    public void setActual(double actual) {
        this.actual = actual;
    }
}
