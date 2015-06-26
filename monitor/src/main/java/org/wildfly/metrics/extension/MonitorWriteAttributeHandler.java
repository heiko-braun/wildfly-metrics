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

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.RestartParentWriteAttributeHandler;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.wildfly.metrics.service.MetricsService;

/**
 * Handler that restarts the service on attribute changes
 * @author Heiko Braun
 */
public class MonitorWriteAttributeHandler extends RestartParentWriteAttributeHandler {

    public MonitorWriteAttributeHandler(AttributeDefinition... definitions) {
        super(MonitorDefinition.MONITOR, definitions);
    }

    @Override
    protected void recreateParentService(
            OperationContext context, PathAddress parentAddress, ModelNode parentModel,
            ServiceVerificationHandler verificationHandler) throws OperationFailedException {

    }

    @Override
    protected ServiceName getParentServiceName(PathAddress parentAddress) {
        return MetricsService.SERVICE_NAME.append(parentAddress.getLastElement().getValue());
    }
}
