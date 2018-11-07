/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.examples.ml.clustering;

import java.util.Arrays;
import java.util.UUID;
import java.io.*;
import java.util.*;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.ml.dataset.impl.cache.CacheBasedDatasetBuilder;
import org.apache.ignite.ml.knn.classification.KNNClassificationTrainer;
import org.apache.ignite.ml.math.Tracer;
import org.apache.ignite.ml.math.impls.vector.DenseLocalOnHeapVector;
import org.apache.ignite.ml.clustering.kmeans.KMeansModel;
import org.apache.ignite.ml.clustering.kmeans.KMeansTrainer;
import org.apache.ignite.thread.IgniteThread;
import org.apache.ignite.configuration.*;
/**
 * Run kNN multi-class classification trainer over distributed dataset.
 *
 * @see KNNClassificationTrainer
 */
public class KMeansClusterizationExample {

    static IgniteCache<Integer, double[]> dataCache;
    static String[] files = {"xaa", "xab", "xac", "xad", "xae", "xaf", "xag", "xah", "xai", "xaj"};
    static int index;

    /** Run example. */
    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        System.out.println(">>> KMeans clustering algorithm over cached dataset usage example started.");
        // Start ignite grid.
        IgniteConfiguration igniteConfiguration = Ignition.loadSpringBean("examples/config/example-ignite.xml",
             "ignite.cfg");
        try (Ignite ignite = Ignition.getOrStart(igniteConfiguration)) {
            System.out.println(">>> Ignite grid started.");

            IgniteThread igniteThread = new IgniteThread(ignite.configuration().getIgniteInstanceName(),
                KMeansClusterizationExample.class.getSimpleName(), () -> {
                //IgniteCache<Integer, double[]> dataCache = getTestCache(ignite);

                CacheConfiguration<Integer, double[]> cacheConfiguration = new CacheConfiguration<>();
                cacheConfiguration.setName("TEST_" + UUID.randomUUID());
                cacheConfiguration.setAffinity(new RendezvousAffinityFunction(false, 10));
                IgniteCache<Integer, double[]> dataCache = ignite.createCache(cacheConfiguration);
                try {
                    //dataCache = loadData("/home/ubuntu/kmeans-10dim-1000000000points", ignite);
                    loadFiles(ignite);
                    long startTime = System.currentTimeMillis();
                    KMeansTrainer trainer = new KMeansTrainer()
                      .withSeed(7867L);
                    trainer.withK(100);
                    trainer.withMaxIterations(5);
                    KMeansModel mdl = trainer.fit(
                       new CacheBasedDatasetBuilder<>(ignite, dataCache),
                       (k, v) -> Arrays.copyOfRange(v, 1, v.length),
                       (k, v) -> v[0]
                    );
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    System.out.println("elapsedTime is " + elapsedTime);
                }
                catch (java.io.FileNotFoundException e) {
                    System.out.println("can not load /home/ubuntu/kmeans-10dim-1000000000points");
                }

            });

            igniteThread.start();
            igniteThread.join();
        }
    }

    private static void loadFiles (final Ignite ignite) throws FileNotFoundException {
     IgniteThread[] threads={null};
     try {
        for (int i = 0; i < 10; i++) {
             index = i;
             System.out.println(">>>>>> IgniteThread-"+i+" to be started");
             IgniteThread igniteThread = new IgniteThread( ignite.configuration().getIgniteInstanceName(), KMeansClusterizationExample.class.getSimpleName()+i, () -> {
                 
                 try {
                        loadFile(dataCache, files[index], ignite);

                 } catch (Exception e) {
                    System.out.println("Catch exception");
                 }

               }
             );
             threads[i] = igniteThread;
             igniteThread.start();
        }
        for (int i = 0; i < threads.length; i++) {
             threads[i].join();
        }
     } catch (Exception e) {
        System.out.println("Catch exception");
     }
   }



    private static void loadFile (IgniteCache<Integer, double[]> cache, String fileName, Ignite ignite) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(fileName));
        int cnt = 0;
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            String[] cells = row.split(" ");
            double[] data = new double[cells.length];

            for (int i = 0; i < data.length; i++)
                data[i] = Double.valueOf(cells[i]);

            cache.put(cnt++, data);
            if (cnt%10000 == 0) {
                 System.out.println("inserted "+cnt+" lines");
            }
        }
    }

    /**
     * Loads data from csv file
     * @param filename csv file as input.
     * @param ignite Ignite instance.
     * @return Filled Ignite Cache.
     */
    private static IgniteCache<Integer, double[]> loadData(String fileName, Ignite ignite) throws FileNotFoundException {
        CacheConfiguration<Integer, double[]> cacheConfiguration = new CacheConfiguration<>();
        cacheConfiguration.setName("TEST_" + UUID.randomUUID());
        cacheConfiguration.setAffinity(new RendezvousAffinityFunction(false, 10));

        IgniteCache<Integer, double[]> cache = ignite.createCache(cacheConfiguration);

        Scanner scanner = new Scanner(new File(fileName));
        int cnt = 0;
        while (scanner.hasNextLine()) {
            String row = scanner.nextLine();
            String[] cells = row.split(" ");
            double[] data = new double[cells.length];

            for (int i = 0; i < data.length; i++)
                data[i] = Double.valueOf(cells[i]);

            cache.put(cnt++, data);
            if (cnt%10000 == 0) {
                 System.out.println("inserted "+cnt+" lines");
            }
        }
       return cache;

    }

    /**
     * Fills cache with data and returns it.
     *
     * @param ignite Ignite instance.
     * @return Filled Ignite Cache.
     */
    private static IgniteCache<Integer, double[]> getTestCache(Ignite ignite) {
        CacheConfiguration<Integer, double[]> cacheConfiguration = new CacheConfiguration<>();
        cacheConfiguration.setName("TEST_" + UUID.randomUUID());
        cacheConfiguration.setAffinity(new RendezvousAffinityFunction(false, 10));

        IgniteCache<Integer, double[]> cache = ignite.createCache(cacheConfiguration);

        for (int i = 0; i < data.length; i++)
            cache.put(i, data[i]);

        return cache;
    }

    /** The Iris dataset. */
    private static final double[][] data = {
        {0, 5.1, 3.5, 1.4, 0.2},
        {0, 4.9, 3, 1.4, 0.2},
        {0, 4.7, 3.2, 1.3, 0.2},
        {0, 4.6, 3.1, 1.5, 0.2},
        {0, 5, 3.6, 1.4, 0.2},
        {0, 5.4, 3.9, 1.7, 0.4},
        {0, 4.6, 3.4, 1.4, 0.3},
        {0, 5, 3.4, 1.5, 0.2},
        {0, 4.4, 2.9, 1.4, 0.2},
        {0, 4.9, 3.1, 1.5, 0.1},
        {0, 5.4, 3.7, 1.5, 0.2},
        {0, 4.8, 3.4, 1.6, 0.2},
        {0, 4.8, 3, 1.4, 0.1},
        {0, 4.3, 3, 1.1, 0.1},
        {0, 5.8, 4, 1.2, 0.2},
        {0, 5.7, 4.4, 1.5, 0.4},
        {0, 5.4, 3.9, 1.3, 0.4},
        {0, 5.1, 3.5, 1.4, 0.3},
        {0, 5.7, 3.8, 1.7, 0.3},
        {0, 5.1, 3.8, 1.5, 0.3},
        {0, 5.4, 3.4, 1.7, 0.2},
        {0, 5.1, 3.7, 1.5, 0.4},
        {0, 4.6, 3.6, 1, 0.2},
        {0, 5.1, 3.3, 1.7, 0.5},
        {0, 4.8, 3.4, 1.9, 0.2},
        {0, 5, 3, 1.6, 0.2},
        {0, 5, 3.4, 1.6, 0.4},
        {0, 5.2, 3.5, 1.5, 0.2},
        {0, 5.2, 3.4, 1.4, 0.2},
        {0, 4.7, 3.2, 1.6, 0.2},
        {0, 4.8, 3.1, 1.6, 0.2},
        {0, 5.4, 3.4, 1.5, 0.4},
        {0, 5.2, 4.1, 1.5, 0.1},
        {0, 5.5, 4.2, 1.4, 0.2},
        {0, 4.9, 3.1, 1.5, 0.1},
        {0, 5, 3.2, 1.2, 0.2},
        {0, 5.5, 3.5, 1.3, 0.2},
        {0, 4.9, 3.1, 1.5, 0.1},
        {0, 4.4, 3, 1.3, 0.2},
        {0, 5.1, 3.4, 1.5, 0.2},
        {0, 5, 3.5, 1.3, 0.3},
        {0, 4.5, 2.3, 1.3, 0.3},
        {0, 4.4, 3.2, 1.3, 0.2},
        {0, 5, 3.5, 1.6, 0.6},
        {0, 5.1, 3.8, 1.9, 0.4},
        {0, 4.8, 3, 1.4, 0.3},
        {0, 5.1, 3.8, 1.6, 0.2},
        {0, 4.6, 3.2, 1.4, 0.2},
        {0, 5.3, 3.7, 1.5, 0.2},
        {0, 5, 3.3, 1.4, 0.2},
        {1, 7, 3.2, 4.7, 1.4},
        {1, 6.4, 3.2, 4.5, 1.5},
        {1, 6.9, 3.1, 4.9, 1.5},
        {1, 5.5, 2.3, 4, 1.3},
        {1, 6.5, 2.8, 4.6, 1.5},
        {1, 5.7, 2.8, 4.5, 1.3},
        {1, 6.3, 3.3, 4.7, 1.6},
        {1, 4.9, 2.4, 3.3, 1},
        {1, 6.6, 2.9, 4.6, 1.3},
        {1, 5.2, 2.7, 3.9, 1.4},
        {1, 5, 2, 3.5, 1},
        {1, 5.9, 3, 4.2, 1.5},
        {1, 6, 2.2, 4, 1},
        {1, 6.1, 2.9, 4.7, 1.4},
        {1, 5.6, 2.9, 3.6, 1.3},
        {1, 6.7, 3.1, 4.4, 1.4},
        {1, 5.6, 3, 4.5, 1.5},
        {1, 5.8, 2.7, 4.1, 1},
        {1, 6.2, 2.2, 4.5, 1.5},
        {1, 5.6, 2.5, 3.9, 1.1},
        {1, 5.9, 3.2, 4.8, 1.8},
        {1, 6.1, 2.8, 4, 1.3},
        {1, 6.3, 2.5, 4.9, 1.5},
        {1, 6.1, 2.8, 4.7, 1.2},
        {1, 6.4, 2.9, 4.3, 1.3},
        {1, 6.6, 3, 4.4, 1.4},
        {1, 6.8, 2.8, 4.8, 1.4},
        {1, 6.7, 3, 5, 1.7},
        {1, 6, 2.9, 4.5, 1.5},
        {1, 5.7, 2.6, 3.5, 1},
        {1, 5.5, 2.4, 3.8, 1.1},
        {1, 5.5, 2.4, 3.7, 1},
        {1, 5.8, 2.7, 3.9, 1.2},
        {1, 6, 2.7, 5.1, 1.6},
        {1, 5.4, 3, 4.5, 1.5},
        {1, 6, 3.4, 4.5, 1.6},
        {1, 6.7, 3.1, 4.7, 1.5},
        {1, 6.3, 2.3, 4.4, 1.3},
        {1, 5.6, 3, 4.1, 1.3},
        {1, 5.5, 2.5, 4, 1.3},
        {1, 5.5, 2.6, 4.4, 1.2},
        {1, 6.1, 3, 4.6, 1.4},
        {1, 5.8, 2.6, 4, 1.2},
        {1, 5, 2.3, 3.3, 1},
        {1, 5.6, 2.7, 4.2, 1.3},
        {1, 5.7, 3, 4.2, 1.2},
        {1, 5.7, 2.9, 4.2, 1.3},
        {1, 6.2, 2.9, 4.3, 1.3},
        {1, 5.1, 2.5, 3, 1.1},
        {1, 5.7, 2.8, 4.1, 1.3},
    };
}
