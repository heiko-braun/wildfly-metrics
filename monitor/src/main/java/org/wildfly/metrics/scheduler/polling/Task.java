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

package org.wildfly.metrics.scheduler.polling;

import org.wildfly.metrics.scheduler.config.Address;
import org.wildfly.metrics.scheduler.config.Interval;

/**
 * Represents a monitoring task. Represents and absolute address within a domain.
 *
 * @author Heiko Braun
 * @since 10/10/14
 */
public class Task {

    private final String host;
    private final String server;
    private final Address address;
    private final String attribute;
    private final String subref;
    private final Interval interval;

    public Task(
            String host, String server,
            Address address,
            String attribute,
            String subref,
            Interval interval
    ) {
        this.host = host;
        this.server = server;
        this.address = address;
        this.attribute = attribute;
        this.subref = subref;
        this.interval = interval;
    }

    public Address getAddress() {
        return address;
    }

    public String getAttribute() {
        return attribute;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getSubref() {
        return subref;
    }

    public String getHost() {
        return host;
    }

    public String getServer() {
        return server;
    }

    @Override
    public String toString() {
        return "Task{" +
                "address=" + address +
                ", attribute='" + attribute + '\'' +
                '}';
    }
}
