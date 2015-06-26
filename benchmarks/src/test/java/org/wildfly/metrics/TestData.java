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

import org.joda.time.DateTime;
import org.wildfly.metrics.storage.MetricStorage;

/**
 * @author Heiko Braun
 * @since 26/06/15
 */
public class TestData {

    public static final String METRIC_NAME = "heap";
    public static final int INTERVAL_SECONDS = 30;
    public final static int NUMBER_OF_DAYS = 7;

    private DateTime offset;

    private final long to;
    private final long from;

    public TestData() {
        offset = new DateTime();
        to = System.currentTimeMillis();
        offset = offset.minusDays(NUMBER_OF_DAYS);
        from = offset.getMillis();
    }

    public void generate(MetricStorage storage) {

        System.out.println("Generate test data, hold on ...");

        storage.registerMetric(METRIC_NAME);

        long numSamples = 0;
        do {
            ++numSamples;

            offset = offset.plusSeconds(INTERVAL_SECONDS);

            double random = Math.random();
            double probe = random *100;
            int measurement = (int)probe;

            storage.addMeasurement(METRIC_NAME, offset.getMillis(), measurement);
        }
        while (offset.getMillis()< to);

        System.out.println("Created "+numSamples+" samples");

    }

    public long getTo() {
        return to;
    }

    public long getFrom() {
        return from;
    }
}
