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

package org.wildfly.metrics.extension;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * Definition of a single metric
 * @author Heiko Braun
 */
public class InputDefinition extends PersistentResourceDefinition {

    public static final InputDefinition INSTANCE = new InputDefinition();

    public static final String DATA_INPUT = "data-input";

    static final SimpleAttributeDefinition RESOURCE = new SimpleAttributeDefinitionBuilder("resource", ModelType.STRING,false)
        .build();

    static final SimpleAttributeDefinition ATTRIBUTE  = new SimpleAttributeDefinitionBuilder("attribute", ModelType.STRING,false)
        .build();

    static final SimpleAttributeDefinition SECONDS  = new SimpleAttributeDefinitionBuilder("seconds", ModelType.INT,true)
           .build();

    static final SimpleAttributeDefinition MINUTES  = new SimpleAttributeDefinitionBuilder("minutes", ModelType.INT,true)
               .build();

    static final SimpleAttributeDefinition HOURS  = new SimpleAttributeDefinitionBuilder("hours", ModelType.INT,true)
               .build();

    static AttributeDefinition[] ATTRIBUTES = {
            RESOURCE,
            ATTRIBUTE,
            SECONDS,
            MINUTES,
            HOURS
    };

    private InputDefinition() {
        super(PathElement.pathElement(DATA_INPUT),
            SubsystemExtension.getResourceDescriptionResolver(MonitorDefinition.MONITOR, DATA_INPUT),
            InputAdd.INSTANCE,
            InputRemove.INSTANCE
            );
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        InputWriteAttributeHandler handler = new InputWriteAttributeHandler(ATTRIBUTES);

        for (AttributeDefinition attr : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, handler);
        }

    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }
}
