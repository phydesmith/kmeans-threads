package io.javasmithy.IO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataFrame {
    String[][] data;
    int numCols;
    int numRows;
    public DataFrame(String[][] data){
        this.data = data;
        this.numCols = data[0].length;
        this.numRows = data.length;
    }

    public double mean(int col){
        double total = 0.0;
        for (int row = 0; row < data.length; row++){
            total += Double.parseDouble(data[row][col]);
        }
        return total/data.length;
    }

    public String[] allMeans(){
        String[] means = new String[numCols];
        for (int col = 0;  col < numCols; col++){
            means[col] = ""+mean(col);
        }
        return means;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return numRows;
    }

    public String[] getRow(int row){
        return this.data[row];
    }

    public void addColumn(String[] newColumn){
        for (int row = 0; row < numRows; row++){
            this.data[row] = Arrays.copyOf(this.data[row], numCols+1);
            this.data[row][numCols]=newColumn[row];
        }
        numCols++;
    }
    public void setColumn(String[] newValues, int column){
        for (int row = 0; row < numRows; row++){
            this.data[row][column]= newValues[row];
        }
    }

    public String head(int rows){
        String output = "";
        for(int row = 0; row<rows; row++){
            output+= "\n\t " + Arrays.toString(this.data[row]);
        }
        return output;
    }

    public DataFrame getRowsByValue(String value, int column){
        List<String[]> rowList = new ArrayList<>();
        for (int i = 0; i < data.length; i++){
            if (data[i][column].equals(value)) rowList.add(data[i]);
        }
        String[][] rows = new String[rowList.size()][numCols];
        for (int i = 0; i < rowList.size(); i++){
            rows[i]=rowList.get(i);
        }
        return new DataFrame(rows);
    }
}
