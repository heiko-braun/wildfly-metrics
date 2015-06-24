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

package org.wildfly.metrics.logstore;

import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.metrics.storage.FS;
import org.wildfly.metrics.storage.MetricStorage;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Each test run creates a dedicated logstore instance (file system)
 * and retains it through a run. It will be nuked at the end of the overall test execution.
 *
 * This means you can rely on entities being created through the test,
 * but at the same time you have to be aware of possible conflicts that may occur.
 *
 * @author Heiko Braun
 * @since 24/06/15
 */
public class MetricStoreTest {

    private static MetricStorage storage;
    private static String dataDir;

    @BeforeClass
    public static void init() {
        dataDir = genStorageName();
        storage = new MetricStorage(dataDir);
        System.out.println("DataDir: "+dataDir);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        FS.removeDir(dataDir);
    }

    @NotNull
    private static String genStorageName() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        return tmpdir + File.pathSeparator + "metrics-data-"+ UUID.randomUUID().toString();
    }

    /**
     * Creation of metric names and retrieval
     */
    @Test
    public void testMetricNames() {
        storage.registerMetric("a");
        storage.registerMetric("b");
        storage.registerMetric("c");

        Set<String> metricNames = storage.getMetricNames();

        Assert.assertTrue(metricNames.contains("a"));
        Assert.assertTrue(metricNames.contains("b"));
        Assert.assertTrue(metricNames.contains("c"));

    }

    /**
     * Adding duplicates should fail
     */
    @Test(expected = RuntimeException.class)
    public void testMetricNameDuplicates() {
        storage.registerMetric("d");
        storage.registerMetric("d");
    }

    /**
     * Creation of measurements and retrieval
     */
    @Test
    public void testMeasurements() {

        storage.registerMetric("e");

        storage.addMeasurement("e", 10, sample());
        storage.addMeasurement("e", 20, sample());
        storage.addMeasurement("e", 30, sample());
        storage.addMeasurement("e", 40, sample());

        List<Long[]> results = storage.getMeasurements("e", 15, 35);

        Assert.assertEquals("Expected two results for range query", 2, results.size());
    }

    public static long sample() {
        double random = Math.random();
        double probe = random *100;
        return (int)probe;
    }

}
