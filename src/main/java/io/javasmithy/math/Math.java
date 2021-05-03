package io.javasmithy.math;

public class Math {

    public static double norm(String[] p, String[]q){
        double norm = -1;
        if (p.length!=q.length) return norm;
        for (int i = 0; i < p.length; i++){
            double val = Double.parseDouble(p[i]) - Double.parseDouble(q[i]);
            val = val*val;
            norm +=  val;
        }
        java.lang.Math.sqrt(norm);
        return norm;
    }

    public static int argMin(double[] p){
        int mIndex = 0;
        for(int i = 0; i < p.length; i++){
            if (p[i] < p[mIndex]) mIndex = i;
        }
        return  mIndex;
    }
}
