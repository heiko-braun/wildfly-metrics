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

import org.wildfly.metrics.scheduler.config.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class IntervalGrouping implements TaskGrouping {

    @Override
    public List<TaskGroup> apply(final List<Task> tasks) {

        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return new Long(t1.getInterval().millis()).compareTo(t2.getInterval().millis());
            }
        });


        List<TaskGroup> groups = new ArrayList<>();
        Interval interval = tasks.get(0).getInterval();
        TaskGroup taskGroup = new TaskGroup(interval);
        groups.add(taskGroup);

        for (Task task : tasks) {

            if(!task.getInterval().equals(interval)) {
                // new group
                interval = task.getInterval();
                groups.add(new TaskGroup(task.getInterval()));
            }

            groups.get(groups.size()-1).addTask(task);
        }

        return groups;
    }
}
