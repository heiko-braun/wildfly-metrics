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

import com.google.common.collect.Iterators;
import org.wildfly.metrics.scheduler.config.Interval;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

/**
 * @author Harald Pehl
 */
public class TaskGroup implements Iterable<Task> {

    private final String id; // to uniquely reference this group
    private final Interval interval; // impacts thread scheduling
    private final long offsetMillis;
    private final LinkedList<Task> tasks;

    public TaskGroup(final Interval interval) {
        this.offsetMillis = 0;
        this.id = UUID.randomUUID().toString();
        this.interval = interval;
        this.tasks = new LinkedList<>();
    }

    public void addTask(Task task) {
        verifyInterval(task);
        tasks.add(task);
    }

    public boolean addTasks(final Collection<? extends Task> collection) {
        for (Task t: collection) {
            verifyInterval(t);
        }
        return tasks.addAll(collection);
    }

    private void verifyInterval(final Task task) {
        if (task.getInterval() != interval) {
            throw new IllegalArgumentException("Wrong interval: Expected " + interval + ", but got " + task.getInterval());
        }
    }

    public int size() {return tasks.size();}

    public boolean isEmpty() {return tasks.isEmpty();}

    @Override
    public Iterator<Task> iterator() {
        return Iterators.unmodifiableIterator(tasks.iterator());
    }

    public String getId() {
        return id;
    }

    public Interval getInterval() {
        return interval;
    }

    public long getOffsetMillis() {
        return offsetMillis;
    }

    public Task getTask(int i) {
        return tasks.get(i);
    }
}
