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

import java.util.List;

/**
 * @author Heiko Braun
 * @since 13/10/14
 */
public interface Configuration {

    public enum Diagnostics {STORAGE, CONSOLE};
    public enum Storage {RHQ, INFLUX, H2}

    /**
     * The host controller host.
     * @return
     */
    String getHost();

    /**
     * The host controller port.
     *
     * @return
     */
    int getPort();

    /**
     * Host controller user
     * @return
     */
    String getUsername();

    /**
     * Host controller password
     * @return
     */
    String getPassword();

    /**
     * Number of threads the scheduler uses to poll for new data.
     *
     * @return
     */
    int getSchedulerThreads();

    /**
     * The resources that are to be monitored.
     * {@link org.wildfly.metrics.scheduler.config.ResourceRef}'s use relative addresses.
     * The core {@link org.wildfly.metrics.scheduler.Service} will resolve it against absolute address within a Wildfly domain.
     *
     * @return
     */
    List<ResourceRef> getResourceRefs();

    Storage getStorageAdapter();

    String getStorageUrl();

    String getStorageUser();

    String getStoragePassword();

    String getStorageDBName();

    String getStorageToken();

    Diagnostics getDiagnostics();

    void addProperty(String name, String value);
    String getProperty(String name);

}
