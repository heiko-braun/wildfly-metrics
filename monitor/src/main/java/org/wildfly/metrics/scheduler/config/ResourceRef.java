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
package org.wildfly.metrics.scheduler.config;

/**
 * A resource reference that is to be monitored.
 *
 * @author Harald Pehl
 */
public class ResourceRef {

    private final String address;
    private final String attribute;
    private final Interval interval;

    public ResourceRef(final String address, final String attribute, final Interval interval) {
        this.address = address;
        this.attribute = attribute;
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "Task(" + address + ":" + attribute + ", " + interval + ")";
    }

    public String getAddress() {
        return address;
    }

    public String getAttribute() {
        return attribute;
    }

    public Interval getInterval() {
        return interval;
    }
}
