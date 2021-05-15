package io.javasmithy.model;

import io.javasmithy.IO.DataFrame;
import io.javasmithy.math.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import java.lang.Math;

public class KMeans {
    private int clusters;
    private int maxIterations;
    private double tolerance;
    private int seed;
    private Random random;
    private boolean usingThreads;
    private int threadCount;

    private DataFrame df;
    private String[][] mu;
    private String[][] oldMu;
    private String[] clusterAssignments;

    private double silhouetteCoef;

    public KMeans(KMeansBuilder builder) {
        this.clusters = builder.clusters;
        this.maxIterations = builder.maxIterations;
        this.tolerance = builder.tolerance;
        this.seed = builder.seed;
        this.random = new Random(this.seed);
        this.usingThreads = builder.usingThreads;
        if(usingThreads) this.threadCount = builder.threadCount;
        this.silhouetteCoef = -999;
    }

    public String[] getClusterAssignments() {
        return clusterAssignments;
    }

    public double getSilhouetteCoef() {
        if (this.silhouetteCoef == -999) return calcSilhouetteCoeff();
        return silhouetteCoef;
    }

    public void fit(DataFrame df){
        this.df = df;
        if(usingThreads){
            concurrentFit();
        } else {
            serialFit();
        }
    }

    private void concurrentFit(){
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        initializeMu();
        updateClusterAssignments();
        df.addColumn(clusterAssignments);

        //  update steps
        for (int iteration = 0; iteration < maxIterations; iteration++){
            System.out.print("\r ITERATION: " + (iteration+1));
            updateMuWithThreads(pool);
            updateClusterAssignmentsWithThreads(pool);
            df.setColumn(clusterAssignments, df.getNumCols()-1);
            if (checkConvergance()) {
                System.out.print("\r Converged at iteration " + (iteration+1) + ".");
                break;
            }
        }

        //  final steps
        pool.shutdown();
    }

    private void updateMuWithThreads(ExecutorService pool){
        updateOldMu();
        try {
            for(int i = 0; i< mu.length; i++){
                mu[i] = pool.submit(new MuUpdate(i, this.df)).get();
            }
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
    }
    private class MuUpdate implements Callable<String[]> {
        private int i;
        private DataFrame df;

        private MuUpdate(int i, DataFrame df){
            this.i = i;
            this.df = df;
        }

        @Override
        public String[] call() throws Exception {
            DataFrame df_cluster = this.df.getRowsByValue(""+i, df.getNumCols()-1);
            return df_cluster.allMeans();
        }
    }

    private void updateClusterAssignmentsWithThreads(ExecutorService pool){
        if (this.clusterAssignments == null) this.clusterAssignments = new String[this.df.getNumRows()];
        int batchSize = df.getNumRows()/10;
        for(int i = 0; i < df.getNumRows(); i += batchSize){
            pool.submit(new ClusterUpdate(i, batchSize, this.clusterAssignments));
        }
    }
    private class ClusterUpdate implements Callable<Boolean>{
        private int startIndex, batchSize;
        private String[] clusterAssignments;

        private ClusterUpdate(int startIndex, int batchSize, String[] clusterAssignments ){
            this.startIndex = startIndex;
            this.batchSize = batchSize;
            this.clusterAssignments = clusterAssignments;
        }

        @Override
        public Boolean call() throws Exception {
            for (int i = startIndex; i < i+batchSize; i++){
                this.clusterAssignments[i] = ""+findClosestCluster(i);
            }
            return true;
        }
    }


    public void serialFit() {
        // initializations
        initializeMu();
        updateClusterAssignments();
        df.addColumn(clusterAssignments);

        //  update steps
        for (int iteration = 0; iteration < maxIterations; iteration++){
            System.out.print("\r ITERATION: " + (iteration+1));
            updateMu();
            updateClusterAssignments();
            df.setColumn(clusterAssignments, df.getNumCols()-1);
            if (checkConvergance()) {
                System.out.print("\r Converged at iteration " + (iteration+1) + ".");
                break;
            }
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
            norms[i] = Tools.norm(this.df.getRow(rowIndex), this.mu[i] );
        }
        return Tools.argMin(norms);
    }
    private void updateMu(){
        updateOldMu();
        for(int i = 0; i< mu.length; i++){
            DataFrame df_cluster = df.getRowsByValue(""+i, df.getNumCols()-1);
            mu[i] = df_cluster.allMeans();
        }
    }
    private void updateOldMu(){
        this.oldMu = new String[this.mu.length][];
        for (int i = 0; i < this.mu.length;i++){
            this.oldMu[i] = Arrays.copyOf(this.mu[i], this.mu[i].length);
        }
    }

    private boolean checkConvergance(){
        double[] oldMuSums = new double[this.oldMu[0].length];
        double[] newMuSums = new double[this.mu[0].length];

        for (int column = 0; column < this.oldMu[0].length; column++){
            for (int row = 0; row < this.oldMu.length; row++){
                oldMuSums[column] += Double.parseDouble(this.oldMu[row][column]);
                newMuSums[column] += Double.parseDouble(this.mu[row][column]);
            }
            oldMuSums[column] = oldMuSums[column]/this.clusters;
            newMuSums[column] = newMuSums[column]/this.clusters;
        }
        //System.out.println("OLD: " + Arrays.toString(oldMuSums));
        //System.out.println("NEW: " + Arrays.toString(newMuSums));

        double oldMuTotal = 0;
        double newMuTotal = 0;

        for (int i = 0; i < oldMuSums.length; i++){
            oldMuTotal += oldMuSums[i];
            newMuTotal += newMuSums[i];
        }

        //System.out.println("OLD TOTAL: " + oldMuTotal);
        //System.out.println("NEW TOTAL: " + newMuTotal);

        double diff = (oldMuTotal/newMuTotal) / 1000;

        //System.out.println("DIFF: " + diff);

        return diff <= this.tolerance;

    }

    private double calcSilhouetteCoeff(){
        int n = this.df.getNumRows();
        this.silhouetteCoef = calcSilhouettes(
                n,
                calcIntraClassMeanDistances(n),
                calcInterClassMeanDistances(n)
        );
        return this.silhouetteCoef;
    }
    private double[] calcIntraClassMeanDistances(int n) {
        double[] meanDistance = new double[n];
        for (int i = 0; i < n; i++) {
            int numAlike = 0;
            for (int j = 0; j < n; j++) {
                if (this.clusterAssignments[i] == this.clusterAssignments[j]) {
                    numAlike++;
                    meanDistance[i] += Tools.norm(this.df.getRow(i), this.df.getRow(j));
                }
            }
            meanDistance[i] /= numAlike;

        }
        return  meanDistance;
    }
    private double[] calcInterClassMeanDistances(int n) {
        double[] meanDistance = new double[n];
        for (int i = 0; i < n; i++) {
            int numAlike = 0;
            for (int j = 0; j < n; j++) {
                if (this.clusterAssignments[i] != this.clusterAssignments[j]) {
                    numAlike++;
                    meanDistance[i] += Tools.norm(this.df.getRow(i), this.df.getRow(j));
                }
            }
            meanDistance[i] /= numAlike;

        }
        return  meanDistance;
    }
    private double calcSilhouettes(int n, double[] intra, double[] inter){
        double meanSilhouette = 0;
        for(int i = 0; i < n; i++){
            meanSilhouette = (intra[i]/inter[i]) / Math.max(intra[i], inter[i]);
        }
        meanSilhouette/=n;
        return meanSilhouette;
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
        private boolean usingThreads;
        private int threadCount;

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

        public KMeansBuilder usingThreads(int n){
            this.usingThreads = true;
            this.threadCount = n;
            return this;
        }

        public KMeans build(){
            KMeans model = new KMeans(this);
            return model;
        }

    }
}
