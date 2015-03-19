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
package org.wso2.carbon.ml.core.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;

/**
 */
public class FileOutputAdapter implements MLOutputAdapter {

    @Override
    public void writeDataset(String outPath, InputStream in) throws MLOutputAdapterException {

        if (in == null || outPath == null) {
            throw new MLOutputAdapterException(String.format(
                    "Null argument values detected. Input stream: %s Out Path: %s", in, outPath));
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(new File(outPath));
            IOUtils.copy(in, out);
        } catch (FileNotFoundException e) {
            throw new MLOutputAdapterException(e);
        } catch (IOException e) {
            throw new MLOutputAdapterException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new MLOutputAdapterException(String.format("Failed to close the output stream of file %s",
                            outPath), e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new MLOutputAdapterException(String.format(
                            "Failed to close the input stream after writing to file %s", outPath), e);
                }
            }
        }
    }

}
