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
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
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
 * A set of test that demonstrated how to use the xodus API to cover the metrics use cases.
 * @author Heiko Braun
 * @since 23/06/15
 */
public class LogStoreTest {

    public static final String TYPE_HEAP = "jvm.heap.size";
    public static final String TYPE_THREADS = "jvm.threads.size";
    public static final String TYPE_MEASUREMENT = "measurement";
    private static final String LINK_SAMPLES = "samples";

    private PersistentEntityStoreImpl store;
    private EntityId threadMetrics;
    private EntityId heapMetrics;

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

        heapMetrics = store.computeInTransaction(
                new StoreTransactionalComputable<EntityId>() {
                    @Override
                    public EntityId compute(@NotNull StoreTransaction txn) {
                        final Entity metric = txn.newEntity(TYPE_HEAP);
                        metric.setProperty("name", "JVM heap size in mb");
                        return metric.getId();

                    }
                }
        );

        threadMetrics = store.computeInTransaction(
                new StoreTransactionalComputable<EntityId>() {
                    @Override
                    public EntityId compute(@NotNull StoreTransaction txn) {
                        final Entity metric = txn.newEntity(TYPE_THREADS);
                        metric.setProperty("name", "JVM thread usage");
                        return metric.getId();
                    }
                }
        );
    }

    @After
    public void cleanup() throws Exception {

        store.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull StoreTransaction txn) {
                txn.getEntity(heapMetrics).delete();
                txn.getEntity(threadMetrics).delete();
            }
        });


        if(store!=null)
            store.close();
    }

    /**
     * Insert N samples and make sure they can be read again.
     *
     * Demonstrate some of the navigability constraints imposed by the way the link are created.
     */
    @Test
    public void testReadWrite() {

        final int NUM_RECORDS = 5;

        // create a sample
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(heapMetrics);
                        List<Entity> timeSeries = createTimeSeries(txn, NUM_RECORDS);
                        for (Entity entry : timeSeries) {
                            entry.addLink(LINK_SAMPLES, metric);  // sample linked to metric
                        }
                    }
                }
        );

        // read the previously created sample
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(heapMetrics);
                        final Iterable<Entity> samples = txn.findLinks(TYPE_MEASUREMENT, metric, LINK_SAMPLES);

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

    /**
     * Link impose a navigability constraint as demonstrated below.
     */
    @Test
    public void testNavigability() {

        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(heapMetrics);
                        Entity measurement = createNewSample(txn, System.currentTimeMillis(), 0);

                        // this link does resolve when using txn.findLink() further down
                        metric.addLink("relates-to", measurement);

                        // this one however does
                        measurement.addLink("relates-to", metric);
                    }
                }
        );

        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(heapMetrics);
                        EntityIterable result = txn.findLinks(TYPE_HEAP, metric, "relates-to");
                        Assert.assertEquals("Expected no links from metric to measurement", 0, result.size());

                        EntityIterable result2 = txn.findLinks(TYPE_MEASUREMENT, metric, "relates-to");
                        Assert.assertEquals("Expected links from measurement to metric", 1, result2.size());
                    }
                }
        );

    }

    /**
     * Insert N metric and read a slice of the full data set.
     *
     * This example uses measurement linked to several metric types. The test should verify
     * that only the measurement for a specific metric type is considered when executing the slice query.
     */
    @Test
    public void testSlices() {

        final int PAST_SECONDS = 60; // 60 seconds backwards

        // heap metrics
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(heapMetrics);
                        List<Entity> timeSeries = createTimeSeries(txn, PAST_SECONDS);
                        for (Entity entry : timeSeries) {
                            entry.addLink(LINK_SAMPLES, metric);
                        }
                    }
                }
        );

        // thread metrics
        store.executeInTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(threadMetrics);
                        List<Entity> timeSeries = createTimeSeries(txn, PAST_SECONDS*2);
                        for (Entity entry : timeSeries) {
                            entry.addLink(LINK_SAMPLES, metric);
                        }
                    }
                }
        );

        // time window
        DateTime dt = new DateTime();
        final DateTime from = dt.minusSeconds(30);
        final DateTime to = dt.minusSeconds(15);

        // read the previously created sample
        store.executeInReadonlyTransaction(
                new StoreTransactionalExecutable() {
                    @Override
                    public void execute(@NotNull StoreTransaction txn) {
                        Entity metric = txn.getEntity(heapMetrics);

                        // the number of samples per type needs to match
                        EntityIterable linkedSamples = txn.findLinks(TYPE_MEASUREMENT, metric, LINK_SAMPLES);
                        Assert.assertEquals("Num samples needs to match", PAST_SECONDS, linkedSamples.size());

                        // within range
                        EntityIterable slice = txn.find(TYPE_MEASUREMENT, "timestamp", from.getMillis(), to.getMillis());

                        // TODO: intersection is currently the only way I know of
                        // But it would be much better only load the link types needed.

                        int i = 0;
                        for (Entity sample : slice.intersect(linkedSamples)) {
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

    private static Entity createNewSample(StoreTransaction txn, long timestamp, int value) {
        final Entity sample = txn.newEntity(TYPE_MEASUREMENT);
        sample.setProperty("timestamp", timestamp);
        sample.setProperty("value", value);
        return sample;
    }
}
