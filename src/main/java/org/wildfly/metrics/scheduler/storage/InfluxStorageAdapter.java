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

package org.wildfly.metrics.scheduler.storage;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.wildfly.metrics.scheduler.config.Configuration;
import org.wildfly.metrics.scheduler.diagnose.Diagnostics;
import org.wildfly.metrics.scheduler.polling.Task;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Pushes the data to Influx.
 *
 * @author Heiko Braun
 * @since 13/10/14
 */
public class InfluxStorageAdapter implements StorageAdapter {

    private InfluxDB influxDB;
    private String dbName;
    private Diagnostics diagnostics;
    private Configuration config;
    private DefaultKeyResolution keyResolution;

    @Override
    public void init(Configuration config, Diagnostics diagnostics) {
        this.config = config;
        this.influxDB = InfluxDBFactory.connect(
                config.getStorageUrl(),
                config.getStorageUser(),
                config.getStoragePassword()
        );
        this.dbName = config.getStorageDBName();

        this.keyResolution = new DefaultKeyResolution();
    }

    @Override
    public void start() {

    }

    @Override
    public void store(Set<DataPoint> datapoints) {

        try {

            Serie[] series = new Serie[datapoints.size()];
            int i=0;
            for (DataPoint datapoint : datapoints) {

                Task task = datapoint.getTask();
                String key = keyResolution.resolve(task);
                Serie dataPoint = new Serie.Builder(key)
                        .columns("datapoint")
                        .values(datapoint.getValue())
                        .build();

                series[i] = dataPoint;
                i++;
            }

            this.influxDB.write(this.dbName, TimeUnit.MILLISECONDS, series);

        } catch (Throwable t) {
            diagnostics.getStorageErrorRate().mark(1);
            t.printStackTrace();
        }

    }

    @Override
    public void stop() {

    }
}
