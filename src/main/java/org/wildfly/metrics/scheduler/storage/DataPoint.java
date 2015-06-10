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

import org.wildfly.metrics.scheduler.polling.Task;

/**
 * @author Heiko Braun
 * @since 13/10/14
 */
public final class DataPoint {
    private Task task;
    private long timestamp;
    private double value;

    public DataPoint(Task task, double value) {
        this.task = task;
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public Task getTask() {
        return task;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }
}
