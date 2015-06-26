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
 * Definition of a diagnostics handler
 * @author Heiko Braun
 */
public class DiagnosticsDefinition extends PersistentResourceDefinition {

    public static final DiagnosticsDefinition INSTANCE = new DiagnosticsDefinition();

    public static final String DIAGNOSTICS = "diagnostics";

    static final SimpleAttributeDefinition ENABLED  = new SimpleAttributeDefinitionBuilder("enabled", ModelType.BOOLEAN,false)
        .build();

    static final SimpleAttributeDefinition SECONDS  = new SimpleAttributeDefinitionBuilder("seconds", ModelType.INT,true)
            .build();

    static final SimpleAttributeDefinition MINUTES  = new SimpleAttributeDefinitionBuilder("minutes", ModelType.INT,true)
            .build();

    static AttributeDefinition[] ATTRIBUTES = {
            ENABLED, SECONDS, MINUTES
    };

    private DiagnosticsDefinition() {
        super(PathElement.pathElement(DIAGNOSTICS),
            SubsystemExtension.getResourceDescriptionResolver(DIAGNOSTICS),
            DiagnosticsAdd.INSTANCE,
            DiagnosticsRemove.INSTANCE
            );
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        DiagnosticsWriteAttributeHandler handler = new DiagnosticsWriteAttributeHandler(ATTRIBUTES);

        for (AttributeDefinition attr : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, handler);
        }

    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }
}
