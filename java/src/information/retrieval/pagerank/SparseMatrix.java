package information.retrieval.pagerank;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A SparseMatrix representation, that does only store non-zero values.
 */
public class SparseMatrix {
    
    public static void main(String[] args) {
        //new SparseMatrix().test();
        SparseMatrix sparse = new SparseMatrix(100, 4, 4);
        sparse.addValue(1, 0, (1/3d));
        sparse.addValue(2, 0, (1/3d));
        sparse.addValue(3, 0, (1/3d));
        
        sparse.addValue(3, 1, 1d);
        
        sparse.addValue(0, 2, (1/3d));
        sparse.addValue(1, 2, (1/3d));
        sparse.addValue(3, 2, (1/3d));
        
        sparse.addValue(0, 3, 0.5);
        sparse.addValue(1, 3, 0.5);
        
        sparse.printMatrix(sparse.toArray());
        
        SparseMatrix vector = new SparseMatrix(100, 1, 4);
        vector.addValue(0, 0, 1d);
        vector = sparse.powerMethodIteration(vector, 3); //1);
        
        sparse.printMatrix(sparse.toArray());
        System.out.println("Vector:");
        sparse.printMatrix(vector.toArray());
        
        double[][] matrix = new double[][] {
            { 0, 0, (1/3d), (1/2d) },
            { (1/3d), 0, (1/3d), (1/2d) },
            { (1/3d), 0, 0, 0 },
            { (1/3d), 1, (1/3d), 0 }
        };
        
        sparse.printMatrix(matrix);
        
        double p = 0.85;
        for(int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix[i].length; j++) {
                matrix[i][j] = matrix[i][j] * 0.85 + (1 - p) / (double) matrix.length;
            }
        }
        
        sparse.printMatrix(matrix);
        
        double[] vec = new double[] { 1, 0, 0, 0 };
        System.out.println("Vector: " + Arrays.toString(vec));
        vec = matrixPowerMethod(matrix, vec);
        
        System.out.println("Result: " + Arrays.toString(vec));
        vec = matrixPowerMethod(matrix, vec);
        System.out.println("Result 2: " + Arrays.toString(vec));
        vec = matrixPowerMethod(matrix, vec);
        System.out.println("Result 2: " + Arrays.toString(vec));        
    }
    
    public static double[] matrixPowerMethod(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for(int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix[i].length; j++) {
                result[i]+= matrix[i][j] * vector[j];
            }
        }
        return result;
    }
    
    private void test() {
        
    }
    
    // Dimensions of the matrix.
    private final int width;
    private final int length;
    private final int initialCapacity;
    
    //private final HashMap<Integer, RowRecord> rows;
    HashMap<Integer, HashMap<Integer, Double>> matrix;

    public SparseMatrix(int capacity, int width, int length) {
        //this.rows = new ArrayList<>(capacity);
        this.initialCapacity = capacity;
        this.matrix = new HashMap<>(capacity);
        this.width = width;
        this.length = length;
    }

    public void addValue(int row, int col, Double value) {
        if(value == null) {
            return;
        }
        
        if(row >= this.length || col >= this.width) {
            //return;
            throw new IllegalArgumentException("row >= max || col >= max for input row=" + row + " and col=" + col);
        }
        
        HashMap<Integer, Double> rowData;
        //int capacity = 5000;
        if(this.matrix.containsKey(row)) {
            rowData = this.matrix.get(row);
        } else {
            // Setting as initiail capacity this to every HashMap is very
            // inefficient since many rows may remain empty.
            rowData = new HashMap<>(); //this.initialCapacity); //(capacity);
            this.matrix.put(row, rowData);
        }
        rowData.put(col, value);
    }
    
    public HashMap<Integer, HashMap<Integer, Double>> getMatrix() {
        return this.matrix;
    }
    
    public SparseMatrix multiplyByVector(SparseMatrix vector) {
        return null;
    }
    
    public SparseMatrix powerMethodIteration(SparseMatrix vector, int loops) {
        for(int i=0; i<loops; i++) {
            System.out.println("Loop: " + i);
            vector = powerMethod(vector);
        }
        return vector;
    }
    
    public SparseMatrix powerMethod(SparseMatrix vector) {
        if(vector.width != 1 || this.length != vector.length) {
            throw new IllegalArgumentException("Invalid vector for power method.");
        }
        
        double probability = 0.85;
        double randomP = 1 - probability;
        
        SparseMatrix resultVector = new SparseMatrix(vector.initialCapacity, 1, this.length);
        
        HashMap<Integer, HashMap<Integer, Double>> vectorMap = vector.getMatrix();
        if(vectorMap == null) {
            System.out.println("Emtpy vector map.");
        }
        
        /**
        for(HashMap.Entry<Integer, HashMap<Integer, T>> entry : vectorMap.entrySet()) {
            int rowIndex = entry.getKey();
            T value = entry.getValue().get(0);
            
            
        }*/
        
        for(HashMap.Entry<Integer, HashMap<Integer, Double>> rowEntry : this.matrix.entrySet()) {
            //int rowIndex = rowEntry.getKey();
            //System.out.println("RowIndex: " + rowIndex);
            
            HashMap<Integer, Double> rowValues = rowEntry.getValue();
            int emptyCells = this.width - rowValues.size();
            
            // Ignore empty rows.
            // TODO is this correct?
            if(rowValues.isEmpty()) {
                System.out.println("Row values is empty.");
                continue;
            }
            
            //System.out.println("EmptyCells: " + emptyCells);
            double result = 0;
            
            /**
            for(HashMap.Entry<Integer, Double> rowCellEntry : rowValues.entrySet()) {
                int columnIndex = rowCellEntry.getKey();
                double value = rowCellEntry.getValue();
                
                System.out.println("(" + rowIndex + "," + columnIndex + ") " + value);
                
                //System.out.println("ColIndex: " + columnIndex);
                //System.out.println("Val: " + value);
                
                if(vectorMap.containsKey(columnIndex)) {
                    Double vectorValue = vectorMap.get(columnIndex).get(0);
                    if(vectorValue != null) {
                        result+= (value * vectorValue);
                    }
                } else {
                    emptyCells--;
                }
                //result+= (value * vectorMap.get(columnIndex).get(0));
            }*/
            
            for(HashMap.Entry<Integer, HashMap<Integer, Double>> rEntry : vectorMap.entrySet()) {
                int rowIndex = rEntry.getKey();
                for(HashMap.Entry<Integer, Double> rowVec : rEntry.getValue().entrySet()) {
                    int colIndex = rowVec.getKey();
                    double value = rowVec.getValue();
                    
                    //System.out.println("Vec (" + rowIndex + "," + colIndex + ") " + value);
                    
                    if(rowValues.containsKey(rowIndex)) {
                        //System.out.println("RowCellVal: " + rowValues.get(rowIndex));
                        result+= (probability * (value * rowValues.get(rowIndex)) + value * (randomP / (double) this.length));
                        
                    } else {
                        result+= (value * (randomP / (double) this.length));
                    }
                    
                    //System.out.println("Result: " + result);
                }
            }
            
            // Add probability.
            //System.out.println("Result: " + result);
            //System.out.println("ResultEmtpyCells: " + emptyCells);
            //result = probability * result + (emptyCells / (double) this.length) * randomP; // + randomP;
            // Store in result vector.
            resultVector.addValue(rowEntry.getKey(), 0, result);
        }
        System.out.println();
        return resultVector;
    }
    
    public double[][] toArray() {
        double[][] matrixArray = new double[this.length][this.width];
        //System.out.println("RowMap: " + this.matrix.size());
        for(HashMap.Entry<Integer, HashMap<Integer, Double>> entry : this.matrix.entrySet()) {
            int rowIndex = entry.getKey();
            //System.out.println(rowIndex + " ColMap: " + entry.getValue().size());
            for(HashMap.Entry<Integer, Double> rowEntry : entry.getValue().entrySet()) {
                int columnIndex = rowEntry.getKey();
                double value = rowEntry.getValue();
                //System.out.println("(" + rowIndex + "," + columnIndex + ") = " + value);
                matrixArray[rowIndex][columnIndex] = value;
            }
        }
        
        return matrixArray;
    }
    
    public void printMatrix(double[][] array) {
        for(int i=0; i<array.length; i++) {
            //System.out.println("RowLength: " + array[i].length);
            for(int j=0; j<array[i].length; j++) {
                System.out.print(array[i][j]);
                
                if(j < array[i].length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    // Only for debugging of small matrices, do not use this on large ones.
    /**
    public void print() {
        int n = this.rowsFrom.size();
        System.out.println("n: " + n);
        double[][] matrix = new double[n][n];
        int index = 0;
        for(List<ColumnRecord> column : rowsFrom) {
            for(ColumnRecord record : column) {
                matrix[record.rowId][index] = record.value;
            }
            index++;
        }

        // Print Matrix.
        for(int row=0; row<matrix.length; row++) {
            for(int column=0; column<matrix.length; column++) {
                System.out.print(matrix[row][column]);

                if(column < matrix.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }*/
    
    /**
     * Used with the ProbabilitySparseMatrix, to store a value in a particular
     * column, it keeps track at which actual row of a regular matrix this
     * value would be seated of the given column position this record is added
     * into the ProbabilitySparseMatrix
     */
    public class ColumnRecord<T> {
        private final int columnId;
        private final T value;
        
        public ColumnRecord(int id, T val) {
            this.columnId = id;
            this.value = val;
        }
    }
    
    /**
    public class RowRecord<T> {
        private final int rowId;
        private List<ColumnRecord<T>> list = null;

        public RowRecord(int rowId) {
            this.rowId = rowId;
        }

        public void addValue(int columnId, T value) {
            ColumnRecord<T> record = new ColumnRecord<>(rowId, value);
            // Insert the record at the right place into the list?
            // Can we use some form of binary insertion?
            // Better use Hashtables?
            
            if(list == null) {
                this.list = new ArrayList<>();
            }
            
            this.list.add(record);
        }
    }*/
    
    public class RowRecord<T> {
        private final int rowId;
        private HashMap<Integer, T> map = null;

        public RowRecord(int rowId) {
            this.rowId = rowId;
        }

        public void addValue(int columnId, T value) {
            //ColumnRecord<T> record = new ColumnRecord<>(rowId, value);
            // Insert the record at the right place into the list?
            // Can we use some form of binary insertion?
            // Better use Hashtables?
            
            map.put(columnId, value);
        }
    }
}