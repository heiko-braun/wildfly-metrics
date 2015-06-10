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

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

/**
 * The subsystem parser, which uses stax to read and write to and from xml
 *
 * @author Heiko Braun
 */
class SubsystemParser
        implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    public static final SubsystemParser INSTANCE = new SubsystemParser();

    private final static PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(RootDefinition.INSTANCE)
                .addChild(
                        builder(StorageDefinition.INSTANCE)
                                .addAttributes(StorageDefinition.ATTRIBUTES)
                )
                .addChild(

                        builder(MonitorDefinition.INSTANCE)
                                .setXmlElementName(MonitorDefinition.MONITOR)
                                .addAttributes(MonitorDefinition.ATTRIBUTES)
                            .addChild(
                                builder(InputDefinition.INSTANCE)
                                        .setXmlElementName(InputDefinition.DATA_INPUT)
                                        .addAttributes(InputDefinition.ATTRIBUTES)
                            )
                )
                .addChild(
                        builder(DiagnosticsDefinition.INSTANCE)
                                .addAttributes(DiagnosticsDefinition.ATTRIBUTES)
                )
                .build();

    }


    private SubsystemParser() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {

        ModelNode model = new ModelNode();
        model.get(RootDefinition.INSTANCE.getPathElement().getKeyValuePair()).set(context.getModelNode());//this is bit of workaround for SPRD to work properly
        xmlDescription.persist(writer, model, SubsystemExtension.NAMESPACE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        xmlDescription.parse(reader, PathAddress.EMPTY_ADDRESS, list);
    }

}
