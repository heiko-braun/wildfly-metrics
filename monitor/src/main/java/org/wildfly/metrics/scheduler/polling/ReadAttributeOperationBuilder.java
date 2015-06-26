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

import org.jboss.dmr.ModelNode;
import org.wildfly.metrics.scheduler.config.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a {@code read-attribute} operation of the given {@link TaskGroup}.
 *
 * @author Harald Pehl
 */
public class ReadAttributeOperationBuilder implements OperationBuilder {

    @Override
    public ModelNode createOperation(final TaskGroup group) {

        if (group.isEmpty()) {
            throw new IllegalArgumentException("Empty groups are not allowed");
        }

        ModelNode comp = new ModelNode();
        List<ModelNode> steps = new ArrayList<>();
        comp.get("address").setEmptyList();
        comp.get("operation").set("composite");
        for (Task task : group) {
            steps.add(readAttribute(task));
        }
        comp.get("steps").set(steps);

        return comp;

    }

    private ModelNode readAttribute(Task task) {
        ModelNode node = new ModelNode();
        Address address = task.getAddress();
        for (Address.Tuple tuple : address) {
            node.get("address").add(tuple.getKey(), tuple.getValue());
        }
        node.get("operation").set("read-attribute");
        node.get("name").set(task.getAttribute());
        return node;
    }
}
