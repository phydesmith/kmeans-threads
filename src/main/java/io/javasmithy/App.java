package io.javasmithy;


import io.javasmithy.IO.DataFrame;
import io.javasmithy.model.KMeans;

import java.util.Arrays;
import java.util.Random;

public class App {
    public static void main( String[] args ) {
        String[][] data = new String[100000][];
        Random random = new Random(183);


        for(int i = 0; i < data.length; i++){
            double val1 = random.nextInt(100);
            double val2 = random.nextInt(100);
            double val3 = random.nextInt(100);
            double val4 = random.nextInt(100);
            data[i] = new String[] { ""+val1, ""+val2, ""+val3, ""+val4};
        }

        DataFrame df = new DataFrame(data);
        KMeans km = new KMeans.KMeansBuilder()
                .withClusters(15)
                .withMaxIterations(100)
                .withTolerance(.001)
                .withSeed(92834983)
                .build();

        km.fit(df);
        //system.out.println(km);

    }
}
