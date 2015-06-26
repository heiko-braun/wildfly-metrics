/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wildfly.metrics;

import com.sun.japex.JapexDriverBase;
import org.wildfly.metrics.storage.FS;
import org.wildfly.metrics.storage.MetricStorage;

import java.io.File;
import java.util.UUID;

/**
 * @author Heiko Braun
 * @since 26/06/15
 */
public class BenchmarkBase extends JapexDriverBase {

    protected MetricStorage storage;
    private String dataDir;
    protected TestData testData;

    @Override
    public void initializeDriver() {
        dataDir = genStorageName();
        storage = new MetricStorage(dataDir);
        System.out.println("DataDir: " + dataDir);

        testData = new TestData();
        testData.generate(storage);
    }

    @Override
    public void terminateDriver() {
        try {
            FS.removeDir(dataDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String genStorageName() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        return tmpdir + File.pathSeparator + "metrics-data-"+ UUID.randomUUID().toString();
    }

}
