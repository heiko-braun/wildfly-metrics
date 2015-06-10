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

package org.wildfly.metrics.scheduler.storage;

import org.wildfly.metrics.scheduler.config.Configuration;
import org.wildfly.metrics.scheduler.diagnose.Diagnostics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author Heiko Braun
 * @since 10/06/15
 */
public class H2Storage implements StorageAdapter {

    private Configuration config;
    private Diagnostics diagnostics;
    private Connection connection;

    @Override
    public void init(Configuration config, Diagnostics diagnostics) {

        this.config = config;
        this.diagnostics = diagnostics;

    }

    @Override
    public void start() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");

        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize database connection");
        }
    }

    @Override
    public void store(Set<DataPoint> datapoints) {

    }

    @Override
    public void stop() {
        try {
            if(connection!=null)
                connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }
}
