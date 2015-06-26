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

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * @author Harald Pehl
 */
public class Interval{

    public final static Interval EACH_SECOND = new Interval(1, SECONDS);
    public final static Interval TWENTY_SECONDS = new Interval(20, SECONDS);
    public final static Interval EACH_MINUTE = new Interval(1, MINUTES);
    public final static Interval TWENTY_MINUTES = new Interval(20, MINUTES);
    public final static Interval EACH_HOUR = new Interval(1, HOURS);
    public final static Interval FOUR_HOURS = new Interval(4, HOURS);
    public final static Interval EACH_DAY = new Interval(24, HOURS);

    private final int val;
    private final TimeUnit unit;

    public Interval(int val, TimeUnit unit) {
        this.val = val;
        this.unit = unit;
    }

    public long millis() {
        return MILLISECONDS.convert(val, unit);
    }

    public int getVal() {
        return val;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
