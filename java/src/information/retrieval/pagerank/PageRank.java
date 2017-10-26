package information.retrieval.pagerank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PageRank {

    private Graph globalGraph = null;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PageRank().readInput();
    }
    
    private double toDecimals(double number, int decimals) {
        number = number * Math.pow(10, decimals);
        number = Math.round(number);
        return (number / Math.pow(10, decimals));
    }
    
    private void printMatrix(double[][] matrix, int decimals) {
        for(int row=0; row<matrix.length; row++) {
            for(int column=0; column<matrix.length; column++) {
                if(decimals > 0) {
                    System.out.print(toDecimals(matrix[row][column], decimals));
                } else {
                    System.out.print(matrix[row][column]);
                }
                if(column < matrix.length - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }
    
    private void readInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a Command:");
        while(scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] parts = line.split(",");
            
            if(parts.length == 1 && this.globalGraph != null) {
                // Print max degree.
                if(parts[0].equals("graphinfo")) {
                    System.out.println("Nodes: " + this.globalGraph.size());
                    System.out.println("Edges: " + this.globalGraph.getEdgesCount());
                    System.out.println("MaxDegree: " + this.globalGraph.getMaxDegree());
                    System.out.println("MaxDegreeNode: " + this.globalGraph.getMaxDegreeNode());
                }
            } else if(parts.length == 2) {
                String command = parts[0];
                if(command.equals("query")) {
                    int nodeId = Integer.parseInt(parts[1]);
                    // Print node degree and neighbours.
                    if(this.globalGraph != null) {
                        this.globalGraph.nodeQuery(nodeId);
                    }
                }
            } else if(parts.length == 3) {
                // Command.
                String command = parts[0];

                if(command.equals("load")) {
                    // Parameters.
                    String path = stripQuotes(parts[1]);
                    int nodes = Integer.parseInt(parts[2]);                    
                    try {
                        this.globalGraph = readGraph(path, nodes);
                        System.out.println("Successfully loaded graph.");
                    } catch (IOException ex) {
                        Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else if(parts.length == 5) {
                // Command.
                String command = parts[0];
                // Parameters.
                String path = stripQuotes(parts[1]);
                int nodes = Integer.parseInt(parts[2]); // Graph size.
                int mode = Integer.parseInt(parts[3]);
                String output = stripQuotes(parts[4]);
                
                if(command.equals("add_edges")) {
                    growEdgesUniformly(path, nodes, mode, new File(output));
                } else if(command.equals("add_nodes")) {
                    growNodesUniformly(path, nodes, mode, new File(output));
                }
            }
        }
    }

    private double[] alternativePowerMethod(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for(int i=0; i<vector.length; i++) {
            for(int j=0; j<vector.length; j++) {
                result[i] = result[i] + matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    /**
    private void powerMethodTest(String path, int nodes) {
        try {
            Graph graph = readGraph(path, nodes);
            System.out.println("Probability Matrix");
            ProbabilitySparseMatrix matrix = graph.toProbablisticModel();
            // Start from A, with initial vector than only has a 1 at index 0.
            double[] columnVector = new double[nodes];
            columnVector[0] = 1;
            // Use power method.
            System.out.println("Power Method");
            int power = 100;
            // power - 1, because only power - 1 operations are needed for 100 operands.
            for(int i=0; i<(power - 1); i++) {
                columnVector = graph.powerMethod(matrix, columnVector);
            }

            // Sort the rank.
            List<Rank> pageRank = new ArrayList<>();
            int id = 1;
            for(double value : columnVector) {
                pageRank.add(new Rank(id, value));
                id++;
            }
            // Sort rank.
            Collections.sort(pageRank);
            // Print results.
            for(Rank rank : pageRank) {
                System.out.println(rank.nodeId + " " + rank.value);
            }
            
            System.out.println("Done");
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/

    private String stripQuotes(String line) {
        for(int i=0; i<line.length(); i++) {
            if(line.charAt(i) != '\"') {
                line = line.substring(i);
                break;
            }
        }
        for(int i=line.length() - 1; i>=0; i--) {
            if(line.charAt(i) != '\"') {
                line = line.substring(0, i + 1);
                break;
            }
        }
        return line;
    }

    /**
     * Class to wrap the results, node id and corresponding rank, and sort
     * the pagerank list on rank in descending order.
     */
    private class Rank implements Comparable<Object> {
        private final int nodeId;
        // The probablistic value after executing random tree walk with power
        // method.
        private final double value;
        
        public Rank(int id, double value) {
            this.nodeId = id;
            this.value = value;
        }

        // Comparable interface is used for sorting by Collections.sort()
        @Override
        public int compareTo(Object obj) {
            Rank other = (Rank) obj;
            
            // Why is <= not allowed?
            if(this.value < other.value) {
                return 1;
            } else if(this.value == other.value) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * Applies (power - 1) endo matrix multiplications to the given matrix,
     * useful to calculate the power result of a matrix.
     * 
     * @param matrix
     * @param power
     * @return 
     */
    private double[][] multiMatrixMultiplication(double[][] matrix, int power) {
        // For # of power operands only power - 1 operations are needed.
        for(int i=0; i<(power - 1); i++) {
            matrix = endoMatrixMultiplication(matrix);
        }
        return matrix;
    }
    
    /**
     * Matrix multiplication on a matrix itself.
     * Running time is O(n^3) this is very slow.
     * 
     * @param matrix
     * @return 
     */
    private double[][] endoMatrixMultiplication(double[][] matrix) {
        double[][] result = new double[matrix.length][matrix.length];
        
        for(int row=0; row<matrix.length; row++) {
            for(int column=0; column<matrix.length; column++) {
                double sum = 0;
                for(int index=0; index<matrix.length; index++) {
                    sum+= matrix[row][index] * matrix[index][column];
                }
                result[row][column] = sum;
            }
        }
        return result;
    }
    
    /**
     * Reads the graph from a specified file.
     * 
     * @param path to text file containing graph
     * @param nodes #nodes in the graph.
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private Graph readGraph(String path, int nodes) throws FileNotFoundException, IOException {
        Graph graph = new Graph(nodes);
        
        File file = new File(path);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line;
        int count = 0;
        while((line = bf.readLine()) != null) {
            String[] parts = line.split(" ");
            if(parts.length != 2) {
                System.err.println("Wrong format: " + line);
            }
            // Each line has the format <nodeId> sp <neighbourId>, the ids
            // are delimited with a space.
            int id = Integer.parseInt(parts[0]);
            int adjacentId = Integer.parseInt(parts[1]);
            graph.addNeighbour(id, adjacentId);
            count++;
        }
        System.out.println("lineCount: " + count);
        
        return graph;
    }
    
    /**
     * Graph representation.
     */
    private class Graph {
        // Counts # of edges in this graph.
        private int edgeCount;
        // Adjacency List.
        List<List<Integer>> adjacency;
        // Keep track of the max degree.
        private int maxDegree;
        // Keep track of the node with the maximal degree. (zero-based)
        private int maxDegreeNodeId;
        
        public Graph(int capacity) {
            this.edgeCount = 0;
            this.maxDegree = 0;
            this.maxDegreeNodeId = 0;
            this.adjacency = new ArrayList<>(capacity);
        }
        
        /**
         * Both id and neighbourId start their id from 1.
         * 
         * @param id
         * @param neighbourId 
         */
        public void addNeighbour(int id, int neighbourId) {
            if(id <= 0 || neighbourId <= 0) {
                System.err.println("addNeighbour, bad IDs");
            }
            
            edgeCount++;
            //System.out.println("Add: " + id + ";" + neighbourId);
            // id - 1, since the nodes start at index 1, while the adjacency
            // list starts from 0.
            // TODO should from neighbourId also 1 be subtracted?
            if((id - 1) < this.adjacency.size()) {
                List<Integer> neighbours = this.adjacency.get(id - 1);
                neighbours.add(neighbourId - 1);
                int degree = neighbours.size();
                // Update max degree.
                updateMaxDegree(id - 1, degree);
            } else {
                // Add new node.
                List<Integer> adjacent = new ArrayList<>();
                adjacent.add(neighbourId - 1);
                this.adjacency.add(adjacent);
                
                // Update max degree.
                updateMaxDegree(id - 1, 1);
            }
        }
        
        // Used to add random edges, and check if the edge already exists.
        // Id, neighbourId both start their indices at 0.
        // Assumes that id already exists in adjacency list.
        public boolean addNeighbourUnique(int id, int neighbourId) {
            // Make sure that we forbid self-loops.
            if(id == neighbourId) {
                return false;
            }
            
            if(id < this.adjacency.size()) { 
                List<Integer> neighbours = this.adjacency.get(id);
                if(neighbours.contains(neighbourId)) {
                    return false;
                }
                // Increment edge count if neighbour node is really added to
                // adjacency list.
                edgeCount++;
                
                // NeighbourId is unique so add it to the list.
                neighbours.add(neighbourId);
                int degree = neighbours.size();
                // Update max degree. Here we must not offset id.
                updateMaxDegree(id, degree);
            } else {
                System.err.println("Node was not added!");
                return false;
            }
            return true;
        }
        
        // For debugging purposes.
        public void printAdjacency() {
            int index = 0;
            for(List<Integer> list : adjacency) {
                System.out.println("Index: " + index);
                for(int neighbour : list) {
                    System.out.println(neighbour);
                }
                index++;
            }
        }
        
        // Classic Matrix representation of graph.
        // Note that the orientation of this representation is different than
        // that of the probability matrix, this matrix you read by row, your
        // read row i, the node with id: (i+1) is connected to each column
        // index in row i that equals 1. Whilst the probability matrix is read
        // column by column.
        public int[][] toMatrix() {
            int n = this.adjacency.size();
            System.out.println("N: " + n);
            int[][] matrix = new int[n][n];
            int id = 0;
            for(List<Integer> list : adjacency) {
                System.out.println("Id: " + id);
                for(int neighbour : list) {
                    System.out.println("Neighbour: " + neighbour);
                    matrix[id][neighbour] = 1;
                }
                id++;
            }
            return matrix;
        }
        
        /**
         * Presents the graph as a matrix with probabilities, without teleport.
         * 
         * @return an initial probability matrix that represents this graph for
         * the RandomTreeWalk.
         */
        public double[][] toProbabilityMatrix() {
            int n = this.adjacency.size();
            System.out.println("N: " + n);
            // Create a squared n x n matrix.
            double[][] matrix = new double[n][n];
            int id = 0;
            for(List<Integer> list : adjacency) {
                double probability = 1 / (double) list.size();
                //System.out.println("Id: " + id + ", probability: " + probability);
                for(int neighbour : list) {
                    //System.out.println("Neighbour: " + neighbour);
                    matrix[neighbour][id] = probability;
                }
                id++;
            }
            return matrix;
        }
        
        /**
         * Represents the graph as a matrix with probabilites but, also adds
         * teleport.
         * 
         * @return probability matrix with teleport for this graph.
         */
        public double[][] toProbabilityMatrixWithTeleport() {
            int n = this.adjacency.size();
            // Create a squared n x n matrix.
            double[][] matrix = new double[n][n];
            int id = 0;
            double p = 0.85; // non-teleport probability.
            for(List<Integer> list : adjacency) {
                // Fill entire column with default values.
                for(int row=0; row<matrix.length; row++) {
                    matrix[row][id] = (1 - p) / (double) n;
                }
                double probability = (p / (double) list.size());
                // Increase the probability of the indices that link to other
                // nodes.
                for(int neighbour : list) {
                    matrix[neighbour][id]+= probability;
                }
                id++;
            }
            return matrix;
        }
        
        /**
         * Creates a Sparse Matrix of the graph with its probabily values.
         * 
         * @return 
         */
        public ProbabilitySparseMatrix toProbablisticModel() {
            ProbabilitySparseMatrix matrix = 
                    new ProbabilitySparseMatrix(this.adjacency.size());
            
            int id = 1;
            for(List<Integer> list : adjacency) {
                double probability = 1 / (double) list.size();
                for(int neighbour : list) {
                    // Offset to neighbour must be added.
                    matrix.addColumnValue(id, neighbour + 1, probability);
                }
                id++;
            }
            return matrix;
        }
        
        /**
         * Assummes that all the neighbours ids, map to nodes that are already
         * in the graph.
         * 
         * @param neighbours
         * @return true if adding this new node succeeded, otherwise false.
         */
        public boolean addNodeList(List<Integer> neighbours) {
            for(int neighbourId : neighbours) {
                if(neighbourId >= size()) {
                    System.err.println("Referencing non-existing neighbours");
                    return false;
                }
            }
            
            // Update edges count.
            this.edgeCount+= (neighbours.size());
            // Add to adjacency list.
            this.adjacency.add(neighbours);
            return true;
        }
        
        /**
         * Updates record of max degree and the specific node that has this
         * maximal degree.
         * @param id node id (zero-based)
         * @param degree 
         */
        private void updateMaxDegree(int id, int degree) {
            // Update max degree if necessary.
            if(degree > this.maxDegree) {
                this.maxDegree = degree;
                this.maxDegreeNodeId = id;
            }
        }
        
        /**
         * Implementation of the power method of an ProbabilitySparseMatrix
         */ 
        public double[] powerMethod(ProbabilitySparseMatrix matrix, double[] columnVector) {
            double[] columnResult = new double[columnVector.length];
            for(int i=0; i<columnVector.length; i++) {
                //System.out.println("i: " + i);
                if(columnVector[i] != 0) {
                    double factor = columnVector[i];
                    // Do the calculations.
                    List<ColumnRecord> column = matrix.getAdjacencyList(i);
                    //System.out.println("Factor: " + factor);
                    for(ColumnRecord record : column) {
                        int index = record.rowId; // Actually is a row id.
                        double value = record.value;
                        //System.out.println("Index: " + index + ", value: " + value);
                        columnResult[index] = columnResult[index] + factor * value;
                        //System.out.println("ColumnResult[" + index + "]: " + columnResult[index]);
                    }
                }
            }
            return columnResult;
        }
        
        public List<Integer> getProportionalToDegreeList() {
            List<Integer> proportionalList = new ArrayList<>(this.edgeCount);
            int id = 0;
            for(List<Integer> list : adjacency) {
                for(int i=0; i<list.size(); i++) {
                    proportionalList.add(id);
                }
                id++;
            }
            return proportionalList;
        }
        
        /**
         * Prints the degree and neighbours of the given node.
         * 
         * @param nodeId, the node to be queried. IDs start from 1 up to and including graph.size().
         */ 
        private void nodeQuery(int nodeId) {
            if(nodeId - 1 < size()) {
                System.out.println("Details for node: " + nodeId);
                List<Integer> neighbours = this.adjacency.get(nodeId - 1);
                System.out.println("Degree: " + neighbours.size());
                System.out.println("InDegree: " + getInDegree(nodeId - 1));
                System.out.println("Neighbours: ");
                int count = 1;
                for(Integer neighbour : neighbours) {
                    // Correct Id.
                    System.out.println(count + ".   " + (neighbour + 1));
                    count++;
                }
            } else {
                System.out.println("Node does not exist in graph.");
            }
        }
        
        /**
         * 
         * @return # of edges in this graph.
         */
        public int getEdgesCount() {
            return this.edgeCount;
        }
        
        /**
         * 
         * @param id node (zero-based)
         * @return 
         */
        public int getInDegree(int id) {
            int degree = 0;
            int count = 0;
            for(List<Integer> list : this.adjacency) {
                // Skip itself.
                if(count == id) {
                    count++;
                    continue;
                }
                // If the list contains id, than it contains it at most once.
                if(list.contains(id)) {
                    degree++;
                }
                
                count++;
            }
            return degree;
        }
        
        /**
         *
         * @return value of highest degree.
         */
        public int getMaxDegree() {
            return this.maxDegree;
        }
        
        /**
         * 
         * @return id of the node with the maximal degree (where node ids are
         * starting from 1, not zero-based)
         */
        public int getMaxDegreeNode() {
            return (this.maxDegreeNodeId + 1);
        }
        
        /**
         * 
         * @return # of nodes in this graph.
         */
        public int size() {
            return this.adjacency.size();
        }
    }
    
    /**
     * Used with the ProbabilitySparseMatrix, to store a value in a particular
     * column, it keeps track at which actual row of a regular matrix this
     * value would be seated of the given column position this record is added
     * into the ProbabilitySparseMatrix
     */
    private class ColumnRecord {
        private final int rowId;
        private final double value;
        
        public ColumnRecord(int id, double val) {
            this.rowId = id;
            this.value = val;
        }
    }
    
    /**
     * A SparseMatrix representation, that does only store non-zero values,
     * should contain the probabilities of the Random Tree Walk, however can
     * actually contain also other values than probabilities.
     */
    private class ProbabilitySparseMatrix {
        List<List<ColumnRecord>> rowsFrom;
        
        public ProbabilitySparseMatrix(int capacity) {
            this.rowsFrom = new ArrayList<>(capacity);
        }
        
        public void addColumnValue(int id, int neighbourId, double value) {
            if(value == 0) {
                System.err.println("Added 0 value for id: " + id + ", id2: " + neighbourId);
                return;
            }
            
            List<ColumnRecord> columnTo;
            // What if record already exists?
            ColumnRecord record = new ColumnRecord(neighbourId - 1, value);
            // Look up if already exists.
            if((id - 1) < this.rowsFrom.size()) {
                columnTo = this.rowsFrom.get(id - 1);
                columnTo.add(record);
            } else {
                // Add new node.
                columnTo = new ArrayList<>();
                columnTo.add(record);
                this.rowsFrom.add(columnTo);
            }
        }
        
        // Returns the list of column records, must be read as from node id (index + 1)
        // we can go to columnrecord.nodeId with probability value columnrecord.value.
        public List<ColumnRecord> getAdjacencyList(int index) {
            return this.rowsFrom.get(index);
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
    }

    /**
     * Power method used to calculate the page rank for a particular matrix,
     * note that we here take actual #power iterations instead of (power - 1), since
     * the first vector is assumed to be the starting vector, indicating from
     * which node we start, so that means we must perform one step more to get 
     * the desired result.
     * 
     * @param matrix
     * @param vector
     * @param power # of times the power method must be applied
     * @return right eigenvector.
     */
    private double[] powerMethod(double[][] matrix, double[] vector, int power) {
        for(int i=0; i<power; i++) {
            vector = alternativePowerMethod(matrix, vector);
        }
        return vector;
    }
    
    private Ranking getGraphRanking(Graph graph, int power, int rankingId) {
        // Calculate new pagerank.
        double[][] matrix = graph.toProbabilityMatrixWithTeleport();
        double[] vector = new double[matrix.length];
        // Start in A.
        vector[0] = 1;
        vector = powerMethod(matrix, vector, power);
        
        return getRanking(vector, rankingId);
    }
    
    /**
     * Add random edges.
     */ 
    private void growEdgesUniformly(String path, int nodes, int mode, File output) {
        try {
            // Grow the graph multiple iterations with a constant number of edges.
            int loops = 10;
            int amount = 500; // # of edges to add.
            
            Graph originalGraph = readGraph(path, nodes);
            System.out.println("Edges: " + originalGraph.edgeCount);
            
            int power = 30;
            // Calculate the baseline ranking.
            Ranking baseLineRanking = getGraphRanking(originalGraph, power, 0);
            
            // Calculate # of loops different rankings for an adjusted graph.
            List<Ranking> rankingList = new ArrayList<>();
            Graph graph = originalGraph;
            for(int i=0; i<loops; i++) {
                // Got a grown graph.
                if(mode == 0) {
                    graph = growGraphEdgesUniformly(graph, amount);
                } else {
                    // Grow proportional to node degree.
                    graph = growGraphEdgesUniformlyProportionalToDegree(graph, amount);
                }
                System.out.println("Edges" + i + ": " + graph.edgeCount);

                // Calculate error rank with respect to the original page rank.
                Ranking ranking = getGraphRanking(graph, power, i+1);
                //double[] errors = ranking.calculateError(baseLineRanking);
                rankingList.add(ranking);
            }
            
            // Output results.
            System.out.println("Output results.");
            outputResults(output, baseLineRanking, rankingList, -1);
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Graph growGraphEdgesUniformly(Graph graph, int amount) {
        Random random = new Random();
        
        while(amount > 0) {
            int nodes = graph.size();
            // Select a random origin node, start indices from 0.
            int i = random.nextInt(nodes);
            int j = random.nextInt(nodes);
            
            // Add edge into graph if not already exists.
            boolean success = graph.addNeighbourUnique(i, j);
            if(success) {
                amount--;
            }
        }
        return graph;
    }
    
    private Graph growGraphEdgesUniformlyProportionalToDegree(Graph graph, int amount) {
        // Build a list of size |Edges|
        List<Integer> proportionalNodes = graph.getProportionalToDegreeList();
        // Pick a random node.
        Random random = new Random();
        // Potentially dangerous, can result in infinite loop if used on a complete graph.
        while(amount > 0) {
            int index = random.nextInt(proportionalNodes.size());
            int nodeId = proportionalNodes.get(index);
            // Add random neighbour for this node.
            int other = random.nextInt(proportionalNodes.size());
            int neighbourId = proportionalNodes.get(other);
            
            // Add edge into graph if it not already exists and creates no
            // self-loop.
            boolean success = graph.addNeighbourUnique(nodeId, neighbourId);
            if(success) {
                amount--;
            }
        }
        return graph;
    }
    
    /**
     * Expands the graph by adding nodes uniformly at random or uniformly at
     * random proportional to node degree.
     * 
     * @param path to read the graph from.
     * @param nodes = size of the graph
     * @param mode 0 = random uniformly, 1 = random uniformly proportional to node degree.
     * @param output, the file to write results to.
     */
    private void growNodesUniformly(String path, int nodes, int mode, File output) {
        try {
            // Grow the graph multiple iterations with a constant number of nodes.
            int loops = 10;
            int amount = 40; // # of nodes to add.
            
            Graph originalGraph = readGraph(path, nodes);
            System.out.println("Nodes: " + originalGraph.size());
            System.out.println("Edges: " + originalGraph.getEdgesCount());
            
            int power = 30;
            // Calculate the baseline ranking.
            Ranking baseLineRanking = getGraphRanking(originalGraph, power, 0);
            
            // Calculate # of loops different rankings for an adjusted graph,
            // with more nodes.
            
            // Store the rankings.
            List<Ranking> rankingList = new ArrayList<>();
            Graph graph = originalGraph;
            for(int i=0; i<loops; i++) {
                // Got a grown graph.
                if(mode == 0) {
                    graph = growGraphNodesUniformly(graph, amount);
                } else {
                    // Grow proportional to node degree.
                    graph = growGraphNodesUniformlyProportionalToDegree(graph, amount);
                }
                System.out.println("Nodes" + i + ": " + graph.size());
                System.out.println("Edges" + i + ": " + graph.getEdgesCount());

                // Calculate error rank with respect to the original page rank.
                Ranking ranking = getGraphRanking(graph, power, i+1);
                //double[] errors = ranking.calculateError(baseLineRanking);
                rankingList.add(ranking);
            }
            
            // Output results.
            System.out.println("Output results.");
            outputResults(output, baseLineRanking, rankingList, -1);
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Graph growGraphNodesUniformly(Graph graph, int amount) {
        Random random = new Random();
        
        // We want the nodes to connect to at least one other node, we do not
        // want completely isolated nodes.
        int minimum = 1;
        while(amount > 0) {
            // Randomly select node degree.
            int degree = Math.max(minimum, random.nextInt(graph.getMaxDegree()));
            
            List<Integer> neighbours = new ArrayList<>();
            while(degree > 0) {
                // Select random neighbours.
                int neighbourId = random.nextInt(graph.size()); // (zero-based id).
                // Watch out if we already add a new node, the graph size grows
                // and random sample space increases we do not want that, we
                // want to add the new node after selecting neighbours.
                if(!neighbours.contains(neighbourId)) {
                    neighbours.add(neighbourId);
                    degree--;
                }
            }
            
            // From here add the new node at once with its neighbours list.
            boolean success = graph.addNodeList(neighbours);
            if(success) {
                amount--;
            }
        }
        return graph;
    }
    
    private Graph growGraphNodesUniformlyProportionalToDegree(Graph graph, int amount) {
        Random random = new Random();
        
        // We want the nodes to connect to at least one other node, we do not
        // want completely isolated nodes.
        int minimum = 1;
        while(amount > 0) {
            // Randomly select node degree.
            int degree = Math.max(minimum, random.nextInt(graph.getMaxDegree()));
            
            // Build a list of size |Edges|
            List<Integer> proportionalNodes = graph.getProportionalToDegreeList();           
            
            List<Integer> neighbours = new ArrayList<>();
            while(degree > 0) {
                int index = random.nextInt(proportionalNodes.size());
                // Select random neighbours.
                int neighbourId = proportionalNodes.get(index);

                // Make sure neighbourId is unique in neighbours list.
                if(!neighbours.contains(neighbourId)) {
                    neighbours.add(neighbourId);
                    degree--;
                }
            }            
            
            // Add the new node with its neighbours list to the graph.
            boolean success = graph.addNodeList(neighbours);
            if(success) {
                amount--;
            }
        }
        return graph;
    }
    
    /**
     * Writes the page rank results to a csv file.
     * 
     * @param file to which the output must be written.
     * @param baseline page rank of the original graph
     * @param rankings page ranks
     * @param limit maximum number of entries to output, starting from top. Use
     * a limit value of -1 to print all entries.
     * @throws IOException 
     */
    private void outputResults(File file, Ranking baseline, List<Ranking> rankings, int limit) throws IOException {
        // Write the rank-based and value-based error as the first row.
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Write the rank-based and value-based error as the first row.
            String seperator = "\t";
            int count = 0;
            for(Ranking ranking : rankings) {
                // Print the rank-based error followed by the value-based error.
                double[] errors = baseline.calculateErrors(ranking, false);
                bw.write(errors[0] + seperator + errors[1]);
                if(count < rankings.size() - 1) {
                    bw.write(seperator);
                }
                count++;
            }
            
            bw.newLine();
            
            int n = baseline.pageRank.size();
            // Only output a selection from the top or all entries.
            if(limit >= 0 && limit < n) {
                n = limit;
            }
            // Write the actual page ranks.
            for(int i=0; i<n; i++) {
                int j = 0;
                for(Ranking ranking : rankings) {
                    Rank rank = ranking.pageRank.get(i);
                    bw.write(rank.nodeId + seperator + rank.value);
                    if(j < rankings.size() - 1) {
                        bw.write(seperator);
                    }
                    j++;
                }
                bw.newLine();
            }
            bw.newLine();
            // Flush data.
            bw.flush();
        }
    }
    
    /**
     * Used as a record with the Ranking class, to access the rank information
     * by querying node id.
     */
    private class RankEntry {
        private final int nodeId;
        private final int rank;
        // Probablistic value after a random tree walk.
        private final double value;
        
        public RankEntry(int id, int rank, double value) {
            this.nodeId = id;
            this.rank = rank;
            this.value = value;
        }
    }
    
    private class Ranking {
        // Initial PageRank list used with sorting, stores the actual
        // ranking order, opposed to pageRankByIndices.
        List<Rank> pageRank;
        
        // Keep a seperate list where we can request rank for any
        // node Id, such that we can easily calculate the difference between
        // old rank and new rank.
        // And also store the value, such that we can calculate value-based
        // error quickly and correctly.
        RankEntry[] pageRankByIndices;
        
        // Identifier for this ranking.
        private final int rankingId;
        
        public Ranking(int rankingId) {
            this.rankingId = rankingId;
            this.pageRank = new ArrayList<>();
        }
        
        private void calculateRank(double[] vector) {
            int id = 1;
            for(double value : vector) {
                // Wrap each value in the Rank wrapper just used for sorting.
                this.pageRank.add(new Rank(id, value));
                id++;
            }
            
            // Sort rank.
            Collections.sort(pageRank);
            
            // Initialize pageRankByIndices.
            pageRankByIndices = new RankEntry[vector.length];
            // After the sorting we know the actual ranks, now store them in
            // an array such that we can access the rank for a particular node
            // by id, this makes it easy to calculate the value based and rank
            // based error.
            
            int actualRank = 1;
            for(Rank rank : pageRank) {
                id = rank.nodeId;
                // offset with -1, to start from index 0.
                pageRankByIndices[id - 1] = new RankEntry(id, actualRank, rank.value);
                actualRank++;
            }
        }
        
        /**
         * Considers this (current) ranking to be the baseline and treats newRanking
         * obviously as the new ranking and calculates the rank-based error
         * and the value-based error between these two rankings.
         * 
         * @param newRanking
         * @return a 2-element array where the first element correspondes to the
         * rank-based error and the second to the value-based error.
         */
        public double[] calculateErrors(Ranking newRanking, boolean debug) {
            double rankBasedError = 0;
            double valueBasedError = 0;
            
            for(int id=0; id<this.pageRankByIndices.length; id++) {
                RankEntry baseline = this.pageRankByIndices[id];
                RankEntry newRank = newRanking.pageRankByIndices[id];
                
                if(baseline.nodeId != newRank.nodeId) {
                    System.err.println("CalculateError: mismatching node ids, id: " 
                            + baseline.nodeId + ", id2: " + newRank.nodeId);
                } else {
                    //System.out.println("Correct NodeID: " + baseline.nodeId);
                }
                
                // Update rank-based error.
                double rankErrorTerm = Math.abs(newRank.rank - baseline.rank) / (double) baseline.rank; //((double) (id + 1));
                if(debug) {
                    System.out.println("RankErrorTerm " + (id + 1) + " : " + rankErrorTerm);
                    System.out.println("Rank difference: " + (newRank.rank - baseline.rank));
                }
                rankBasedError+= rankErrorTerm;
                
                // Update value-based error.
                double valueErrorTerm = Math.abs(newRank.value - baseline.value) / (double) baseline.rank; // ((double) (id + 1));
                valueBasedError+= valueErrorTerm;
            }
            
            return new double[] { rankBasedError, valueBasedError };
        }
    }

    private Ranking getRanking(double[] vector, int rankingId) {
        Ranking ranking = new Ranking(rankingId);
        ranking.calculateRank(vector);
        return ranking;
    }
    
    /**
     * Calculates how many iterations of the power method are needed, before
     * the result converges really closely to the limits. The accuracy/deviation
     * from the limit is defined in the threshold variable.
     * 
     * @param matrix for which convergence with power method must be calculated.
     */
    private void convergence(double[][] matrix) {
        double threshold = 0.000001;
        // Establish vector that starts in A.
        double[] vector = new double[matrix.length];
        vector[0] = 1;
        
        int loops = 1;
        int limit = 1000;
        double[] vectorNew = alternativePowerMethod(matrix, vector);
        while(getMaxDeviation(vector, vectorNew) > threshold && loops < limit) {
            vector = vectorNew; // Assign the latest new right eigen vector to be the old vector.
            vectorNew = alternativePowerMethod(matrix, vector);
            loops++;
        }
        
        System.out.println("Convergence Loops: " + loops);
    }
    
    /**
     * Assumes both sectors are of same length.
     * @param vector
     * @param vectorNew
     * @return 
     */
    private double getMaxDeviation(double[] vector, double[] vectorNew) {
        double max = 0; // Must be between 0 and 1
        for(int i=0; i<vector.length; i++) {
            double deviation = Math.abs(vectorNew[i] - vector[i]);
            if(deviation > max) {
                max = deviation;
            }
        }
        return max;
    }
}
