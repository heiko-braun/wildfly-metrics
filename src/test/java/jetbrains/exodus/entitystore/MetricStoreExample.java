/**
 * Copyright 2010 - 2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.exodus.entitystore;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public class MetricStoreExample {

    public static final String METRIC_NAME = "jvm.heap.size";
    public static final String SAMPLE_NAME = "measurement";

    public static void main(String[] args) {
        final ClassLoader classLoader = MetricStoreExample.class.getClassLoader();

        //Create or open persistent store under directory "data"
        final PersistentEntityStoreImpl store = PersistentEntityStores.newInstance("data");

        // Create new metric and link samples
        final EntityId blogId = store.computeInTransaction(new StoreTransactionalComputable<EntityId>() {
            @Override
            public EntityId compute(@NotNull StoreTransaction txn) {
                final Entity metric = txn.newEntity(METRIC_NAME);
                metric.setProperty("name", "JVM heap size in mb");

                double random = Math.random();
                for(int i=0;i<10000; i++)
                    try {
                        double probe = random*100;
                        int heap = (int)probe;

                        Entity sample = createNewSample(txn, System.currentTimeMillis(), heap);
                        metric.addLink("samples", sample);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                return metric.getId();
            }
        });

        // Load blog and show posts and print content
        store.executeInTransaction(new StoreTransactionalExecutable() {
            @Override
            public void execute(@NotNull StoreTransaction txn) {
                final EntityIterable metrics = txn.getAll(METRIC_NAME);

                for (Entity sample : metrics) {
                    final Iterable<Entity> blogItems = sample.getLinks("samples");

                    for (Entity item : blogItems) {
                        try {
                            printSample(item);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        });

        // Close store when we are done
        store.close();
    }

    private static void printSample(Entity item) throws IOException {
        System.out.println("\tTimestamp: " + item.getProperty("timestamp"));
        System.out.println("\tValue: " + item.getProperty("value"));
    }


    private static Entity createNewSample(StoreTransaction txn, long timestamp, int heap) throws IOException {
        final Entity sample = txn.newEntity(SAMPLE_NAME);
        sample.setProperty("timestamp", timestamp);
        sample.setProperty("value", heap);
        return sample;
    }
}
