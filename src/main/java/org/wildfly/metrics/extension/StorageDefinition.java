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
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * storage adapter configuration.
 *
 * @author Heiko Braun
 */
public class StorageDefinition extends PersistentResourceDefinition {

    public static final StorageDefinition INSTANCE = new StorageDefinition();

    public static final String STORAGE_ADAPTER = "storage-adapter";

    private StorageDefinition() {
        super(PathElement.pathElement(STORAGE_ADAPTER),
                SubsystemExtension.getResourceDescriptionResolver(STORAGE_ADAPTER),
                StorageAdd.INSTANCE,
                StorageRemove.INSTANCE
        );

    }

    static final SimpleAttributeDefinition URL = new SimpleAttributeDefinitionBuilder("url", ModelType.STRING,false)
            .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setAllowExpression(true)
            .build();

    static final SimpleAttributeDefinition USER = new SimpleAttributeDefinitionBuilder("user", ModelType.STRING,true)
            .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setAllowExpression(true)
            .build();

    static final SimpleAttributeDefinition PASSWORD = new SimpleAttributeDefinitionBuilder("password", ModelType.STRING,true)
            .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .setAllowExpression(true)
            .build();

    static final SimpleAttributeDefinition TOKEN = new SimpleAttributeDefinitionBuilder("token", ModelType.STRING,true)
            .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
            .build();

    static final SimpleAttributeDefinition DB = new SimpleAttributeDefinitionBuilder("db", ModelType.STRING,true)
                .addFlag(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
                .build();


    static final AttributeDefinition[] ATTRIBUTES = {
            URL,
            USER, PASSWORD,
            TOKEN, DB
    };

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {

        StorageWriteAttributeHandler handler = new StorageWriteAttributeHandler(ATTRIBUTES);

        for (AttributeDefinition attr : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, null, handler);
        }
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }


}
