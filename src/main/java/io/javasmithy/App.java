package io.javasmithy;


import io.javasmithy.IO.DataFrame;
import io.javasmithy.model.KMeans;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main( String[] args ) {
        final int DATA_SIZE = Integer.parseInt(args[0]);
        final int MAX_ITERATIONS = Integer.parseInt(args[1]);
        final int NUM_THREADS = Integer.parseInt(args[2]);
        final int NUM_CLUSTERS = Integer.parseInt(args[3]);


        String[][] data = new String[DATA_SIZE][];
        Random random = new Random(183);


        for(int i = 0; i < data.length; i++){
            double val1 = random.nextInt(100);
            double val2 = random.nextInt(100);
            double val3 = random.nextInt(100);
            double val4 = random.nextInt(100);
            data[i] = new String[] { ""+val1, ""+val2, ""+val3, ""+val4};
        }


//        System.out.println("\nStarting Serial execution.");
//        long serialStartTime = System.nanoTime();
//        DataFrame df = new DataFrame(data);
//        KMeans km = new KMeans.KMeansBuilder()
//                .withClusters(NUM_CLUSTERS)
//                .withMaxIterations(MAX_ITERATIONS)
//                .withTolerance(.001)
//                .withSeed(1337)
//                .build();
//        km.fit(df);
//        long serialEndTime = System.nanoTime();
//        long serialExecution = TimeUnit.MILLISECONDS.convert( (serialEndTime - serialStartTime), TimeUnit.NANOSECONDS);
//        System.out.println("\nSERIAL EXECUTION: " + serialExecution);
        //System.out.println("SC: " + km.getSilhouetteCoef());
        //System.out.println("Clusters: " + Arrays.toString(km.getClusterAssignments()));

        System.out.println("\nStarting Concurrent execution.");
        long concurrentStartTime = System.nanoTime();
        DataFrame dfThreads = new DataFrame(data);
        KMeans kmThreads = new KMeans.KMeansBuilder()
                .withClusters(NUM_CLUSTERS)
                .withMaxIterations(MAX_ITERATIONS)
                .withTolerance(.001)
                .withSeed(1337)
                .usingThreads( NUM_THREADS )
                .build();
        kmThreads.fit(dfThreads);
        long concurrentEndTime = System.nanoTime();
        long concurrentExecution = TimeUnit.MILLISECONDS.convert( (concurrentEndTime - concurrentStartTime), TimeUnit.NANOSECONDS);
        System.out.println("\nCONCURRENT EXECUTION: " + concurrentExecution);
        //System.out.println("SC: " + kmThreads.getSilhouetteCoef());
        //System.out.println("Clusters: " + Arrays.toString(kmThreads.getClusterAssignments()));

    }
}
