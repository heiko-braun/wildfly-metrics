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
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.StoreTransaction;
import jetbrains.exodus.entitystore.StoreTransactionalComputable;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.Map;

import static io.undertow.Handlers.path;

/**
 * Simple HTTP server for load test purposes
 *
 * @author Heiko Braun
 * @since 24/06/15
 */
public class Server {

    public Server() {

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

                                    server.readMetric(
                                            params.get("metric").getFirst(),
                                            Long.valueOf(params.get("from").getFirst()),
                                            Long.valueOf(params.get("to").getFirst())
                                    );
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
        System.out.println("Start server");
    }

    private void stop() {
        System.out.println("Stop server");
    }

    private void readMetric(String metric, long from, long to) {
    }

    private void writeMetric(String metric, Long value) {
        System.out.println("Insert "+metric +" > " + value);

    }
}
