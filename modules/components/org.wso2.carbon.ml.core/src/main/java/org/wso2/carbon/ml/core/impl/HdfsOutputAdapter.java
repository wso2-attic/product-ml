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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.wso2.carbon.ml.core.exceptions.MLOutputAdapterException;
import org.wso2.carbon.ml.core.interfaces.MLOutputAdapter;

/**
 * HDFS based output adapter for ML. Responsible for writing a given input stream to a given HDFS path.
 */
public class HdfsOutputAdapter implements MLOutputAdapter {

    @Override
    public void writeDataset(String outPath, InputStream in) throws MLOutputAdapterException {

        if (in == null || outPath == null) {
            throw new MLOutputAdapterException(String.format(
                    "Null argument values detected. Input stream: %s Out Path: %s", in, outPath));
        }
        if (!outPath.startsWith("hdfs://")) {
            outPath = "hdfs://localhost:9000".concat(outPath);
        }
        FSDataOutputStream out = null;
        try {
            Configuration conf = new Configuration();
            URI uri = URI.create(outPath);
            FileSystem hdfs = FileSystem.get(uri, conf);
            out = hdfs.create(new Path(uri), true);
            IOUtils.copyBytes(in, out, conf);
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
