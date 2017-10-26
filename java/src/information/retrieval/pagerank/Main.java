package information.retrieval.pagerank;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Main {
    
    public static void main(String[] args) throws IOException {
        new Main().readCSV();
    }
    
    private void readCSV() throws IOException {
        long start = System.currentTimeMillis();
        Path p = Paths.get("Datasets\\nips-papers\\paper_authors.csv");
        //String path = "../../../../../../Datasets/nips-papers/paper_authors.csv";
        HashMap<Integer, Set<Integer>> map = new HashMap<>();
        List<String> lines = Files.readAllLines(p);
        
        int i = 0;
        for(String line : lines) {
            // Skip headers.
            if(i == 0) {
                i++;
                continue;
            }
            
            String[] splits = line.split(",");
            
            int paperId;
            int authorId;
            try {
                paperId = Integer.parseInt(splits[1]);
                authorId = Integer.parseInt(splits[2]);
                
                if(map.containsKey(paperId)) {
                    map.get(paperId).add(authorId);
                } else {
                    Set<Integer> set = new HashSet<>();
                    set.add(authorId);
                    map.put(paperId, set);
                }
            } catch(NumberFormatException e) {
                e.printStackTrace();
            }
            
            i++;
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) + " ms. to read csv.");
        
        buildSparseMatrix(map);
    }
    
    private void buildSparseMatrix(HashMap<Integer, Set<Integer>> map) {
        HashMap<Integer, Set<Integer>> coAuthors = new HashMap<>();
        
        // Create from the undirected graph a directed graph, input map
        // is half of the directed graph, but if an author directs to a set
        // of author authors we must also have the other authors point back.
        long start = System.currentTimeMillis();
        for(HashMap.Entry<Integer, Set<Integer>> entry : map.entrySet()) {
            for(Integer i : entry.getValue()) {
                for(Integer j : entry.getValue()) {
                    if(!i.equals(j)) {
                        if(coAuthors.containsKey(i)) {
                            coAuthors.get(i).add(j);
                        } else {
                            Set<Integer> set = new HashSet<>();
                            set.add(j);
                            coAuthors.put(i, set);
                        }
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Time to construct co-author graph " + (end - start) + " ms.");
        
        /**
        int count = 0;
        for(HashMap.Entry<Integer, Set<Integer>> entry : coAuthors.entrySet()) {
            if(count >= 10) {
                break;
            }
            int key = entry.getKey();
            System.out.print(key + ": ");
            for(Integer i : entry.getValue()) {
                System.out.print(i);
                System.out.print(", ");
            }
            System.out.println();
            
            count++;
        }*/
        
        long start2 = System.currentTimeMillis();
        SparseMatrix sp = new SparseMatrix((int) (1.4 * coAuthors.size()), 
                coAuthors.size(), coAuthors.size());
        
        // Construct the sparse matrix.
        for(HashMap.Entry<Integer, Set<Integer>> entry : coAuthors.entrySet()) {
            // Each author in a group is connected to every other author in the
            // group, except itself.
            
            /**
            for(Integer i : group) {
                for(Integer j : group) {
                    if(i.equals(j)) {
                        sp.addValue(j, i, 1d);
                    }
                } 
           }*/
            
           int key = entry.getKey();
           Set<Integer> group = entry.getValue();
           for(Integer i : group) {
               sp.addValue(i, key, 1 / ((double) group.size()));
           }
        }
        long end2 = System.currentTimeMillis();
        System.out.println("Constructed SparseMatrix in " + (end2 - start2) + " ms.");
        
        System.out.println("|N| = " + coAuthors.size());
        
        // Execute PageRank computation.
        // What is a good number of iterations to approximate?
        long start3 = System.currentTimeMillis();
        
        SparseMatrix vector = new SparseMatrix((int) (1.4 * coAuthors.size()), 
                1, coAuthors.size());
        vector.addValue(0, 0, 1d);
        
        System.out.println("Execute PowerMethod");
        vector = sp.powerMethodIteration(vector, 30);
        
        long end3 = System.currentTimeMillis();
        System.out.println("PowerMethod time " + (end3 - start3) + " ms.");
        
        //System.out.println("Print SparseMatrix");
        //sp.printMatrix(sp.toArray());
        List<AuthorRank> authors = new ArrayList<>();
        for(HashMap.Entry<Integer, HashMap<Integer, Double>> entry : vector.getMatrix().entrySet()) {
            int authorId = entry.getKey();
            if(entry.getValue().size() > 1) {
                System.err.println("Vector width > 1");
            }
            
            double rank = entry.getValue().get(0);
            AuthorRank ranking = new AuthorRank();
            ranking.authorId = authorId;
            ranking.rank = rank;
            authors.add(ranking);
        }
        
        Collections.sort(authors, new Comparator<AuthorRank>() {
            @Override
            public int compare(AuthorRank o1, AuthorRank o2) {
                if(o1.rank > o2.rank) {
                    return -1;
                } else if(o1.rank == o2.rank) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        
        List<String> lines = new ArrayList<>();
        for(AuthorRank r : authors) {
            String line = r.authorId + " " + r.rank;
            lines.add(line);
        }
        
        Path out = Paths.get("out.txt");
        try {
            Files.write(out, lines, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Written PageRank.");
    }
    
    private class AuthorRank {
        private int authorId;
        private double rank;
    }
}
