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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of {@link ResourceRef}s with a unique id.
 *
 * @author Harald Pehl
 */
public class ConfigurationInstance implements Configuration {

    private final List<ResourceRef> resourceRefs;
    private int schedulerThreads = 2;

    private String host;
    private int port;
    private String user;
    private String password;

    private String storageUrl = null;
    private String storageUser = null;
    private String storagePassword = null;
    private String storageDb = null;
    private String storageToken = null;
    private Storage storageAdapter = Storage.INFLUX;

    private Diagnostics diagnostics = Diagnostics.CONSOLE;
    private Map<String, String> props = new HashMap<>();

    public ConfigurationInstance() {
        this("localhost", 9990, new ArrayList<ResourceRef>());
    }

    public ConfigurationInstance(String host, int port) {
        this(host, port, new ArrayList<ResourceRef>());
    }

    public ConfigurationInstance(String host, int port, final List<ResourceRef> resourceRefs) {
        this.host = host;
        this.port = port;
        this.resourceRefs = resourceRefs;
    }

    public List<ResourceRef> getResourceRefs() {
        return Collections.unmodifiableList(resourceRefs);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUsername() {
        return this.user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSchedulerThreads(int schedulerThreads) {
        this.schedulerThreads = schedulerThreads;
    }

    @Override
    public int getSchedulerThreads() {
        return schedulerThreads;
    }

    @Override
    public String getStorageUrl() {
        return this.storageUrl;
    }

    @Override
    public String getStorageUser() {
        return storageUser;
    }

    @Override
    public String getStoragePassword() {
        return storagePassword;
    }

    @Override
    public String getStorageDBName() {
        return storageDb;
    }


    @Override
    public Storage getStorageAdapter() {
        return storageAdapter;
    }

    public void setStorageAdapter(Storage storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public void setStorageUser(String storageUser) {
        this.storageUser = storageUser;
    }

    public void setStoragePassword(String storagePassword) {
        this.storagePassword = storagePassword;
    }

    public void setStorageDb(String storageDb) {
        this.storageDb = storageDb;
    }

    public void addResourceRef(ResourceRef ref) {
        resourceRefs.add(ref);
    }

    public String getStorageDb() {
        return storageDb;
    }

    public String getStorageToken() {
        return storageToken;
    }

    public void setStorageToken(String storageToken) {
        this.storageToken = storageToken;
    }

    public String getUser() {
        return user;
    }

    public Diagnostics getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    @Override
    public void addProperty(String name, String value) {
        this.props.put(name, value);

    }

    @Override
    public String getProperty(String name) {
        return this.props.get(name);
    }
}

