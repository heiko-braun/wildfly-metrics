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

package org.wildfly.metrics.storage;

import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Heiko Braun
 * @since 24/06/15
 */
public class MetricStorage {


    private static final String METRIC_TO_MEASUREMENT = "metric-measurement";
    private static final String MEASUREMENT_TO_METRIC = "measurement-metric";
    private static final String KEY_TO_KEYS = "key_keys";

    private static final String TYPE_METRIC_KEYS = "metric.keys";
    private static final String TYPE_METRIC_KEY = "metric.key";
    private static final String TYPE_MEASUREMENT = "measurement";

    private final PersistentEntityStoreImpl store;

    private EntityId keysId;

    public MetricStorage(String dataDir) {

        EnvironmentConfig config = new EnvironmentConfig();
        config.setManagementEnabled(false); //disable jmx
        Environment environment = Environments.newContextualInstance(dataDir, config);

        store = PersistentEntityStores.newInstance(environment);

        createSchema();
    }

    private void createSchema() {

        store.executeInTransaction(
                txn -> {
                    Entity keys = null;
                    EntityIterable records = txn.getAll(TYPE_METRIC_KEYS);
                    if (records.isEmpty())
                        keys = txn.newEntity(TYPE_METRIC_KEYS);
                    else
                        keys = records.getFirst();

                    MetricStorage.this.keysId = keys.getId();
                }
        );
    }

    /**
     * Retrieve previously created metric names
     * @see #registerMetric(String)
     * @return
     */
    public Set<String> getMetricNames() {

        final Set<String> metricsNames = store.computeInReadonlyTransaction(txn -> {

            Set<String> res = new HashSet<String>();

            Entity keys = txn.getEntity(keysId);
            EntityIterable links = txn.findLinks(TYPE_METRIC_KEY, keys, KEY_TO_KEYS);
            links.forEach(
                    entity -> res.add((String) entity.getProperty("name"))
            );

            return res;
        });

        return metricsNames;
    }

    /**
     * Adds a new metric name.
     * All metric names need to registered before any of the other operations (i.e. queries) are performed.
     *
     * @param metricName
     */
    public void registerMetric(String metricName) {
        store.executeInTransaction(
                txn -> {

                    // prevent duplicates
                    txn.getAll(TYPE_METRIC_KEY).forEach(
                            entity -> {
                                if(entity.getProperty("name").equals(metricName))
                                    throw new RuntimeException("metric name already registered: "+metricName);
                            }
                    );

                    Entity keys = txn.getEntity(keysId);
                    Entity key = txn.newEntity(TYPE_METRIC_KEY);
                    key.setProperty("name", metricName);
                    key.addLink(KEY_TO_KEYS, keys);

                    txn.commit();
                }
        );
    }

    /**
     * Adds a new measurement for a metric
     *
     * @param metricName
     * @param timestamp
     * @param value
     */
    public void addMeasurement(String metricName, long timestamp, long value) {
        store.executeInTransaction(
                txn -> {
                    Entity metric = txn.find(TYPE_METRIC_KEY, "name", metricName).getFirst();

                    final Entity measurement = txn.newEntity(TYPE_MEASUREMENT);
                    measurement.setProperty("timestamp", timestamp);
                    measurement.setProperty("value", value);
                    measurement.addLink(MEASUREMENT_TO_METRIC, metric);

                    txn.commit();
                }
        );
    }

    public List<Long[]> getMeasurements(String metricName, long from, long to) {

        List<Long[]> results = store.computeInReadonlyTransaction(txn -> {
            Entity metric = txn.find(TYPE_METRIC_KEY, "name", metricName).getFirst();
            EntityIterable links = txn.findLinks(TYPE_MEASUREMENT, metric, MEASUREMENT_TO_METRIC);

            // within range
            EntityIterable slice = txn.find(TYPE_MEASUREMENT, "timestamp", from, to);

            List<Long[]> res = new LinkedList<Long[]>();
            slice.intersect(links).forEach(entity -> {
                Long[] tuple = new Long[2];
                tuple[0] = (Long) entity.getProperty("timestamp");
                tuple[1] = (Long) entity.getProperty("value");
                res.add(tuple);
            });

            return res;
        });

        return results;
    }

    public void start() {

    }

    public void stop() {

    }
}
