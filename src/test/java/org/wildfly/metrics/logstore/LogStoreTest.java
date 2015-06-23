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

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.StoreTransactionalComputable;
import jetbrains.exodus.entitystore.StoreTransactionalExecutable;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Heiko Braun
 * @since 23/06/15
 */
public class LogStoreTest {

    public static final String METRIC_NAME = "jvm.heap.size";
    public static final String SAMPLE_NAME = "measurement";

    private PersistentEntityStoreImpl store;
    private EntityId metricId;

    static DateTimeFormatter FMT= DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");

    /**
     * Initialize a store at 'java.io.tmpdir' and create an example top level entity
     * to record the measurements
     */
    @Before
    public void initializeStore() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        String dataDir = tmpdir + File.pathSeparator + "metrics-data";

        store = PersistentEntityStores.newInstance(
                dataDir
        );

        System.out.println("Data dir: " + dataDir);

        // Create new metric and link samples
        metricId = store.computeInTransaction(
                new StoreTransactionalComputable<EntityId>() {
                    @Override
                    public EntityId compute(@NotNull StoreTransaction txn) {
                        final Entity metric = txn.newEntity(METRIC_NAME);
                        metric.setProperty("name", "JVM heap size in mb");
                        return metric.getId();

                    }
                }
        );

    }

    @After
    public void cleanup() throws Exception {
        if(store!=null)
            store.close();
    }

    /**
     * Insert N samples and make sure they can be read again.
     */
    @Test
    public void testReadWrite() {

        final int NUM_RECORDS = 5;

        // create a sample
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(metricId);
                        List<Entity> timeSeries = createTimeSeries(txn, NUM_RECORDS);
                        for (Entity entry : timeSeries) {
                            metric.addLink("samples", entry);
                        }
                    }
                }
        );

        // read the previously created sample
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(metricId);
                        final Iterable<Entity> samples = metric.getLinks("samples");

                        int i=0;
                        for (Entity sample : samples) {
                            printSample(sample);
                            i++;
                        }

                        Assert.assertEquals("Expected a single record within store", NUM_RECORDS, i);
                    }

                }
        );

    }

    @Test
    public void testSlices() {

        final int PAST_SECONDS = 60; // 60 seconds backwards

        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(metricId);
                        List<Entity> timeSeries = createTimeSeries(txn, PAST_SECONDS);
                        for (Entity entry : timeSeries) {
                            metric.addLink("samples", entry);
                        }
                    }
                }
        );

        // time window
        DateTime dt = new DateTime();
        final DateTime from = dt.minusSeconds(30);
        final DateTime to = dt.minusSeconds(15);

        // read the previously created sample
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(metricId);
                        //Entity samples = metric.getLink(SAMPLE_NAME);

                        EntityIterable slice = txn.find(SAMPLE_NAME, "timestamp", from.getMillis(), to.getMillis());

                        int i=0;
                        for (Entity sample : slice) {
                            printSample(sample);
                            i++;
                        }

                        Assert.assertEquals("Expected 15 records within slice", 15, i);

                    }

                }
        );
    }

    private static List<Entity> createTimeSeries(StoreTransaction txn, int size) {

        List<Entity> results = new LinkedList<>();

        DateTime dt = new DateTime();
        DateTime offset = dt.minusSeconds(size);

        for(int i=0; i<size; i++) {
            double random = Math.random();
            double probe = random *100;
            int heap = (int)probe;
            offset = offset.plusSeconds(1);
            Entity sample = createNewSample(txn, offset.getMillis(), heap);
            results.add(sample);
        }

        return results;
    }

    private static void printSample(Entity item) {
        DateTime timestamp = new DateTime(item.getProperty("timestamp"));
        System.out.println("\tTimestamp: " + FMT.print(timestamp));
        System.out.println("\tValue: " + item.getProperty("value"));
    }

    private static Entity createNewSample(StoreTransaction txn, long timestamp, int heap) {
        final Entity sample = txn.newEntity(SAMPLE_NAME);
        sample.setProperty("timestamp", timestamp);
        sample.setProperty("value", heap);
        return sample;
    }
}
