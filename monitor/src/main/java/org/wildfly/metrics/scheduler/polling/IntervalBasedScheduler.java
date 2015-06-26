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

import com.codahale.metrics.Timer;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.wildfly.metrics.scheduler.ModelControllerClientFactory;
import org.wildfly.metrics.scheduler.SchedulerLogger;
import org.wildfly.metrics.scheduler.diagnose.Diagnostics;
import org.wildfly.metrics.scheduler.storage.DataPoint;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.wildfly.metrics.scheduler.polling.Scheduler.State.RUNNING;
import static org.wildfly.metrics.scheduler.polling.Scheduler.State.STOPPED;

/**
 * @author Harald Pehl
 * @author Heiko Braun
 */
public class IntervalBasedScheduler extends AbstractScheduler {

    private final ScheduledExecutorService executorService;
    private final List<ScheduledFuture> jobs;
    private final int poolSize;

    private final ModelControllerClientFactory clientFactory;
    private final Diagnostics monitor;

    private ConcurrentLinkedQueue<ModelControllerClient> connectionPool = new ConcurrentLinkedQueue<>();

    public IntervalBasedScheduler(ModelControllerClientFactory clientFactory, Diagnostics monitor, final int poolSize) {
        this.clientFactory = clientFactory;
        this.monitor = monitor;
        this.poolSize = poolSize;

        this.executorService = Executors.newScheduledThreadPool(poolSize, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                SchedulerLogger.LOGGER.debug("Creating new executor thread");
                return new Thread(r);
            }
        });

        this.jobs = new LinkedList<>();

    }

    @Override
    public void schedule(List<Task> tasks, final CompletionHandler completionHandler) {
        verifyState(STOPPED);

        // optimize task groups
        List<TaskGroup> groups = new IntervalGrouping().apply(tasks);

        SchedulerLogger.LOGGER.infof("Number of tasks: %s", tasks.size());
        SchedulerLogger.LOGGER.infof("Number of task groups: %s", groups.size());

        // populate connection pool
        for (int i = 0; i < poolSize; i++) {
            try {
                connectionPool.add(
                        clientFactory.createClient()
                );
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // schedule IO
        for (TaskGroup group : groups) {
            jobs.add(

                    // schedule tasks
                    executorService.scheduleWithFixedDelay(
                            new IO(group, completionHandler),
                            group.getOffsetMillis(), group.getInterval().millis(),
                            MILLISECONDS
                    )
            );
        }

        pushState(RUNNING);
    }

    @Override
    public void shutdown() {
        verifyState(RUNNING);


        try {
            for (ScheduledFuture job : jobs) {
                job.cancel(false);
            }
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            // cleanup pool
            for (ModelControllerClient client : connectionPool) {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            pushState(STOPPED);
        }
    }

    private class IO implements Runnable {

        private static final String OUTCOME = "outcome";
        private static final String RESULT = "result";
        private static final String FAILURE_DESCRIPTION = "failure-description";
        private static final String SUCCESS = "success";

        private final TaskGroup group;
        private final CompletionHandler completionHandler;
        private final ModelNode operation;

        private IO(TaskGroup group, CompletionHandler completionHandler) {
            this.group = group;
            this.completionHandler = completionHandler;

            // for the IO lifetime the operation is immutable and can be re-used
            this.operation = new ReadAttributeOperationBuilder().createOperation(group);
        }

        @Override
        public void run() {

            if(connectionPool.isEmpty())
                throw new IllegalStateException("Connection pool expired!");
            final ModelControllerClient client = connectionPool.poll();

            try {

                Timer.Context requestContext = monitor.getRequestTimer().time();

                // execute request
                ModelNode response = client.execute(operation);

                long durationMs = requestContext.stop() / 1000000;

                String outcome = response.get(OUTCOME).asString();
                if (SUCCESS.equals(outcome))
                {

                    if (durationMs > group.getInterval().millis()) {
                        monitor.getDelayedRate().mark(1);
                    }

                    List<Property> steps = response.get(RESULT).asPropertyList();
                    assert steps.size() == group.size() : "group structure doesn't match actual response structure";

                    int i=0;
                    for (Property step : steps) {
                        Task task = group.getTask(i);

                        // deconstruct model node
                        ModelNode data = step.getValue();
                        Double value = null;
                        if(task.getSubref()!=null)
                        {
                            value = data.get(RESULT).get(task.getSubref()).asDouble();
                        }
                        else
                        {
                            value = data.get(RESULT).asDouble();
                        }

                        completionHandler.onCompleted(new DataPoint(task, value));
                        i++;
                    }


                } else {
                    monitor.getErrorRate().mark(1);
                    completionHandler.onFailed(new RuntimeException(response.get(FAILURE_DESCRIPTION).asString()));
                }

            } catch (Throwable e) {
                monitor.getErrorRate().mark(1);
                completionHandler.onFailed(e);
            } finally {

                // return to pool
                connectionPool.add(client);

            }
        }

    }

}
