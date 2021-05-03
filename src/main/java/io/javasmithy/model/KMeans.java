package io.javasmithy.model;

import io.javasmithy.IO.DataFrame;
import io.javasmithy.math.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class KMeans {
    private int clusters;
    private int maxIterations;
    private double tolerance;
    private int seed;
    private Random random;

    private DataFrame df;
    private String[][] mu;
    private String[] clusterAssignments;

    public KMeans(KMeansBuilder k) {
        this.clusters = k.clusters;
        this.maxIterations = k.maxIterations;
        this.tolerance = k.tolerance;
        this.seed = k.seed;
        this.random = new Random(this.seed);
    }

    public void fit(DataFrame df) {
        // initializations
        this.df = df;
        initializeMu();
        updateClusterAssignments();
        df.addColumn(clusterAssignments);

        //  update steps
        for (int iteration = 0; iteration < maxIterations; iteration++){
            System.out.println("ITERATION: " + iteration);
            updateMu();
            System.out.println(muToString());
            updateClusterAssignments();
            df.setColumn(clusterAssignments, df.getNumCols()-1);
        }


    }
    private void initializeMu(){
        this.mu = new String[clusters][this.df.getNumCols()];
        List<Integer> usedIndexes = new ArrayList<>();
        for (int i = 0; i < clusters; i++){
            int randomRow = -1;
            do {
                randomRow = this.random.nextInt(this.df.getNumRows());
            } while (usedIndexes.contains(randomRow));
            usedIndexes.add(randomRow);
            this.mu[i] = this.df.getRow(randomRow);
        }
    }
    private void updateClusterAssignments(){
        if (this.clusterAssignments == null) this.clusterAssignments = new String[this.df.getNumRows()];
        for(int i = 0; i < df.getNumRows(); i++){
            this.clusterAssignments[i] = ""+findClosestCluster(i);
        }
    }
    private int findClosestCluster(int rowIndex){
        double[] norms = new double[this.clusters];
        for (int i = 0; i < this.clusters; i++){
            norms[i] = Math.norm(this.df.getRow(rowIndex), this.mu[i] );
        }
        return Math.argMin(norms);
    }
    private void updateMu(){
        for(int i = 0; i< mu.length; i++){
            DataFrame df_cluster = df.getRowsByValue(""+i, df.getNumCols()-1);
            mu[i] = df_cluster.allMeans();
        }
    }



    @Override
    public String toString(){
        return "KMeans with " + this.clusters +
                " clusters, " + this.maxIterations +
                " max iterations, a tolerance of " +  this.tolerance +
                " and a random state of " + this.seed +
                "\n Cluster Assignments: " + Arrays.toString(this.clusterAssignments) +
                "\n Mu: " + muToString();
    }
    private String muToString(){
        String muToString = "";
        for (int i = 0; i < mu.length; i++){
            muToString += "\n\t" + Arrays.toString(this.mu[i]);
        }
        return muToString;
    }



    public static class KMeansBuilder {
        private int clusters;
        private int maxIterations;
        private double tolerance;
        private int seed;

        public KMeansBuilder withClusters(int k){
            this.clusters = k;
            return this;
        }

        public KMeansBuilder withMaxIterations(int i){
            this.maxIterations = i;
            return this;
        }

        public KMeansBuilder withTolerance(double t){
            this.tolerance = t;
            return this;
        }

        public KMeansBuilder withSeed(int seed){
            this.seed = seed;
            return this;
        }

        public KMeans build(){
            KMeans model = new KMeans(this);
            return model;
        }

    }
}
