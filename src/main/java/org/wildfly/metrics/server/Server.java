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

package org.wildfly.metrics.server;

import io.undertow.Undertow;
import io.undertow.util.Headers;
import org.jetbrains.annotations.NotNull;
import org.wildfly.metrics.storage.FS;
import org.wildfly.metrics.storage.MetricStorage;

import java.io.File;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.undertow.Handlers.path;

/**
 * Simple HTTP server to run load tests
 *
 * @author Heiko Braun
 * @since 24/06/15
 */
public class Server {

    private final String dataDir;
    private final MetricStorage storage;

    public Server() {
        dataDir = genStorageName();
        storage = new MetricStorage(dataDir);

    }

    public static void main(String[] args) throws Exception
    {
        final Server server = new Server();

        Undertow http = Undertow.builder()
                .addHttpListener(4444, "localhost")
                .setHandler(
                        path()
                                .addPrefixPath("/api/put", exchange -> {
                                    Map<String, Deque<String>> params = exchange.getQueryParameters();

                                    server.writeMetric(
                                            params.get("metric").getFirst(),
                                            Long.valueOf(params.get("value").getFirst())
                                    );
                                })

                                .addPrefixPath("/api/get", exchange -> {
                                    Map<String, Deque<String>> params = exchange.getQueryParameters();

                                    List<Long[]> values = server.readMetric(
                                            params.get("metric").getFirst()
                                    );

                                    StringBuffer sb = new StringBuffer();
                                    values.forEach( t -> {
                                        sb.append(t[0]).append(",").append(t[1]).append("\n");
                                    });
                                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                    exchange.getResponseSender().send(sb.toString());
                                })

                                .addPrefixPath("/", exchange -> {
                                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                    exchange.getResponseSender().send("Metric Server");
                                })
                ).build();

        server.start();
        http.start();

        System.out.println("Press enter to exit ...");
        System.in.read();

        http.stop();
        server.stop();
    }

    private void start() {
        System.out.println("Starting server");
        System.out.println("DataDir: "+ dataDir);
    }

    private void stop() {
        try {
            FS.removeDir(dataDir);
            System.out.println("Stopped server");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeMetric(String metric, Long value) {
        if(!storage.getMetricNames().contains(metric))
            storage.registerMetric(metric);
        storage.addMeasurement(metric, System.currentTimeMillis(), value);
    }

    private List<Long[]> readMetric(String metric) {
        long now = System.currentTimeMillis();
        return storage.getMeasurements(metric, 0, now);
    }

    @NotNull
    private static String genStorageName() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        return tmpdir + File.pathSeparator + "metrics-data-"+ UUID.randomUUID().toString();
    }
}
