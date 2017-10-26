package information.retrieval.pagerank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 */
public class CitationsParser {
    
    private final String authorsCSV = "Datasets\\nips-papers\\authors.csv";
    private final String papersCSV = "Datasets\\nips-papers\\papers.csv";
    private final String parserTestCSV = "parser_tests.csv";
    
    private final String paperCitationsCSV = "citations\\paper_citations.csv";
    private final String citationsCSV = "citations\\citations.csv";
    
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        //new CitationsParser().parse();
        //new CitationsParser().test();
        //new CitationsParser().citationTest();
        //new CitationsParser().extractReferences();
        //new CitationsParser().verifyIds();
        //new CitationsParser().analysis();
        //new CitationsParser().parsify();
        //new CitationsParser().initialParserTest();
        //new CitationsParser().parserCorrectnessTest();
        
        //new CitationsParser().citationExtractor();
        //new CitationsParser().testQuery();
        
        //new CitationsParser().testFreeCite();
        //new CitationsParser().testMolder();
        //new CitationsParser().queryCitations();
        //new CitationsParser().constructCitationGraphBetter();
        //new CitationsParser().exactMatches();
        
        //new CitationsParser().citationsResults();
        //new CitationsParser().analyzeCitationGraph();
        
        //new CitationsParser().buildGraph();
        
        //new CitationsParser().citationsDuplicates();
        //new CitationsParser().graphStats();
        new CitationsParser().authorsStats();
        
        long end = System.currentTimeMillis();
        
        System.out.println("Total Time: " + (end - start) + " ms.");
    }
    
    private void parse() {
        try {
            CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                    CSVFormat.RFC4180);  
        
            Map<String, Integer> map = parser.getHeaderMap();
            if(map != null) {
                System.out.println("Map " + map.size());
            }
            
            HashMap<String, Integer> paperTitles = readTitles();
            HashMap<Integer, Integer> yearCount = new HashMap<>();
            
            int noReferences = 0;
            int bracketFormat = 0;
            
            int i = 0;
            int width = 0;
            Iterator<CSVRecord> iterator = parser.iterator();
            while(iterator.hasNext()) {
                CSVRecord record = iterator.next();
                int N = record.size();
                if(i > 0 && N != width) {
                    System.err.println("Record Width smaller " + N);
                } else if(i == 0) {
                    width = N;
                    System.out.println("Width: " + width);
                    // Skip the header line.
                    i++;
                    continue;
                }
                
                // Debugging
                if(i > 2) {
                    //break;
                }
                
                //System.out.println("Record #" + record.getRecordNumber());
                
                int paperId = Integer.parseInt(record.get(0));
                String title = record.get(2);
                
                String paperText = record.get(6);
                
                if(!paperText.contains("References") && !paperText.contains("REFERENCES")) {
                    //System.out.println("Record " + i + " with title \"" + title + "\" has no references.");
                    noReferences++;
                } else {
                    // See where the word "references" occurs more than twice.
                    int countReferences = 0;
                    int index = paperText.length() - 1;
                    while(true) {
                        int result = paperText.lastIndexOf("References", index);
                        if(result == -1) {
                            result = paperText.lastIndexOf("REFERENCES", index);
                        }
                        
                        //System.out.println("Result: " + result);
                        //System.out.println(paperText.lastIndexOf("References"));
                        
                        if(result != -1) {
                            countReferences++;
                            index = result - 1;
                        } else {
                            break;
                        }
                    }
                    
                    if(countReferences > 1) {
                        System.err.println("References Count > 1: " + countReferences + " for paper " + paperId);
                    }
                    
                    //System.out.println("Text length: " + paperText.length());
                    
                    // Extract references section and try to parse and construct
                    // graph.
                    int pos = paperText.lastIndexOf("References", paperText.length() - 1);
                    if(pos == -1) {
                        pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
                    }
                    
                    if(pos != -1) {
                        //System.out.println("Pos: " + pos);
                        // Extract section.
                        pos = pos + "references".length() + 1; // + 1 to remove the linefeed.
                        
                        String referenceText = paperText.substring(pos);
                        //System.out.println("ReferenceText: " + referenceText);
                        
                        if(referenceText.charAt(0) == '[') {
                            bracketFormat++;
                        }
                        
                        citationParser(paperId, referenceText, paperTitles);
                    }
                }
                
                int year = Integer.parseInt(record.get(1));
                
                if(yearCount.containsKey(year)) {
                    yearCount.put(year, yearCount.get(year) + 1);
                } else {
                    yearCount.put(year, 1);
                }
                
                /**
                Iterator<String> recordIterator = record.iterator();
                int i = 0;
                while(recordIterator.hasNext()) {
                    String value = recordIterator.next();
                    System.out.println("Field " + i + " Value: " + value);
                    
                    i++;
                }*/
                
                i++;
            }
            
            
            for(HashMap.Entry<Integer, Integer> entry : yearCount.entrySet()) {
                System.out.println("Year: " + entry.getKey() + " Count: " + entry.getValue());
            }
            
            System.out.println("No References " + noReferences + " out of " + i);
            System.out.println("# bracket format " + bracketFormat);
            
            //System.out.println("Total: " + i);
        } catch (IOException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Probably return a Citation object.
     * 
     * @param paperId
     * @param referenceSection
     * @param titles
     */
    private void citationParser(int paperId, String referenceSection, HashMap<String, Integer> titles) {
        System.out.println("CitationParsing for " + paperId);
        
        referenceSection = referenceSection.toLowerCase();
        
        int matches = 0;
        for(HashMap.Entry<String, Integer> entry : titles.entrySet()) {
            String title = entry.getKey();
            
            if(referenceSection.contains(title.toLowerCase())) {
                matches++;
            }
        }
        
        System.out.println("Matches: " + matches);
        
        // The words within a title are likely to be re-ordered, however small
        // words may be omitted.
        // We can use LCS for similarity matching.
        
        // See if titles are between quotes.
        // If not then ...
        /*
        boolean newLine = true;
        for(int i=0; i<referenceSection.length() - 2; i++) {
            char c = referenceSection.charAt(i);
            char nextC = referenceSection.charAt(i);
            
            if(newLine && Character.isDefined(c)) { // Integer enumeration format.
                
            } else if(newLine && c == '[') { // Brack format
                
            }
            
            newLine = false;
            if(c == '\n') {
                newLine = true;
            }
        }*/
        
        int BUFFER_SIZE = 50;
        List<String> authors = new ArrayList<>();
        String title = "";
        String publication = "";
        
        String[] lines = referenceSection.split("\n");
        for(int i=0; i<lines.length; i++) {
            String line = lines[i].trim();
            
            int letterSeq = 0;
            StringBuffer buffer = new StringBuffer(BUFFER_SIZE);
            StringBuffer token = new StringBuffer(BUFFER_SIZE);
            
            int j;
            // Check if line starts with [1] or 1. or has no enumeration identifier.
            for(j=0; j<line.length(); j++) {
                char c = line.charAt(j);
                System.out.println("Char: " + c + " with j = " + j);
                
                // Find boundaries of the citation index number.
                if(j == 0 && c == '[') { // [1]
                    System.out.println("Form 1");
                    // Ignore citation index number.
                    for(int k=j; k<line.length(); k++) {
                        if(line.charAt(k) == ']') {
                            j = k + 1;
                            break;
                        }
                    }
                    
                    continue;
                } else if(j == 0 && Character.isDigit(c)) { // 1.
                    System.out.println("Form 2");
                    
                    for(int k=j; k<line.length(); k++) {
                        char letter = line.charAt(k);
                        if(Character.isDigit(letter)) {
                            // continue
                        } else if(letter == '.') {
                            j = k + 1;
                            break;
                        } else {
                            // What if it is not a digit?
                        }
                    }
                    
                    continue;
                } else if(j == 0) { // No citation index
                    // No index number indentifier or some different format?
                    
                }
                
                // Skip whitespaces if buffer is empty.
                if(Character.isWhitespace(c) && buffer.length() == 0) {
                    continue;
                } // else append the whitespace into the buffer.
                else if(Character.isWhitespace(c)) {
                    token = new StringBuffer(c);
                } else {
                    if(c != '.') {
                        token.append(c);
                    } else if(token.length() == 1) { // Reset on Initial Capital Letter.
                        token = new StringBuffer(c);
                        //continue;
                    }
                }
                
                // End of author name.
                if(c == ',') {
                    String author = buffer.toString();
                    if(author.startsWith("and ")) {
                        author = author.substring("and ".length());
                    }
                    authors.add(author);
                    buffer = new StringBuffer(BUFFER_SIZE);
                    token = new StringBuffer(c);
                    letterSeq = 0;
                    System.out.println("Author end.");
                    
                    continue;
                }
                
                // End of author section, behind the surname of the last author
                // a dot is placed, also behind the initials a dot is placed,
                // but initials are always a single Capital letter followed by
                // a dot, and surnames are always greater than one, so we are
                // checking if the last non-interrupted (by a whitespace) sequence
                // is longer than one.
                if(c == '.' && token.length() > 1) { //&& letterSeq > 1) {
                    // Extract title name, the title name may contain commas, but
                    // is usually terminated with a single dot.
                    
                    String author = buffer.toString();
                    if(author.startsWith("and ")) {
                        author = author.substring("and ".length());
                    }                    
                    
                    authors.add(author);
                    buffer = new StringBuffer(BUFFER_SIZE);
                    letterSeq = 0;
                    
                    System.out.println("Last author.");
                    j = j + 1;
                    break;
                }
                
                System.out.println("Append: " + c);
                buffer.append(c);
                //token.append(c);
                letterSeq++;
            }
            
            buffer = new StringBuffer(BUFFER_SIZE);
            token = new StringBuffer(BUFFER_SIZE);
            for(int h=j; h<line.length(); h++) {
                char c = line.charAt(h);
                
                if(Character.isWhitespace(c) && buffer.length() == 0) {
                    continue;
                } else if(Character.isWhitespace(c)) {
                    token = new StringBuffer(BUFFER_SIZE);
                } else {
                    if(c != '.') {
                        token.append(c);
                    } else if(token.length() == 1) {
                        token = new StringBuffer(c);
                        //continue;
                    }
                }
                
                if(c == '.' && token.length() > 1) {
                    title = buffer.toString();
                    break;
                }
                
                buffer.append(c);
            }
            
            for(String author : authors) {
                System.out.println("Author: " + author);
            }
            
            System.out.println("Title: " + title);
            
            System.out.println("Length: " + line.length());
            System.out.println("Title start of j: " + j);
            System.out.println("To process \"" + line.substring(j) + "\"");
        }
    }
    
    private void citationTest() {
        int id = 0;
        HashMap<String, Integer> titles = readTitles();
        
        //String str = "[1] J. Pearl. Probabilistic reasoning in intelligent systems: Networks of Plausible Inference. Morgan Kaufmann, San Francisco, California, 1988.";
        String str = "1. J. Pearl. Probabilistic reasoning in intelligent systems: Networks of Plausible Inference. Morgan Kaufmann, San Francisco, California, 1988.";
        //String str = "[2] C. H. Bischof, A. Carle, G. F. Corliss, A. Griewank, and P. D. Hovland. ADIFOR: Generating derivative codes from Fortran programs. Scientific Programming, 1(1):11?29, 1992.";
        //String str = "H.J . Kappen and W .A.J.J. Wiegerinck. Second order approximations for probability models. In Todd Leen, Tom Dietterich, Rich Caruana, and Virginia de Sa, editors, Advances in Neural Information Processing Systems 13, pages 238- 244. MIT Press, 2001";
        //String str = "H.J. Kappen and W.A.J.J. Wiegerinck. Second order approximations for probability models. In Todd Leen, Tom Dietterich, Rich Caruana, and Virginia de Sa, editors, Advances in Neural Information Processing Systems 13, pages 238- 244. MIT Press, 2001.";
        
        citationParser(id, str, titles);
    }
    
    private void storeTitles() {
        try {
            CSVParser parser = new CSVParser(new FileReader(papersCSV),
                    CSVFormat.RFC4180);
            
            try (CSVPrinter printer = new CSVPrinter(new FileWriter("titles.csv"), CSVFormat.RFC4180)) {
                printer.print("id");
                printer.print("year");
                printer.print("title");
                printer.println();
                
                Iterator<CSVRecord> iterator = parser.iterator();
                // Skip header line.
                iterator.next();
                
                while(iterator.hasNext()) {
                    CSVRecord record = iterator.next();
                    
                    int paperId = Integer.parseInt(record.get(0));
                    int year = Integer.parseInt(record.get(1));
                    String title = record.get(2);
                    
                    printer.print(paperId);
                    printer.print(year);
                    printer.print(title);
                    printer.println();
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    private HashMap<String, Integer> readTitles() {
        HashMap<String, Integer> map = new HashMap<>();
        
        try {
            CSVParser parser = new CSVParser(new FileReader("titles.csv"),
                    CSVFormat.RFC4180);
                
            Iterator<CSVRecord> iterator = parser.iterator();
            // Skip header line.
            iterator.next();

            while(iterator.hasNext()) {
                CSVRecord record = iterator.next();

                int paperId = Integer.parseInt(record.get(0));
                int year = Integer.parseInt(record.get(1));
                String title = record.get(2);
                
                map.put(title, paperId);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return map;
    }
    
    private void test() {
        String s1 = "[1] J. Pearl. Probabilistic reasoning in intelligent systems: Networks of Plausible Inference. Morgan Kaufmann, San Francisco, California, 1988.";
        //String s2 = "[1] J. Pearl. Probilistic reasoning in intelligent systems: Networks of Plausible Inference. Morgan Kaufmann, San Francisco, California, 1988.";
        String s2 = "[3] R. Kikuchi. Physical R eview, 81:988, 1951.";
        
        System.out.println("s1: " + s1.length() + " s2: " + s2.length());
        System.out.println("LCS: " + getLongestCommonSubsequence(s1, s2));
    }
    
    /**
     * Longest common subsequence implementation from 
     * "https://www.programcreek.com/2014/04/longest-common-subsequence-java/"
     * @param a
     * @param b
     * @return 
     */
    public static int getLongestCommonSubsequence(String a, String b){
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m+1][n+1];

        for(int i=0; i<=m; i++){
            for(int j=0; j<=n; j++){
                if(i==0 || j==0){
                    dp[i][j]=0;
                } else if (a.charAt(i - 1) == b.charAt( j - 1)){
                    dp[i][j] = 1 + dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                }
            }
        }

        return dp[m][n];
    }
    
    public void extractReferences() throws IOException {
        CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180);
        
        Set<Integer> randomNumbers = new HashSet<>();
        SecureRandom r = new SecureRandom();
        
        System.out.println("Construct random numbers.");
        while(randomNumbers.size() < 100) {
            int x = r.nextInt(6560 - 1) + 1; // Prevent 0
            randomNumbers.add(x);
        }
        
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("paper_references_RANDOM_100.csv"), CSVFormat.RFC4180)) {
            // Print headers for output file.
            printer.print("paperId");
            printer.print("year");
            printer.print("title");
            printer.print("reference_text");
            printer.println();
            
            Iterator<CSVRecord> records = parser.iterator();
            // Skip headers.
            records.next();

            int noReferences = 0;
            int bracketFormat = 0;
            int paperCount = 0;
            int twoOrMoreReferenceOccurrence = 0; // # times the word reference occurs in this document, not the # references (citations) in the reference section in this document.
            while(records.hasNext()) {
                CSVRecord record = records.next();
                paperCount++;

                int paperId = Integer.parseInt(record.get(0));
                String year = record.get(1);
                String title = record.get(2);
                String eventType = record.get(3);
                String pdfName = record.get(4);
                String abstractText = record.get(5);
                String paperText = record.get(6);

                if(!paperText.contains("References") && !paperText.contains("REFERENCES")) {
                    //System.out.println("PaperId " + paperId + " with title \"" + title + "\" has no references.");
                    noReferences++;
                } else {
                    // See where the word "references" occurs more than twice.
                    int countReferences = 0;
                    int index = paperText.length() - 1;
                    while(true) {
                        int result = paperText.lastIndexOf("References", index);
                        if(result == -1) {
                            result = paperText.lastIndexOf("REFERENCES", index);
                        }

                        //System.out.println("Result: " + result);
                        //System.out.println(paperText.lastIndexOf("References"));

                        if(result != -1) {
                            countReferences++;
                            index = result - 1;
                        } else {
                            break;
                        }
                    }

                    if(countReferences > 1) {
                        //System.err.println("References Count > 1: " + countReferences + " for paper " + paperId);
                        twoOrMoreReferenceOccurrence++;
                    }

                    //System.out.println("Text length: " + paperText.length());

                    // Extract references section and try to parse and construct
                    // graph.
                    int pos = paperText.lastIndexOf("References", paperText.length() - 1);
                    if(pos == -1) {
                        pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
                    }

                    if(pos != -1) {
                        //System.out.println("Pos: " + pos);
                        // Extract reference section.
                        pos = pos + "references".length() + 1; // + 1 to remove the linefeed.

                        String referenceText = paperText.substring(pos);
                        //System.out.println("ReferenceText: " + referenceText);
                        
                        // Add to CSV file.
                        // Only write if this record is one of the randomly chosen
                        // records.
                        if(randomNumbers.contains(paperCount)) {
//                            printer.print(paperId);
//                            printer.print(year);
//                            printer.print(title);
//                            printer.print(referenceText);
//                            printer.println();
                        }
                        
                        if(referenceText.charAt(0) == '[') {
                            bracketFormat++;
                        }
                    }
                }
            }

            System.out.println("paperCount: " + paperCount);
            System.out.println("noReferences: " + noReferences);
            System.out.println("bracketFormat: " + bracketFormat);
            System.out.println("# Reference Occurrences > 1: " + twoOrMoreReferenceOccurrence);
        }
    }
    
    private void verifyIds() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180) ) {
        
            List<Integer> ids = new ArrayList<>();
            Iterator<CSVRecord> records = parser.iterator();
            // Skip headers.
            records.next();

            while(records.hasNext()) {
                CSVRecord record = records.next();

                int paperId = Integer.parseInt(record.get(0));
                ids.add(paperId);
            }
            
            System.out.println("# ids: " + ids.size());
            // Sort Ids
            Collections.sort(ids);

            // Detect gaps.
            int previous = ids.get(0) - 1;
            System.out.println("Start: " + previous);

            for(int id : ids) {
                if(previous + 1 != id) {
                    System.out.println("ID gap from " + previous + " to " + id);
                }
                
                previous = id;
            }
        }
    }
    
    private void citationExtractor() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180) ) {
        
            Iterator<CSVRecord> records = parser.iterator();
            // Skip headers.
            records.next();
            
            List<String> allCitations = new ArrayList<>();
            
            int total = 0;
            int indexBracketForm = 0;
            int totalIndexBracketCitations = 0;
            int citationMolderErrors = 0;
            while(records.hasNext()) {
                CSVRecord record = records.next();
                total++;
                
                int paperId = Integer.parseInt(record.get(0));
                String paperText = record.get(6);
                
                // Extract reference section.
                int pos = paperText.lastIndexOf("References", paperText.length() - 1);
                if(pos == -1) {
                    pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
                }

                if(pos != -1) {
                    // Extract reference section.
                    pos = pos + "references".length() + 1; // + 1 to remove the linefeed.

                    String referenceText = paperText.substring(pos).trim();
                    if(isBracketFormat(referenceText)) {
                        indexBracketForm++;
                        
                        // Mold citation strings.
                        List<String> citations = referenceMolder(referenceText);
                        if(molderError) {
                            citationMolderErrors++;
                        } else {
                            totalIndexBracketCitations+= citations.size();
                            allCitations.addAll(citations);
                        }
                        
                        if(indexBracketForm >= 5) { //10) {
                            break;
                        }
                        System.out.println(referenceText);
                        System.out.println();
                        System.out.println(citations.size() + " Citations: ");
                        int k = 1;
                        for(String c : citations) {
                            System.out.println(k + ". " + c);
                            k++;
                        }
                        System.out.println();
                        
                        // Bind the citations to this paperId
                        // Later construct the citation graph of all these
                        // binds.
                    }
                }
            }
            
            System.out.println("Total papers: " + total);
            System.out.println("# index bracket form: " + indexBracketForm);
            System.out.println("# molder errors: " + citationMolderErrors);
            System.out.println("# correctly molded bracket form citations: " + totalIndexBracketCitations);
            
            // For each citation request the correct results from the website.
            System.out.println("# citations: " + allCitations.size());
            for(int i=0; i<allCitations.size(); i++) {
                //String title = queryCitation(citation);
                String citation = allCitations.get(i);
                //System.out.println("Citation: \"" + citation + "\"");
                //System.out.println("Title: \"" + title + "\""); 

                queryFreeCite(allCitations);
                if(i == 1) {
                    break;
                }
            }
            
//            // Process all citations.
//            int emptyCitations = 0;
//            int incorrectTitles = 0;
//            int emptyTitles = 0;
//            int numberTitles = 0;
//            int initialsTitles = 0;
//            for(int i=0; i<allCitations.size(); i++) {
//                String citation = allCitations.get(i);
//                if(citation.isEmpty()) {
//                    emptyCitations++;
//                }
//                
//                String title = roughCitationParser(citation);
//                // If the title results is empty or contains numbers or "pp."
//                // then assume it is an incorrect parse.
//                if(title.isEmpty()) {
//                    emptyTitles++;
//                    incorrectTitles++;
//                    
//                    //System.out.println("empty title for: " + citation);
//                }
//                
//                boolean correct = true;
//                for(char c : title.toCharArray()) {
//                    if(Character.isDigit(c)) {
//                        correct = false;
//                        break;
//                    }
//                }
//                
//                if(title.contains("pp.") || !correct) {
//                    incorrectTitles++;
//                    numberTitles++;
//                }
//                
//                boolean hasInitials = false;
//                for(String token : title.split(" ")) {
//                    // malformedInitials may raise many false-positives in this condition and context.
//                    if((token.length() % 2 == 0) && isInitials(token) || !token.toLowerCase().equals("a") && malformedInitials(token)) {
//                        hasInitials = true;
//                        break;
//                    }
//                }
//                
//                if(hasInitials && !title.contains("pp.") && correct) {
//                    incorrectTitles++;
//                }
//                
//                if(hasInitials) {
//                    initialsTitles++;
//                }
//                
//                if(i >= 100) {
//                    break;
//                } else {
//                    if(title.isEmpty() || title.contains("pp.") || !correct || hasInitials) {
//                        //System.out.println("(False) Title: \"" + title + "\" for citation: \"" + citation + "\"");
//                    } else {
//                        //System.out.println("Title: \"" + title + "\" for citation: \"" + citation + "\"");
//                    }
//                }
//            }
//            
//            System.out.println("Empty citations: " + emptyCitations);
//            System.out.println("incorrect: " + incorrectTitles + " out of " + allCitations.size());
//            System.out.println("empty titles: " + emptyTitles);
//            System.out.println("number titles: " + numberTitles);
//            System.out.println("initials titles: " + initialsTitles);
        }
    }
    
    boolean molderError = false;
    
    /**
     * TODO what about empty citations and supposedly incorrect citations, how
     * to filter them out?
     * 
     * @param referenceText
     * @return 
     */
    private List<String> referenceMolder(String referenceText) {
        molderError = false;
        List<String> citations = new ArrayList<>();
       
        int indexNumber = 1;
        StringBuilder citation = new StringBuilder();
        
        for(String line : referenceText.split("\n")) {
            if(line.isEmpty()) {
                continue;
            }            
            
            //String[] tokens = line.split(" ");
            // Check if the first token contains an index number for the citation.
            int pos = line.indexOf(" ");
            //if(pos == -1) {
            //    System.out.println("\"" + line + "\" in: " + referenceText);
            //}
            
            String firstToken = line.trim();
            if(pos != -1) {
                firstToken = line.substring(0, pos).trim();
            } else if(!line.isEmpty()) {
                // TODO which cases get here?
                //System.out.println("pos != -1: \"" + line + "\"");
            }
            
            //System.out.println("FirstToken: " + firstToken);
            
            // TODO not error-tolerant, OCR may sometimes recognize "]" as "}" etc.
            if((firstToken.startsWith("[") || firstToken.startsWith("{")) 
                    && (firstToken.endsWith("]") || firstToken.endsWith("J") || firstToken.endsWith("}"))
                    && firstToken.length() == (String.valueOf(indexNumber).length() + 2)) {
//                if(pos == -1) {
//                    // A line containing only an index number surrounded by
//                    // brackets. TODO it may occur that in the next lines the
//                    // actual citation pop-ups, but it may also be that you
//                    // have multiple lines that contain just the index number
//                    // surrounded by brackets.
//                    molderError = true;
//                    System.out.println("pos == -1");
//                    break;
//                    //return new ArrayList<>();
//                }
                
                try {
                    String indexStr = firstToken.substring(1, firstToken.length() - 1);
                    if(indexNumber == 1) {
                        // Allow tolerance for OCR error where a "1" is recognized
                        // as "l", "I"
                        
                        if(firstToken.length() != 3 || indexStr.length() != 1) {
                            //System.err.println("Wrong BracketForm token: " + firstToken + " in \"" + referenceText + "\"");
                            molderError = true;
                            break;
                        }
                        
                        // When will this be a false-positive?
                        if(indexStr.equals("l") || indexStr.equals("I")) {
                            indexStr = "1";
                        }
                    }
                    
                    if(indexNumber == 5) {
                        // TODO allow for fault tolerance where a 5 is recognized
                        // as an "S".
                        if(indexStr.equals("S")) {
                            indexStr = "5";
                        }
                    }
                    
                    if(indexNumber >= 10 && indexNumber < 20) {
                        if(indexNumber == 10 && indexStr.equals("IO") || indexStr.equals("to") || indexStr.equals("lO")) {
                            indexStr = "10";
                        } else if(indexNumber == 11 && (indexStr.equals("ll") || indexStr.equals("II"))) {
                            indexStr = "11";
                        } else if(indexNumber == 15 && (indexStr.equals("IS") || indexStr.equals("lS"))) {
                            indexStr = "15";
                        } else {
                            if(indexStr.charAt(0) == 'l' || indexStr.charAt(0) == 'I') {
                                indexStr = "1" + indexStr.charAt(1);
                            }
                        }
                    }
                    
                    int index = Integer.parseInt(indexStr);
                    //System.out.println("Index: " + index);
                    if(index != indexNumber) {
                        //System.out.println("Index Number out of sync for token: " + firstToken + " in \"" + referenceText + "\"");
                        molderError = true;
                        break;
                    } else {
                        // Next citation beginning detected, store previous
                        // and reset buffer.
                        if(citation.length() == 0 && indexNumber > 1) {
                            // Empty citation
                            //System.out.println("Empty Citation, for index = " + (indexNumber - 1) + " in: \"" + referenceText + "\"");
                            molderError = true;
                            break;
                        }
                        
                        if(indexNumber > 1) {
                            //System.out.println("Add citation: \"" + citation.toString().trim() + "\"");
                            
                            if(citation.length() > 0) {
                                citations.add(citation.toString().trim());
                            }
                        } // else indexNumber is 1, first citation must yet be
                        // discovered, so do not add current empty buffer.
                        
                        //System.out.println("New buffer.");
                        citation = new StringBuilder();
                        indexNumber++;
                        
                        // Add next citation line to buffer, but remove the
                        // index number identifier.
                        //System.out.println("Append citation: \"" + line.substring(pos + 1).trim() + "\"");
                        
                        if(pos != -1) {
                            citation.append(line.substring(pos + 1).trim());
                        } else {
                            // This line only contains the index number
                            // surrounded by brackets, expect the next lines to
                            // contain the actual citation, if there is only
                            // empty space then report error.
                        }
                    }
                    
                } catch(NumberFormatException e) {
                    molderError = true;
                    System.err.println("referenceMolder() Number Parse Error for token: " + firstToken + " in: \"" + referenceText + "\"");
                    break;
                }
            } else {
                // TODO what if for the first citation no index number was yet
                // detected.
                if(citation.length() > 0) {
                    citation.append(" ");
                }
                //System.out.println("Append line: \"" + line + "\"");
                citation.append(line);
            }
        }
        
        // Add last citation if not empty.
        if(citation.length() > 0) {
            //System.out.println("Add citation: \"" + citation.toString().trim() + "\"");
            citations.add(citation.toString().trim());
        } else {
            //System.out.println("Empty Citation (Last Citation): " + referenceText);
        }
        
        return citations;
    }
    
    private void analysis() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180) ) {
        
            Iterator<CSVRecord> records = parser.iterator();
            // Skip headers.
            records.next();
            
            int total = 0;
            int totalWithReferences = 0;
            int bracketFormatCount = 0; // # of reference sections using [number] format.
            int potentialBracketFormat = 0;
            int bracketForm2 = 0;
            int enumerationFormat = 0;
            int badResultsCounter = 0;
            int enumBadResultCount = 0;
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                total++;

                int paperId = Integer.parseInt(record.get(0));
                String paperText = record.get(6);
                
                // Extract reference section.
                int pos = paperText.lastIndexOf("References", paperText.length() - 1);
                if(pos == -1) {
                    pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
                }

                if(pos != -1) {
                    totalWithReferences++;
                    
                    //System.out.println("Pos: " + pos);
                    // Extract reference section.
                    pos = pos + "references".length() + 1; // + 1 to remove the linefeed.

                    String referenceText = paperText.substring(pos).trim();
                    
                    // Check if this reference uses the [number] format.
                    if(referenceText.charAt(0) == '[') {
                        potentialBracketFormat++;
                        boolean correct = true;
                        int i = 1;
                        // Assume there are not more than 999 citations in a paper.
                        for(; i<5; i++) {
                            char c = referenceText.charAt(i);
                            
                            if(c == ']') {
                                break;
                            } else if(!Character.isDigit(c) && c != 'I' && c != 'i' && c != 'l') { // OCR often recognizes a 1 literal as a Capital I
                                correct = false;
                                break;
                            }
                            
                            if(i == 4) {
                                // Would be weird, would mean an open bracket followed by
                                // 4 digits in a row, but no end bracket within in 5 characters.
                                // Would imply a citation index of over 1000.
                                System.err.println("WEIRD");
                                correct = false;
                            }
                        }
                        
                        if(correct) {
                            bracketFormatCount++;
                            continue;
                        } else {
                            //System.out.println(referenceText);
                        }
                        
                        // Bracket format two.
                        // [Short author list form followed by year]
                        // Short author list form is a list of the authors
                        // last names, space-seperated.
                        
                        // Look for a closing bracket within a max range of 250 characters.
                        StringBuilder builder = new StringBuilder();
                        i = 1;
                        boolean valid = false;
                        for(; i < 250; i++) {
                            char c = referenceText.charAt(i);
                            if(c == ']') {
                                valid = true;
                                break;
                            } else {
                                builder.append(c);
                            }
                        }
                        
                        if(valid) {
                            String inner = builder.toString().trim();
                            // See if the string between the brackets ends with a
                            // number of two digits or four digits.
                            int s = inner.length();
                            if(inner.length() >= 2 && Character.isDigit(inner.charAt(s - 1)) 
                                    && Character.isDigit(inner.charAt(s - 2))
                                    //&& Character.isWhitespace(inner.charAt(s - 3))
                                    || inner.length() >= 2
                                    && Character.isDigit(inner.charAt(s - 1)) 
                                    && Character.isDigit(inner.charAt(s - 2))
                                    && Character.isDigit(inner.charAt(s - 3))
                                    && Character.isDigit(inner.charAt(s - 4))
                                    //&& Character.isWhitespace(inner.charAt(s - 5))
                                    ) {
                                bracketForm2++;
                            } else {
                                if(badResultsCounter == 12) {
                                    //System.out.println(referenceText);
                                    //break;
                                }
                                
                                badResultsCounter++;
                            }
                        }
                    } else {
                        // Probably not the bracket format, can still be if
                        // other symbols were wrongly recognized by OCR or other
                        // parsing mistakes.

                        // Check if the form is an enumeration like 1., 2., 3. etc.
                        boolean correct = true;
                        for(int i=0; i<5; i++) {
                            char c = referenceText.charAt(i);
                            
                            if(c == '.') {
                                break;
                            } else if(!Character.isDigit(c)) {
                                correct = false;
                                break;
                            }
                        }
                        
                        if(correct) {
                            enumerationFormat++;
                        } else {

//                            if(enumBadResultCount == 100) {
//                                System.out.println(referenceText);
//                                break;
//                            }
//                            enumBadResultCount++;
                        }
                        
                    }
                }
            }
            
            System.out.println("# total " + total);
            System.out.println("# totalWithReferences " + totalWithReferences);
            System.out.println("# total with no reference " + (total - totalWithReferences));
            System.out.println("# potential bracket format " + potentialBracketFormat);
            System.out.println("# not bracket format " + (totalWithReferences - potentialBracketFormat));
            System.out.println("# bracket format " + bracketFormatCount);
            System.out.println("# bracket form 2: " + bracketForm2);
            System.out.println("Enumeration format: " + enumerationFormat);
            System.out.println("Unknown Non-bracket format: " + ((totalWithReferences - potentialBracketFormat) - enumerationFormat));
            System.out.println("Unknown Bracket Format " + (potentialBracketFormat - bracketFormatCount - bracketForm2));
        }
    }
    
    private boolean isBracketFormat(String referenceText) {
        boolean correct = false;
        if(referenceText.length() > 0 && referenceText.charAt(0) == '[') {
            correct = true;
            int i = 1;
            // Assume there are not more than 999 citations in a paper.
            for(; i<5; i++) {
                char c = referenceText.charAt(i);

                if(c == ']') {
                    break;
                } else if(!Character.isDigit(c) && c != 'I' && c != 'i' && c != 'l') { // OCR often recognizes a 1 literal as a Capital I or i or l
                    correct = false;
                    break;
                }

                if(i == 4) {
                    // Would be weird, would mean an open bracket followed by
                    // 4 digits in a row, but no end bracket within in 5 characters.
                    // Would imply a citation index of over 1000.
                    System.err.println("WEIRD");
                    correct = false;
                }
            }
        }
        return correct;
    }
    
    private void parsify() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180) ) {
        
            Iterator<CSVRecord> records = parser.iterator();
            records.next();
            
            int bracketFormatCount = 0;
            int initialsStart = 0;
            while(records.hasNext()) {
                CSVRecord record = records.next();
                
                String paperText = record.get(6);
                
                
                String referenceText = extractReferenceSection(paperText);
                if(isBracketFormat(referenceText)) {
                    bracketFormatCount++;
                    
                    // Skip citation index [number]
                    int pos = referenceText.indexOf("]");
                    referenceText = referenceText.substring(pos + 1).trim();
                    
                    StringBuilder authorName = new StringBuilder();
                    /**
                    for(int i=0; i<referenceText.length(); i++) {
                        char c = referenceText.charAt(i);
                        char c2 = referenceText.charAt(i + 1);
                        
                        // Initial detection.
                        if(c == ',') {
                            
                        } else if(Character.isUpperCase(c) && c2 == '.') {
                            // Expect more initials or a surname.
                        } else if(c == '.') {
                            // No initial and a dot. What about isolated dots?
                        }
                    }*/
                    
                    if(referenceText.length() >= 2) {
                        char c = referenceText.charAt(0);
                        char c2 = referenceText.charAt(1);
                        
                        // Citation format is first initials followed by surname
                        if(Character.isUpperCase(c) && c2 == '.') {
                            initialsStart++;
                            

                        }
                    }
                }
            }
            System.out.println("# bracket form " + bracketFormatCount);
            System.out.println("Initials Start " + initialsStart);
        }
    }
    
    private class ParserTest {
        private final String citation;
        private final List<String> authors;
        private final String title;
        private boolean correct = false;
        private boolean titleWrong = false;
        private boolean authorsWrong = false;

        public ParserTest(String citation, String title, List<String> authors) {
            this.citation = citation;
            this.authors = authors;
            this.title = title;
        }        
    }
    
    private void parserCorrectnessTest() throws IOException {
        // Read the test strings from a text file.
        CSVParser parser = new CSVParser(new FileReader(parserTestCSV), 
                CSVFormat.RFC4180);
        
        Iterator<CSVRecord> records = parser.iterator();
        // Skip headers
        records.next();
        
        List<ParserTest> tests = new ArrayList<>();
        
        while(records.hasNext()) {
            CSVRecord record = records.next();
            
            String citation = record.get(0);
            String title = record.get(2);
            
            String[] authors = record.get(1).split(",");
            List<String> paperAuthors = new ArrayList<>();
            for(int i=0; i<authors.length; i++) {
                paperAuthors.add(authors[i].trim());
            }
            
            ParserTest test = new ParserTest(citation, title, paperAuthors);
            tests.add(test);
        }
        
        // Test the correctness
        int correctTests = 0;
        for(ParserTest test : tests) {
            boolean correctTest = true;
            List<String> correctAuthors = test.authors;
            CitationResult result = genericCitationParser(test.citation);
            
            if(!result.title.equals(test.title)) {
                test.titleWrong = true;
                correctTest = false;
            }
            
            if(result.authors.size() != result.authors.size()) {
                correctTest = false;
            } else {
                boolean correct = true;
                for(String author : result.authors) {
                    if(correctAuthors.contains(author)) {
                        correctAuthors.remove(author); // To avoid that we say it is equal but we are comparing to a list with duplicates, that does not contain each unique element.
                    } else {
                        correct = false;
                        break;
                    }
                }
                
                if(!correct) {
                    test.authorsWrong = true;
                    correctTest = false;
                }
            }
            
            if(correctTest) {
                correctTests++;
            }
            
            test.correct = correctTest;
        }
        
        System.out.println("Correct tests: " + correctTests + " out of " + tests.size());
        System.out.println("Failed tests: " + (tests.size() - correctTests) + " out of " + tests.size());
        for(ParserTest test : tests) {
            if(!test.correct) {
                System.out.println("Citation: " + test.citation);
                System.out.println("AuthorsWrong: " + test.authorsWrong);
                System.out.println("TitleWrong: " + test.titleWrong);
                
//                if(test.titleWrong) {
//                    CitationResult r = genericCitationParser(test.citation);
//                    
//                    System.out.println(test.title);
//                    System.out.println(r.title);
//                    System.out.println("Equals " + test.title.equals(r.title));
//                    break;
//                }
                
                //System.out.println("Authors empty: " + test.authors.isEmpty());
                
//                System.out.println("AuthorsWrong " + test.authorsWrong);
//                if(test.authorsWrong) {
//                    CitationResult r = genericCitationParser(test.citation);
//                    System.out.println("Correct Authors:");
//                    for(String author : test.authors) {
//                        System.out.println(author);
//                    }
//                    
//                    System.out.println("Parser Authors:");
//                    for(String author : r.authors) {
//                        System.out.println(author);
//                    }
//                    
//                    break;
//                }
            }
        }
    }
    
    private void initialParserTest() {
        //String str = "J. Pearl. Probabilistic reasoning in intelligent systems: Networks of Plausible Inference. Morgan Kaufmann, San Francisco, California, 1988.";
        //String str = "1. J. Pearl. Probabilistic reasoning in intelligent systems: Networks of Plausible Inference. Morgan Kaufmann, San Francisco, California, 1988.";
        //String str = "C. H. Bischof, A. Carle, G. F. Corliss, A. Griewank, and P. D. Hovland. ADIFOR: Generating derivative codes from Fortran programs. Scientific Programming, 1(1):11?29, 1992.";
        //String str = "H.J . Kappen and W .A.J.J. Wiegerinck. Second order approximations for probability models. In Todd Leen, Tom Dietterich, Rich Caruana, and Virginia de Sa, editors, Advances in Neural Information Processing Systems 13, pages 238- 244. MIT Press, 2001";
        //String str = "H.J. Kappen and W.A.J.J. Wiegerinck. Second order approximations for probability models. In Todd Leen, Tom Dietterich, Rich Caruana, and Virginia de Sa, editors, Advances in Neural Information Processing Systems 13, pages 238- 244. MIT Press, 2001.";        
        //String str = "H. J. Kappen and W. A. J. J. Wiegerinck.";
        //String str = "H J Kappen and W A J J Wiegerinck.";
        
        //String str = "D.H Wolpert. Stacked generalization. Neural Networks, 5(2):241-59, 1992.";
        //String str = "Michael P. Perrone and Leon N Cooper. When networks disagree: Ensemble method for neural networks. In R. J. Mammone, editor, Neural Networks for Speech and Image processing. Chapman-Hall, 1993.";
        //String str = "H.S. Seung, M. Opper, and H. Sompolinsky. Query by committee. In Proceedings of the Fifth Workshop on Computational Learning Theory, pages 287-294, San Mateo, CA, 1992. Morgan Kaufmann.";
        
        //String str = "Barlow, H. B. and Foldiak, P. (1989). Adaptation and decorrelation in the cortex. In Durbin, R. M., Miall, c., and Mitchison, G. J., editors, The Computing Neuron, chapter 4, pages 54-72. Addison-Wesley, Wokingham.";
        //String str = "Barlow, H. B. and Foldiak, P. Adaptation and decorrelation in the cortex. In Durbin, R. M., Miall, c., and Mitchison, G. J., editors, The Computing Neuron, chapter 4, pages 54-72. Addison-Wesley, Wokingham.";
        String str = "S. Mika, B. Sch?olkopf, A. J. Smola, K.-R. M?uller, M. Scholz, and G. R?atsch. Kernel PCA and de-noising in feature spaces. In M. S. Kearns, S. A. Solla, and D. A. Cohn, editors, Advances in Neural Information Processing Systems 11, pages 536?542, Cambridge, MA, 1999. MIT Press.";
        
        //String str = "P. Simard, B. Victorrio, Y. Le Cun & J. Denker. (1992) \"\"Tangent Prop - A formalism for specifying selected invariances in an adaptive network,\"\" in J. E. Moody, S. J . Hanson and R. P. Lippmann (eds.) Advances in Neural Information Processing Systems-4 Morgan Kaufmann pp. 895-903.";
        
        //String str = "G. D. Wilensky & N. Manukian. (1994)";
        //String str = "S. Mika, B. Sch?olkopf, A. J. Smola";
        
        
        //genericCitationParser(str);
        
        roughCitationParser(str);
        
        //initialStartCitationParser(str);
    }
    
    // Not going to work since if the author section is of the form
    // initials followed by surname than the surname also ends with a dot.
    // Some initials are wrongly formatted and may be recognized as the author
    // section end.
    private String roughCitationParser(String citation) {
        String[] tokens = citation.split(" ");
        //System.out.println("# tokens " + tokens.length);

        StringBuilder builder = new StringBuilder();
        int dotCount = 0;
        
        // First detect the format.
        
        for(int i=0; i<tokens.length; i++) {
            String token = tokens[i];
            //System.out.println("Token" + i + ": " + token);
            
            if(token.endsWith(".")) {
                // Check if it is not an initial.
                //if((token.length() == 1 && Character.isUpperCase(token.charAt(0)) 
                //        || token.length() % 2 == 0) && !isInitials(token)) {  
                //}                
                
                // Build a test case that tests this function with inputs like ".A.J.J." or ".A.J.J" or "A.J.J."
                if(token.length() >= 2 &&
                        ((token.length() % 2 != 0) && !malformedInitials(token))
                        || 
                        (token.length() % 2 == 0 && !isInitials(token))
                        ) {
                    // Make sure the token is not a malformed initial like ".A.J.J." or ".A"
                    // This one is not modulo 2, but still an initial.
                    
                    //System.out.println("Ends with dot: " + token);
                    //System.out.println((token.length() >= 2) && (token.length() % 2 != 0));
                    //System.out.println((token.length() >= 2) && (token.length() % 2 == 0) && !isInitials(token));
                    //System.out.println("Increment Dot Count");
                    dotCount++;

                    if(dotCount == 2) {
                        //builder.append(token.substring(0, token.length() - 1));
                        // Add final token.
                        builder.append(token);
                        break;
                    }

                    continue;
                }
            }
            
            // TODO title end may also be signaled by a quote.
            
            if(dotCount == 1) {
                // Here you may get tokens that are publication years or prepended
                // with a quote.
                
                builder.append(token);
                builder.append(" ");
            }
        }
        
        //System.out.println("Title: " + builder.toString());
        return builder.toString();
    }    
    
    private class CitationResult {
        private final List<String> authors;
        private final String title;

        public CitationResult(List<String> authors, String title) {
            this.authors = authors;
            this.title = title;
        }        
    }
    
    private CitationResult genericCitationParser(String citation) {
        //Analyzer analyzer = new StandardAnalyzer();

        String[] tokens = citation.split(" ");
        System.out.println("# tokens " + tokens.length);
        
        // Paper authors in a citation. (not the authors of the publication
        // in which the paper is published)
        List<String> authors = new ArrayList<>();
        // Title of the paper.
        String title = "";

        StringBuilder builder = new StringBuilder();
        boolean isName = false;
        //boolean addedAuthor = false;
        
        boolean titleSection = false;
        
        for(int i=0; i<tokens.length; i++) {
            String token = tokens[i].trim();
            System.out.println("Token " + i + ": " + token);

            if(!titleSection) {
                // Check if token equals "and" or "&"
                if(token.equals("and") || token.equals("&")) {
                    isName = false;
                    // TODO this is a clue that only one author name must
                    // follow.
                    System.out.println("SKIP AND");
                    //addedAuthor = false;
                    
                    // TODO continue correct here?
                    continue;
                }

                if(token.length() == 1 || token.length() % 2 == 0) {
                    //System.out.println("Initials Check");

                    if(isInitials(token)) {
                        if(isName && builder.length() > 0) {
                            // Last token was a name, current token is an initial.
                            // Assume author name complete, but author name
                            // not terminated by a "," or "."
                            // This usually only happens for middle names like in
                            // Dutch "van der" or names like "Le Cun"
                            String author = builder.toString();
                            System.out.println("Author: " + author);                        
                        }

                        System.out.println("Is Initials");
                        builder.append(token);

                        if(token.length() == 1) {
                            builder.append(".");
                        }

                        // Reset.
                        isName = false;
                    }
                }

                if(isSurname(token)) { // Check if token is surname.
                    System.out.println("Is Surname");
                    isName = true;

                    if(token.endsWith(".")) {
                        // Strip "."
                        token = token.substring(0, token.length() - 1);
                        builder.append(" ");
                        builder.append(token);

                        // Should expect the title section now.
                        String author = builder.toString();
                        // Add author to list.
                        authors.add(author);                        
                        
                        System.out.println("Author: " + author);
                        //System.out.println("Author: " + author);
                        System.out.println("End of Author section.");
                        titleSection = true;
                        builder = new StringBuilder();
                        
                        continue;
                        //break;
                    } else if(token.endsWith(",")) {
                        token = token.substring(0, token.length() - 1);
                        builder.append(" ");
                        builder.append(token);

                        // Should expect the next author name.
                        String author = builder.toString();
                        // Add author to list.
                        authors.add(author);
                        
                        System.out.println("Author: " + author);
                        builder = new StringBuilder();
                        
                        // TODO What if the author section ends here?
                        
                        
                    } // else if followed by and it is an author name.
                    else if(i < (tokens.length - 1)) {
                        if(tokens[i + 1].equals("and") || tokens[i + 1].equals("&")) {
                            builder.append(" ");
                            builder.append(token);

                            String author = builder.toString();
                            // Add author to list.
                            authors.add(author);
                        
                            System.out.println("Author: " + author);
                            builder = new StringBuilder();
                        } else {
                            System.out.println("Potential Middlename");
                            if(builder.length() > 0) {
                                builder.append(" ");
                            }
                            
                            builder.append(token);
                            
                            // TODO is this correct?
                            builder.append(" ");
                        }
                    } else {

                    }
                }
                //else {
                //    // Unexpected token, incorrect citation format.
                //    System.err.println("ERROR TOKEN: " + token);
                //    break;
                //}
            } else {
                builder.append(token);
                if(i < (tokens.length - 1) && !token.endsWith(".")) {
                    builder.append(" ");
                }
                
                if(token.endsWith(".")) {
                    System.out.println("End of title section.");
                    title = builder.toString();
                    //System.out.println("Title: " + title);
                    break;
                }
                
                // If no token ending with "." was encountered and this is the
                // last token, assume title section did not end with "."
                if(i == (tokens.length - 1)) {
                    title = builder.toString();
                }
            }
        }
        
        int j = 1;
        for(String author : authors) {
            System.out.println("Author " + j + ": " + author);
            j++;
        }
        
        System.out.println("Title: " + title);
        
        return (new CitationResult(authors, title));
    }
    
    private void initialStartCitationParser(String referenceText) {
        String[] lines = referenceText.split("\n");
        System.out.println("# lines " + lines.length);
        for(String line : lines) {
            // TODO before splitting on tokens, we may try
            // to fix the string for errors.

            String[] tokens = line.split(" ");
            System.out.println("# tokens " + tokens.length);

            StringBuilder builder = new StringBuilder();
            for(int i=0; i<tokens.length; i++) {
                String token = tokens[i].trim();
                System.out.println("Token " + i + ": " + token);

                // Check if token equals "and" or "&"
                if(token.equals("and") || token.equals("&")) {
                    // TODO this is a clue that only one author name must
                    // follow.
                    System.out.println("SKIP AND");
                    continue;
                }                
                
                if(token.length() == 1 || token.length() % 2 == 0) {
                    if(isInitials(token)) {
                        System.out.println("Is Initials");
                        builder.append(token);
                        
                        if(token.length() == 1) {
                            builder.append(".");
                        }
                    }
                }

                // Check if token is surname.
                if(isSurname(token)) {
                    System.out.println("Is Surname");

                    if(token.endsWith(".")) {
                        // Strip "."
                        token = token.substring(0, token.length() - 1);
                        builder.append(" ");
                        builder.append(token);
                        
                        // Should expect the title section now.
                        String author = builder.toString();
                        System.out.println("Author: " + author);
                        System.out.println("End of Author section.");
                        break;
                    } else if(token.endsWith(",")) {
                        token = token.substring(0, token.length() - 1);
                        builder.append(" ");
                        builder.append(token);
                        
                        // Should expect the next author name.
                        String author = builder.toString();
                        System.out.println("Author: " + author);
                        builder = new StringBuilder();
                    } // else if followed by and it is an author name.
                    else if(i < (tokens.length - 1)) {
                        if(tokens[i + 1].equals("and")) {
                            builder.append(" ");
                            builder.append(token);
                            
                            String author = builder.toString();
                            System.out.println("Author: " + author);
                            builder = new StringBuilder();
                        } else {
                            
                        }
                    } else {
                        
                    }
                }
            }
        }        
    }
    
    private boolean isCapitalized(String str) {
        if(str.isEmpty()) {
            return false;
        }
        
        return Character.isUpperCase(str.charAt(0));
    }
    
    private boolean isSurname(String text) {
        if(text.isEmpty()) {
            return false;
        }
        
        // Assume a surname is longer than two characters.
        // For a surname this assumption is correct but for a middlename like
        // Y. Le Cun, "Le" is acceptable, but the true surname is "Cun" is longer
        // than three characters.
        if(text.length() <= 1) {
            return false;
        }
        
        if(text.length() == 2) {
            // This filters out initials that were input or wierd two-letter
            // sequences.
            // TODO this filters out a middlename in lowercases.
            if(!Character.isUpperCase(text.charAt(0)) || !Character.isLetter(text.charAt(1))) {
                return false;
            }
        }
        
        boolean correct = true;
        for(int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            
            // Asssume that a surname always starts with a Capitalized letter.
            if(i == 0 && !Character.isUpperCase(c)) {
                // A middlename may not start with a capital letter.
                System.err.println("No start with uppercase letter for " + text);                
                correct = false;
                break;
            }
            
            if(i > 0 && !(i == text.length() - 1 && (c == ',' || c == '.')) 
                    && !Character.isLetter(c) && !Character.isLowerCase(c)) {
                correct = false;
                break;
            }
        }
        return correct;
    }
    
    private boolean malformedInitials(String text) {
        if(text.isEmpty()) {
            return false;
        }
        
        if(text.length() == 1) {
            if(Character.isUpperCase(text.charAt(0))) {
                return true;
            }
            return false;
        }
        
        char firstChar = text.charAt(0);
        // This is not a supposed initials string, so it cannot be malformed
        // initials.
        if(!Character.isLetter(firstChar) && firstChar != '.') {
            return false;
        }
        
        boolean letterFirst = true;
        if(firstChar == '.') {
            letterFirst = false;
        }
        
        boolean malformed = true;
        for(int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            if(letterFirst) {
                if(i % 2 == 0 && !Character.isLetter(c)) {
                    malformed = false;
                    break;
                }
                
                if(i % 2 != 0 && c != '.') {
                    malformed = false;
                    break;
                }
            } else {
                if(i % 2 == 0 && c != '.') {
                    malformed = false;
                    break;
                }
                
                if(i % 2 != 0 && !Character.isLetter(c)) {
                    malformed = false;
                    break;
                }
            }
        }
        
        return malformed;
    }
    
    /**
     * 
     * @param text a string without whitespaces, potentially containing intials seperated by dots.
     * @return 
     */
    private boolean isInitials(String text) {
        if(text.length() % 2 != 0) {
            throw new IllegalArgumentException("Input string is not modulo 2.");
        }
        
        if(text.isEmpty()) {
            return false;
        }
        
        if(text.length() == 1) {
            if(Character.isUpperCase(text.charAt(0))) {
                return true;
            }
            return false;
        }
        
        boolean correct = true;
        for(int i=0; i<text.length(); i+=2) {
            char c = text.charAt(i);
            char c2 = text.charAt(i + 1);
            
            if(c2 == '.') {
                if(!(Character.isUpperCase(c) || c == '1' || c == 'i' || c == 'l')) {
                    correct = false;
                    break;
                }
            } else {
                correct = false;
                break;
            }
        }
        
        return correct;
    }
    
    private String extractReferenceSection(String paperText) {
        // Extract reference section.
        int pos = paperText.lastIndexOf("References", paperText.length() - 1);
        if(pos == -1) {
            pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
        }

        if(pos != -1) {
            pos = pos + "references".length() + 1; // + 1 to remove the linefeed.
            String referenceText = paperText.substring(pos).trim();
            return referenceText;
        }
        return "";
    }
    
    private void testQuery() {
        String citation = "P. Massart A. Barron, L. Birg?e. Risk bounds for model selection via penalization. Proba.Theory Relat.Fields, 113:301?413, 1999.";
        String title = queryCitation(citation);
        System.out.println("Title: " + title);
    }
    
    private String queryCitation(String citation) {
        String result = "";
        try {
            String urlString = "http://search.labs.crossref.org/dois?rows=1&q=";
            urlString = urlString + URLEncoder.encode(citation, "UTF-8");
            
            URLConnection connection = null;
            try {
                URL url = new URL(urlString);
                
                connection = url.openConnection();
                connection.setDoInput(true);
                InputStream input = connection.getInputStream();
                //connection.setRequestProperty("", citation);
                connection.connect();
                
                //connection.
                JSONParser parser = new JSONParser();
                JSONArray array = (JSONArray) parser.parse(new InputStreamReader(input));
                //System.out.println("Array Length: " + array.size());
                //System.out.println(array);
                
                Iterator<JSONObject> iterator = array.iterator();
                //while(iterator.hasNext()) {
                    //JSONObject obj = iterator.next();
                    //System.out.println("Title: " + obj.get("title"));
                    //return obj.get("title");
                //}
                
                if(iterator.hasNext()) {
                    return (String) iterator.next().get("title");
                }
                
            } catch (MalformedURLException ex) {
                Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

            }

            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    private void testFreeCite() {
        //String citation = "D. Applegate and W. Cook. A computational study of the job-shop problem. ORSA Journal of Computing, 3(2), 1991.";
        //List<String> list = new ArrayList<>();
        //list.add(citation);
        
        String citations = "[1] D. Ackley. A Connectionist Machine for Genetic Hillclimbing. Kluwer Academic Publishers, 1987.\n" +
"[2] D. Applegate and W. Cook. A computational study of the job-shop problem. ORSA Journal of Computing, 3(2), 1991.\n" +
"[3] J. Carlier and E. Pinson. An algorithm for solving the jobshop problem. Mngmnt. Sci., 35:(2):164-176, 1989.\n" +
"[4] L. Davis. Bit-climbing, representational bias, and test suite design. In Belew and Booker, editors, ICGA-4, pages 18-23, 1991.\n" +
"[5] H. Fang, P. Ross, and D. Corne. A promising GA approach to job-shop scheduling, rescheduling, and open-shop scheduling problems. In Forrest, editor, ICGA-5, 1993.\n" +
"[6] M. Garey and D. Johnson. Computers and Intractability. W .H. Freeman and Co., 1979.\n" +
"[7] D. Goldberg. Genetic Algorithms in Search, Optimization, and Machine Learning. Addison Wesley, 1989.\n" +
"[8] D. Goldberg and K. Deb. A comparative analysis of selection schemes used in GAs. In FOGA-2, pages 69-93, 1991.\n" +
"[9] K. De Jong. An Analysis of the Behavior of a Class of Genetic Adaptive Systems. PhD thesis, University of Michigan, 1975.\n" +
"[10] A. Juels and M. Wattenberg. Stochastic hillclimbing as a baseline method for evaluating genetic algorithms. Technical Report CSD-94-834, UC Berkeley, CS Division, 1994.\n" +
"[11] S. Khuri, T. Back, and J. Heitk6tter. An evolutionary approach to combinatorial optimization problems. In Procs. of CSC 1994, 1994.";
        
        List<String> list = referenceMolder(citations);
//        for(String cite : list) {
//            System.out.println(cite);
//        }
        
        String response = queryFreeCite(list);
        if(response != null) {
            parseFreeCiteResponse(response);            
        } else {
            System.err.println("Response == null");
        }
    }
    
    private String queryFreeCite(List<String> citations) {
        String urlString = "http://freecite.library.brown.edu/citations/create";
        //urlString = urlString + URLEncoder.encode(citation, "UTF-8");
        // Create citation payload.
        //String citeString = URLEncoder.encode("citation=" + citations.get(0), "UTF-8");
        
        // Build parameter string.
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<citations.size(); i++) {
            String citation = citations.get(i);
            if(i > 0) {
                builder.append("&");
            }
            
            builder.append("citation[]=");
            builder.append(citation);
        }
        
        String citeString = builder.toString();
        //System.out.println(citeString);
        byte[] payload = citeString.getBytes(Charset.forName("UTF-8"));
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "text/xml");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Length", "" + payload.length);
            
            connection.setConnectTimeout(7000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            
            //InputStream input = connection.getInputStream();
            try (OutputStream output = connection.getOutputStream()) {
                //System.out.println("Write output");
                output.write(payload);
                output.flush();
                //System.out.println("Written output.");
            }
            //System.out.println("Connect");
            //connection.connect();
            
            //System.out.println("Expecting Content");
            //String content = (String) connection.getContent();
            //System.out.println("Content: " + content);
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                //System.out.println("Read input.");
                String line;
                while((line = reader.readLine()) != null) {
                    //System.out.println("Line: " + line);
                    response.append(line);
                }
            }
            
            // Disconnect.
            //System.out.println("Disconnect");
            connection.disconnect();
            
            return response.toString();
        } catch (MalformedURLException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
        }
        
        return null;
    }
    
    private class ParsedCitation {
        private boolean valid = false;
        private final String rawCitation;
        private final String authors;
        private final String title;

        public ParsedCitation(boolean valid, String rawCitation, String authors, String title) {
            this.valid = valid;
            this.rawCitation = rawCitation;
            this.authors = authors;
            this.title = title;
        }
    }
    
    private List<ParsedCitation> parseFreeCiteResponse(String response) {
        List<ParsedCitation> parsedList = new ArrayList<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));

            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            //System.out.println("Root: " + doc.getDocumentElement().getNodeName());
            
            NodeList citationElements = doc.getElementsByTagName("citation");
            int validCitations = 0;
            int invalidCitations = 0;
            //System.out.println("# citations in response: " + citationElements.getLength());
            for(int k=0; k<citationElements.getLength(); k++) {
                Node citation = citationElements.item(k);

                NamedNodeMap attributes = citation.getAttributes();
                boolean valid = Boolean.valueOf(attributes.getNamedItem("valid").getNodeValue());

                StringBuilder authorsString = new StringBuilder();
                String title = "";
                String rawCitation = "";
                
                NodeList nodeList = citation.getChildNodes();
                for(int i=0; i<nodeList.getLength(); i++) {
                    Node item = nodeList.item(i);

                    if(item.getNodeName().equals("authors")) {
                        NodeList list = item.getChildNodes();
                        for(int j=0; j<list.getLength(); j++) {
                            Node author = list.item(j);
                            //System.out.println("Author: " + author.getTextContent());
                            
                            authorsString.append(author.getTextContent().trim());
                            if(j < (list.getLength() - 1)) {
                                authorsString.append(" ;; ");
                            }
                        }                  
                    }

                    if(item.getNodeName().equals("title")) {
                        title = item.getTextContent().trim();
                        //System.out.println("Title: " + title);
                    }

                    if(item.getNodeName().equals("raw_string")) {
                        rawCitation = item.getTextContent().trim();
                    }
                }

                ParsedCitation ps = new ParsedCitation(valid, rawCitation, authorsString.toString().trim(), title);
                parsedList.add(ps);
            }
            
            //System.out.println("Valid citations: " + validCitations);
            //System.out.println("Invalid citations: " + invalidCitations);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return parsedList;
    }
    
    private void testMolder2() {
        String referenceText = "[1]\n" +
"[2]\n" +
"[3]\n" +
"[4]\n" +
"[5]\n" +
"[6]\n" +
"[7]\n" +
"[8]\n" +
"[9]\n" +
"[10]\n" +
"[11]\n" +
"\n" +
"C. Granger, Journal of Forecasting 8, 231 (1989).\n" +
"D. Wolpert, Neural Networks 5, 241 (1992) .\n" +
"L. Breimann, Tutorial at NIPS 7 and personal communication.\n" +
"L. Hansen and P. Salamon, IEEE Trans. Pattern Anal. and Mach. Intell. 12,\n" +
"993 (1990).\n" +
"M. P. Perrone and L. N. Cooper, in Neural Networks for Speech and Image\n" +
"processing, ed. R. J. Mammone (Chapman-Hall, 1993).\n" +
"S. Hashem: Optimal Linear Combinations of Neural Networks. Tech. Rep .\n" +
"PNL-SA-25166, submitted to Neural Networks (1995) .\n" +
"A. Krogh and J. Vedelsby, in NIPS 7, ed. G. Tesauro et al., p. 231 (MIT Press,\n" +
"1995).\n" +
"R. Meir, in NIPS 7, ed. G. Tesauro et al., p. 295 (MIT Press, 1995).\n" +
"A. Krogh and J. A. Hertz, J. Phys. A 25,1135 (1992).\n" +
"J. A. Hertz, A. Krogh, and G. I. Thorbergsson, J. Phys. A 22, 2133 (1989).\n" +
"P. Sollich, J. Phys. A 27, 7771 (1994).";

//        String referenceText = "[1]\n" +
//"\n" +
//"B. D. O. Anderson and J. B. Moore, Optimal Filtering, Prentice Hall,\n" +
//"1979.\n" +
//"\n" +
//"[2]\n" +
//"\n" +
//"A. Gelb, Ed., Applied Optimal Estimation, MIT Press, 1974.\n" +
//"\n" +
//"[3]\n" +
//"\n" +
//"B. Irie, and S. Miyake, \"Capabilities of Three-layered Perceptrons,\"\n" +
//"Proceedings of the IEEE International Conference on Neural Networks,\n" +
//"San Diego, June 1988, Vol. I, pp. 641-648.\n" +
//"\n" +
//"[4]\n" +
//"\n" +
//"R. E. Kalman, \"A New Approach to Linear Filtering and Prediction\n" +
//"Problems,\" 1. Basic Eng., Trans. ASME, Series D, Vol 82, No.1, 1960,\n" +
//"pp.35-45.\n" +
//"\n" +
//"[5]\n" +
//"\n" +
//"R. W. Prager and F. Fallside, \"The Modified Kanerva Model for\n" +
//"Automatic Speech Recognition,\" in 1988 IEEE Workshop on Speech\n" +
//"Recognition, Arden House, Harriman NY, May 31-Jllne 3,1988.\n" +
//"\n" +
//"[6]\n" +
//"\n" +
//"D. E. Rumelharl, G. E. Hinton and R. J. Williams, \"Learning Internal\n" +
//"Representations by Error Propagation,\" in D. E. Rllmelhart and\n" +
//"J. L. McCelland (Eds.), Parallel Distributed Processing: Explorations in\n" +
//"the Microstructure oj' Cognition. Vol 1: Foundations. MIT Press, 1986.\n" +
//"\n" +
//"[7J\n" +
//"\n" +
//"A. Waibel, T. Hanazawa, G. Hinton, K. Shikano and K . Lang\n" +
//"\"Phoneme Recognition Using Time-Delay Neural Networks,\" A 1R\n" +
//"internal Report TR-I-0006, October 30, 1987.";
        List<String> citations = referenceMolder(referenceText);
        System.out.println("# citations: " + citations.size());
    }
    
    private void testMolder() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180) ) {
        
            Iterator<CSVRecord> records = parser.iterator();
            // Skip headers.
            records.next();
            
            List<String> allCitations = new ArrayList<>();
            
            int total = 0;
            int indexBracketForm = 0;
            int totalIndexBracketCitations = 0;
            int citationMolderErrors = 0;
            int correctFormPapers = 0;
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                total++;
                
                int paperId = Integer.parseInt(record.get(0));
                String paperText = record.get(6);
                
                // Extract reference section.
                int pos = paperText.lastIndexOf("References", paperText.length() - 1);
                if(pos == -1) {
                    pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
                }

                if(pos != -1) {
                    // Extract reference section.
                    pos = pos + "references".length() + 1; // + 1 to remove the linefeed.

                    String referenceText = paperText.substring(pos).trim();
                    
                    if(isBracketFormat(referenceText)) {
                        indexBracketForm++;
                        
                        // Mold citation strings.
                        List<String> citations = referenceMolder(referenceText);
                        if(molderError) {
                            citationMolderErrors++;
                        } else {
                            // Correct mold.
                            correctFormPapers++;
                            totalIndexBracketCitations+= citations.size();
                            allCitations.addAll(citations);
                            
                            // Check if only the brackets with index number
                            // occur.
                            boolean occur = false;
                            for(String c : citations) {
                                c = c.trim();
                                if(c.startsWith("[") && c.endsWith("]")) {
                                    occur = true;
                                    break;
                                }
                            }
                            
                            if(occur) {
                                System.out.println("References for paper " + paperId + ": ");
                                System.out.println("\"" + referenceText + "\"");
                                System.out.println();
                                System.out.println("# citations " + citations.size());
                                for(String c : citations) {
                                    System.out.println(c);
                                }
                                System.out.println();
                                break;
                            }
                        }
                        
//                        if(indexBracketForm >= 5) { //10) {
//                            break;
//                        }
//                        System.out.println(referenceText);
//                        System.out.println();
//                        System.out.println(citations.size() + " Citations: ");
//                        int k = 1;
//                        for(String c : citations) {
//                            System.out.println(k + ". " + c);
//                            k++;
//                        }
//                        System.out.println();
                        
                        // Bind the citations to this paperId
                        // Later construct the citation graph of all these
                        // binds.
                    }
                }
            }
            
            System.out.println("Total papers: " + total);
            System.out.println("# index bracket form: " + indexBracketForm);
            System.out.println("# molder errors: " + citationMolderErrors);
            System.out.println("# correctly molded papers: " + correctFormPapers);
            System.out.println("# correctly molded bracket form citations: " + totalIndexBracketCitations);
            System.out.println("All Citations: " + allCitations.size());
            
            // How many citations are shorter than 10 characters?
            int shortCitations = 0;
            int citationWithBrackets = 0;
            for(String citation : allCitations) {
                if(citation.trim().length() < 10) {
                    shortCitations++;
                }
                
                citation = citation.trim();
                if(citation.startsWith("[") && citation.endsWith("]")) {
                    citationWithBrackets++;
                } else if(citation.length() < 10) {
                    System.out.println("ELSE: " + citation);
                }
            }
            System.out.println("Short Citations: " + shortCitations);
            System.out.println("Citation with []: " + citationWithBrackets);
            
            // Investigate a random sample of 100 citations and look if they are correct.
            Set<Integer> set = new HashSet<>();
            SecureRandom sr = new SecureRandom();
            
            int sampleSize = 100;
            if(allCitations.size() < sampleSize) {
                sampleSize = (int) (0.7 * allCitations.size());
            }
            
            while(set.size() != sampleSize) {
                set.add(sr.nextInt(allCitations.size()));
            }
            
            System.out.println();
            System.out.println("Citations random sample of 100");
            for(int i : set) {
                System.out.println(allCitations.get(i));
            }
        }
    }
    
    private class PaperCitations {
        private final int paperId;
        private final List<String> citations;

        public PaperCitations(int paperId, List<String> citations) {
            this.paperId = paperId;
            this.citations = citations;
        }
    }
    
    private void queryCitations() throws IOException {
        
        List<PaperCitations> papers = new ArrayList<>();
        try (CSVParser parser = new CSVParser(new FileReader(papersCSV), 
                CSVFormat.RFC4180) ) {
        
            Iterator<CSVRecord> records = parser.iterator();
            // Skip headers.
            records.next();
            
            int total = 0;
            int indexBracketForm = 0;
            int totalIndexBracketCitations = 0;
            int citationMolderErrors = 0;
            int emptyCites = 0;
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                total++;
                
                int paperId = Integer.parseInt(record.get(0));
                String paperText = record.get(6);
                
                // Extract reference section.
                int pos = paperText.lastIndexOf("References", paperText.length() - 1);
                if(pos == -1) {
                    pos = paperText.lastIndexOf("REFERENCES", paperText.length() - 1);
                }

                if(pos != -1) {
                    // Extract reference section.
                    pos = pos + "references".length() + 1; // + 1 to remove the linefeed.

                    String referenceText = paperText.substring(pos).trim();
                    
                    if(isBracketFormat(referenceText)) {
                        indexBracketForm++;
                        
                        // Mold citation strings.
                        List<String> citations = referenceMolder(referenceText);
                        if(!molderError && citations.isEmpty()) {
                            emptyCites++;
                            System.out.println("Empty:");
                            System.out.println("\"" + referenceText + "\"");
//                            
//                            if(emptyCites > 5) {
//                                break;
//                            }
                        }
                        
                        if(molderError) {
                            citationMolderErrors++;
                        } else if(!molderError && !citations.isEmpty()) {
                            // Correct mold.
                            totalIndexBracketCitations+= citations.size();

                            PaperCitations p = new PaperCitations(paperId, citations);
                            papers.add(p);
                        }
                    }
                }
            }
            
            System.out.println("Total papers: " + total);
            System.out.println("Empty Citations: " + emptyCites);
            System.out.println("# index bracket form: " + indexBracketForm);
            System.out.println("# molder errors: " + citationMolderErrors);
            System.out.println("# correctly molded papers: " + papers.size());
            System.out.println("# correctly molded bracket form citations: " + totalIndexBracketCitations);           
        }
        
        // Process paper citations.
        // First give each citations a seperate ID, even though some papers are
        // citing the same paper, give these citations a unique ID, the detection
        // of equal citations will be done later.
        System.out.println("Write Citations CSV");
        long start = System.currentTimeMillis();
        try (CSVPrinter citationPrinter = new CSVPrinter(new FileWriter(citationsCSV), CSVFormat.RFC4180)) {  
            citationPrinter.print("paperID");
            citationPrinter.print("raw_citation");
            citationPrinter.print("authors");
            citationPrinter.print("title");
            citationPrinter.print("valid");
            citationPrinter.println();

            int k = 0;
            int incorrectXML = 0;
            int incorrectParsedXML = 0;
            int unequalCitations = 0;
            // TODO improve this to make API request in batches of fixed size for
            // multiple papers and no seperately for each paper.
            for(PaperCitations pc : papers) {
                k++;
                
                if(k % 200 == 0) {
                    System.out.println("Beat: " + k);
                }                
                
                int paperId = pc.paperId;
                List<String> citations = pc.citations;
                
                String xml = queryFreeCite(citations);
                if(xml == null) {
                    System.err.println("XML == null for paperId: " + paperId);
                    incorrectXML++;
                    continue;
                }
                
                List<ParsedCitation> parsedCitations = parseFreeCiteResponse(xml);
                if(parsedCitations == null) {
                    System.err.println("Failed to parse XML for paperId: " + paperId);
                    incorrectParsedXML++;
                    continue;
                }
                
                if(citations.size() != parsedCitations.size()) {
                    System.err.println("Unequal lists for paperId: " + paperId);
                    unequalCitations++;
                }
                
                for(ParsedCitation parsedCitation : parsedCitations) {
                    citationPrinter.print(paperId);
                    citationPrinter.print(parsedCitation.rawCitation);
                    citationPrinter.print(parsedCitation.authors);
                    citationPrinter.print(parsedCitation.title);
                    citationPrinter.print(parsedCitation.valid);
                    citationPrinter.println();
                }
                
                try {
                    // Sleep 100 milliseconds to not overload the API server.
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CitationsParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println("Incorrect XML: " + incorrectXML);
            System.out.println("Incorrect Parsed XML: " + incorrectParsedXML);
            System.out.println("Unequal Lists: " + unequalCitations);
        }
        long end = System.currentTimeMillis();
        
        System.out.println("Creating citation csv in " + (end - start) + " ms.");
    }
    
    private class CitationRecord {
        private final int paperId;
        private final String rawCitation;
        private final String authors;
        private final String title;
        private final boolean valid;

        public CitationRecord(int paperId, String rawCitation, String authors, String title, boolean valid) {
            this.paperId = paperId;
            this.rawCitation = rawCitation;
            this.authors = authors;
            this.title = title;
            this.valid = valid;
        }   
    }
    
    private void constructCitationGraph() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(citationsCSV), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            List<CitationRecord> citationRecords = new ArrayList<>();
            List<CitationRecord> citationRecordsCopy = new ArrayList<>();
            int totalCitations = 0;
            while(records.hasNext()) {
                CSVRecord record = records.next();
                totalCitations++;
                
                try {
                    int paperId = Integer.parseInt(record.get(0));
                    String rawCitation = record.get(1);
                    String authors = record.get(2);
                    String title = record.get(3);
                    boolean valid = Boolean.valueOf(record.get(4));
                    
                    if(valid) {
                        CitationRecord cr = new CitationRecord(paperId, rawCitation, authors, title, valid);
                        citationRecords.add(cr);                        
                    }
                    
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            
            for(CitationRecord record : citationRecords) {
                citationRecordsCopy.add(record);
            }
            
            List<Integer> list = new ArrayList<>();
            for(int k=0; k<citationRecords.size(); k++) {
                list.add(0);
            }
            
            System.out.println("Total citation records " + totalCitations);
            System.out.println("# valid citation records " + citationRecords.size());
            System.out.println("# invalid citation records " + (totalCitations - citationRecords.size()));
            
            // Process citation records.
            long total = 0;
            long start = System.currentTimeMillis();
            int matches = 0;
            long start2 = System.currentTimeMillis();
            
            for(int i=0; i<citationRecords.size(); i++) {
                int id1 = citationRecords.get(i).paperId;
                String title = citationRecords.get(i).title;
                
//                if(i % 500 == 0) {
//                    System.out.println("IBeat: " + i);
//                }
                
                if(i % 1000 == 0) {
                    long end2 = System.currentTimeMillis();
                    System.out.println("Batch time " + i + ": " + (end2 - start2) + " ms.");
                    
                    start2 = System.currentTimeMillis();
                }
                
                for(int j=i+1; j<citationRecordsCopy.size(); j++) {
                    int id2 = citationRecordsCopy.get(j).paperId;
                    String title2 = citationRecordsCopy.get(j).title;
                    
                    if(title.equals(title2)) {
                        matches++;
                        list.set(i, list.get(i) + 1);
                    }
                    
//                    if(j % 500 == 0) {
//                        System.out.println("Beat: " + j);
//                    }
                    
//                    if(i == j) {
//                        continue;
//                    }
//                    
//                    if((title.length() / (double) title2.length()) < 0.95) {
//                        continue;
//                    }
                    
//                    if(getLongestCommonSubsequence(title, title2) / (double) Math.max(title.length(), title2.length()) >= 0.95) {
//                        //System.out.println("Potential match t1: \"" + title + "\" and t2: \"" + title2 + "\" and " + id1 + " to " + id2);
//                        matches++;
//                    }
                }
                //long end2 = System.currentTimeMillis();
                //System.out.println("Round" + i + ": " + (end2 - start2) + " ms.");
                //total+= (end2 - start2);
                //System.out.println("Expected time: " + ((total / (double) (i + 1)) * citationRecords.size()) / 1000.0 + " s.");
            }
            long end = System.currentTimeMillis();
            
            System.out.println("Matches: " + matches);
            System.out.println("Execution Time: " + (end - start) + " ms.");
            
            for(int b=0; b<list.size(); b++) {
                System.out.println(b + ". " + list.get(b));
            }            
        }
    }
    
    private void constructCitationGraphBetter() throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(citationsCSV), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            List<CitationRecord> citationRecords = new ArrayList<>();
            List<CitationRecord> citationRecordsCopy = new ArrayList<>();
            int totalCitations = 0;
            while(records.hasNext()) {
                CSVRecord record = records.next();
                totalCitations++;
                
                try {
                    int paperId = Integer.parseInt(record.get(0));
                    String rawCitation = record.get(1);
                    String authors = record.get(2);
                    String title = record.get(3);
                    boolean valid = Boolean.valueOf(record.get(4));
                    
                    if(valid) {
                        CitationRecord cr = new CitationRecord(paperId, rawCitation, authors, title, valid);
                        citationRecords.add(cr);                        
                    }
                    
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total citation records " + totalCitations);
            System.out.println("# valid citation records " + citationRecords.size());
            System.out.println("# invalid citation records " + (totalCitations - citationRecords.size()));
            
            citationRecords = equalityCheck(citationRecords);
            System.out.println("citation records " + citationRecords.size());
            
            System.out.println("Matching...");
            List<CitationRecord> uniqueCitations = new ArrayList<>();
            long matches = 0;
            long nonMatches = 0;
            boolean streak = false;
            for(int i=0; i<citationRecords.size(); i++) {
                CitationRecord cr = citationRecords.get(i);
                
                if(i < (citationRecords.size() - 1)) {
                    CitationRecord crNext = citationRecords.get(i + 1);
                    
                    if(cr.title.equals(crNext.title)) {
                        matches++;
                        // Only add the first of the duplicates.
                        if(!streak) {
                            uniqueCitations.add(cr);
                        }
                        
                        streak = true;
                    } else {
                        // If the current is not the last of the previous
                        // duplicates than add it to unique.
                        if(!streak) {
                            uniqueCitations.add(cr);
                        }
                        
                        nonMatches++;
                        streak = false;
                    }
                } else if(!streak) {
                    nonMatches++;
                    uniqueCitations.add(cr);
                } else {
                    matches++; // The last citation is also a duplicate.
                }
            }
            System.out.println("matches: " + matches);
            System.out.println("Unique Citations: " + uniqueCitations.size());
            
//            Set<String> set = new HashSet<>();
//            for(CitationRecord cr : citationRecords) {
//                set.add(cr.title);
//            }

            try (CSVPrinter printer = new CSVPrinter(new FileWriter("citations\\unique_citations.csv"), CSVFormat.RFC4180)) {
                // Print headers.
                printer.print("citationID");
                printer.print("authors");
                printer.print("title");
                printer.println();
                
                for(int i=0; i<uniqueCitations.size(); i++) {
                    CitationRecord cr = uniqueCitations.get(i);
                    printer.print(i);
                    printer.print(cr.authors);
                    printer.print(cr.title);
                    // TODO may also store the paperIDs from which this citation
                    // originates.
                    printer.println();
                }
            }

            try (CSVPrinter printer = new CSVPrinter(new FileWriter("citations\\paper_citations.csv"), CSVFormat.RFC4180)) {
                // Print headers.
                printer.print("paperID");
                printer.print("citationID");
                printer.println();
                
                // Assign a unique ID to each citation title.
                for(int i=0; i<citationRecords.size(); i++) {
                    CitationRecord cr = citationRecords.get(i);
                    
                    // Find matching ID in the unqiue citations.
                    boolean match = false;
                    for(int j=0; j<uniqueCitations.size(); j++) {
                        CitationRecord unique = uniqueCitations.get(j);

                        if(cr.title.equals(unique.title)) {
                            match = true;
                            printer.print(cr.paperId);
                            printer.print(j); // citation ID
                            printer.println();
                            break;
                        }
                    }
                    
                    if(!match) {
                        System.err.println("No match found for citation record.");
                    }
                }
            }
            
            System.out.println("Finished.");
        }
    }
    
    private List<CitationRecord> equalityCheck(List<CitationRecord> list) {
        long start = System.currentTimeMillis();
        Collections.sort(list, new Comparator<CitationsParser.CitationRecord>() {
            @Override
            public int compare(CitationRecord o1, CitationRecord o2) {
                return o1.title.compareTo(o2.title);
            }
        });
        
        long end = System.currentTimeMillis();
        System.out.println("Sorting done in " + (end - start) + " ms.");
        
        return list;
    }
    
    private void exactMatches() throws IOException {
        List<UniqueCitation> uniqueCitations = new ArrayList<>();
        try(CSVParser parser = new CSVParser(new FileReader("citations\\unique_citations.csv"), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            int shortTitles = 0;
            while(records.hasNext()) {
                CSVRecord record = records.next();
                
                int paperId = Integer.parseInt(record.get(0));
                String authors = record.get(1);
                String title = record.get(2);
                
                if(title.length() <= 10) {
                    shortTitles++;
                    //System.out.println(title);
                }
                
                UniqueCitation uc = new UniqueCitation(paperId, authors, title);
                uniqueCitations.add(uc);
            }
            
            System.out.println("Short Titles: " + shortTitles);
        }
        
        // Read in the original NIPS Papers titles.
        List<TitleRecord> titles = new ArrayList<>();
        try(CSVParser parser = new CSVParser(new FileReader("titles.csv"), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                
                int paperId = Integer.parseInt(record.get(0));
                int year = Integer.parseInt(record.get(1));
                String title = record.get(2);
                
                TitleRecord tr = new TitleRecord(paperId, year, title);
                titles.add(tr);
            }
        }
        
        //HashMap<String, Integer> titles = readTitles();
        //System.out.println("# titles " + titles.size());
        
        System.out.println("# title records " + titles.size());
        
        System.out.println("Unique Citations: " + uniqueCitations.size());
        long start = System.currentTimeMillis();
        
        List<MatchResult> results = new ArrayList<>();
        //int matches = 0;
        for(int i=0; i<uniqueCitations.size(); i++) {
            String title = uniqueCitations.get(i).title;
            
            // Check if it is an exact match.
            //if(titles.containsKey(title)) {
            //    matches++;
            //}
            
            if(i % 1000 == 0) {
                System.out.println("BEEP " + i);
            }
            
            // Try similarity matching.
            List<Integer> matchedPapers = new ArrayList<>();
            for(int j=0; j<titles.size(); j++) {
                TitleRecord tr = titles.get(j);
                String nipsTitle = tr.title;
                
                if(title.length() < nipsTitle.length() && (double) title.length() / (double) nipsTitle.length() < 0.8
                        || title.length() >= nipsTitle.length() && (double) nipsTitle.length() / (double) title.length() < 0.8) {
                    continue;
                }
                
                if(getLongestCommonSubsequence(title, nipsTitle) / (double) Math.max(title.length(), nipsTitle.length()) >= 0.8) {
                    int paperId = tr.paperId;
                    matchedPapers.add(paperId);
                }
            }
            
            // Add to match results.
            MatchResult mr = new MatchResult(uniqueCitations.get(i).citationId, matchedPapers);
            results.add(mr);
        }
        
        //System.out.println("Exact matches: " + matches);
        
        long end = System.currentTimeMillis();
        System.out.println("Execution Time: " + (end - start) + " ms.");
        
        System.out.println("Write out results.");
        System.out.println("# match results: " + results.size());
        
        try(CSVPrinter printer = new CSVPrinter(new FileWriter("citations\\citations_matches.csv"), CSVFormat.RFC4180)) {
            // Print headers.
            printer.print("citationId,paperIds");
            printer.println();
            
            for(MatchResult result : results) {
                int citationId = result.citationId;
                List<Integer> matches = result.paperIds;
                StringBuilder matchStr = new StringBuilder();
                for(int i=0; i<matches.size(); i++) {
                    matchStr.append(matches.get(i));
                    if(i < (matches.size() - 1)) {
                        matchStr.append(" ");
                    }
                }
                
                printer.print(citationId);
                printer.print(matchStr);
                printer.println();
            }
        }
        
        System.out.println("Finished.");
    }
    
    private class UniqueCitation {
        private final int citationId;
        private final String authors;
        private final String title;

        public UniqueCitation(int citationId, String authors, String title) {
            this.citationId = citationId;
            this.authors = authors;
            this.title = title;
        }
    }
    
    private class TitleRecord {
        private final int paperId;
        private final int year;
        private final String title;

        public TitleRecord(int paperId, int year, String title) {
            this.paperId = paperId;
            this.year = year;
            this.title = title;
        }
    }
    
    private class MatchResult {
        private final int citationId;
        private final List<Integer> paperIds;

        public MatchResult(int citationId, List<Integer> paperIds) {
            this.citationId = citationId;
            this.paperIds = paperIds;
        }
    }
    
    private void citationsResults() throws IOException {
        List<MatchResult> results = new ArrayList<>();
        try(CSVParser parser = new CSVParser(new FileReader("citations\\citations_matches.csv"), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                
                int citationId = Integer.parseInt(record.get(0));
                String papers = record.get(1);
                String[] paperIds = papers.split(" ");
                
                List<Integer> ids = new ArrayList<>();
                for(String paperId : paperIds) {
                    if(!paperId.isEmpty()) {
                        int id = Integer.parseInt(paperId);
                        ids.add(id);
                    }
                }
                
                MatchResult mr = new MatchResult(citationId, ids);
                results.add(mr);
            }
        }
        
        System.out.println("# results " + results.size());
        int noMatches = 0;
        int moreThanOneMatch = 0;
        for(MatchResult mr : results) {
            if(mr.paperIds.isEmpty()) {
                noMatches++;
            }
            
            if(mr.paperIds.size() > 1) {
                moreThanOneMatch++;
                System.out.println("More than one match for " + mr.citationId);
            }
        }
        
        System.out.println("Matches " + (results.size() - noMatches));
        System.out.println("No matches " + noMatches);
        System.out.println("More than one match " + moreThanOneMatch);
        
        
        HashMap<String, Integer> titleToId = readTitles();
        HashMap<Integer, String> titles = new HashMap<>();
        for(HashMap.Entry<String, Integer> entry : titleToId.entrySet()) {
            titles.put(entry.getValue(), entry.getKey());
        }
        
        // Mapping from paperId to citations Ids
        HashMap<Integer, Set<Integer>> map = new HashMap<>();
        try(CSVParser parser = new CSVParser(new FileReader("citations\\paper_citations.csv"), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                int paperId = Integer.parseInt(record.get(0));
                int citationId = Integer.parseInt(record.get(1));
                
                Set<Integer> set;
                if(map.containsKey(paperId)) {
                    set = map.get(paperId);
                } else {
                    set = new HashSet<>();
                    map.put(paperId, set);
                }
                
                set.add(citationId);
            }
        }
        
        // Mapping from citationIds to paperIds
        HashMap<Integer, Set<Integer>> citationToPaper = new HashMap<>();
        for(MatchResult mr : results) {
            Set<Integer> set;
            if(citationToPaper.containsKey(mr.citationId)) {
                set = citationToPaper.get(mr.citationId);
            } else {
                set = new HashSet<>();
                citationToPaper.put(mr.citationId, set);
            }
            
            set.addAll(mr.paperIds);
        }
        
        
        // Mapping from paperIds to paperIds
        HashMap<Integer, Set<Integer>> graph = new HashMap<>();        
        for(HashMap.Entry<Integer, Set<Integer>> entry : map.entrySet()) {
            int paperId = entry.getKey();
            Set<Integer> citationsIds = entry.getValue();
            
            for(int citationId : citationsIds) {
                
                Set<Integer> set;
                if(graph.containsKey(paperId)) {
                    set = graph.get(paperId);
                } else {
                    set = new HashSet<>();
                    graph.put(paperId, set);
                }
                
                Set<Integer> paperIdTargets = citationToPaper.get(citationId);
                if(paperIdTargets == null) {
                    System.out.println("paperIds null for citationId " + citationId);
                }
                
                for(int paperIdTarget : paperIdTargets) {
                    set.add(paperIdTarget);
                }
            }
        }
        
        try (CSVPrinter printer = new CSVPrinter(new FileWriter("citations\\citations_graph.csv"), CSVFormat.RFC4180)) {
            // Print headers.
            printer.print("paperIdSource");
            printer.print("paperIdTarget");
            printer.println();
            
            for(HashMap.Entry<Integer, Set<Integer>> entry : graph.entrySet()) {
                int source = entry.getKey();
                Set<Integer> targets = entry.getValue();
                
                for(int target : targets) {
                    printer.print(source);
                    printer.print(target);
                    printer.println();
                }
            }
        }
        
        System.out.println("Constructed Graph");
    }
    
    private void citationsDuplicates() throws IOException {
        try(CSVParser parser = new CSVParser(new FileReader("citations\\unique_citations.csv"), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            int total = 0;
            Set<String> citationTitles = new HashSet<>();
            while(records.hasNext()) {
                CSVRecord record = records.next();
                total++;
                
                int citationId = Integer.parseInt(record.get(0));
                String authors = record.get(1);
                String title = record.get(2);
                
                citationTitles.add(title.trim().toLowerCase());
            }
            
            System.out.println("Total " + total);
            System.out.println("# unique " + citationTitles.size());
        }
    }
    
    private void analyzeCitationGraph() throws IOException {
        HashMap<Integer, Set<Integer>> graph = new HashMap<>();
        try(CSVParser parser = new CSVParser(new FileReader("citations\\citations_graph.csv"), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                
                int sourceId = Integer.parseInt(record.get(0));
                int targetId = Integer.parseInt(record.get(1));
                
                Set<Integer> set;
                if(graph.containsKey(sourceId)) {
                    set = graph.get(sourceId);
                } else {
                    set = new HashSet<>();
                    graph.put(sourceId, set);
                }
                
                set.add(targetId);
            }
        }
        
        // Look which NIPS papers do not reference other NIPS papers.
        HashMap<String, Integer> titleToId = readTitles();
        
        Set<Integer> paperIds = new HashSet<>();
        for(HashMap.Entry<String, Integer> entry : titleToId.entrySet()) {
            paperIds.add(entry.getValue());
        }
        
        System.out.println("Paper Ids: " + paperIds.size());
        
        for(HashMap.Entry<Integer, Set<Integer>> entry : graph.entrySet()) {
            int sourceId = entry.getKey();
            paperIds.remove(sourceId);
            //paperIds.removeAll(entry.getValue());
        }
        
        // Check which papers are not referencing another NIPS paper.
        System.out.println("Paper Ids left: " + paperIds.size());
    }
    
    private HashMap<Integer, Set<Integer>> readGraph() throws IOException {
        HashMap<Integer, Set<Integer>> adjacencyMap = new HashMap<>();
        
        try(CSVParser parser = new CSVParser(new FileReader("citations\\citations_graph.csv "), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                int sourceId = Integer.parseInt(record.get(0));
                int targetId = Integer.parseInt(record.get(1));
                
                Set<Integer> set;
                if(adjacencyMap.containsKey(sourceId)) {
                    set = adjacencyMap.get(sourceId);
                } else {
                    set = new HashSet<>();
                    adjacencyMap.put(sourceId, set);
                }
                set.add(targetId);
            }
        }
        
        return adjacencyMap;
    }
    
    private void buildGraph() throws IOException {
        HashMap<String, Integer> titleToId = readTitles();
        
        Set<Integer> paperIds = new HashSet<>();
        for(HashMap.Entry<String, Integer> entry : titleToId.entrySet()) {
            paperIds.add(entry.getValue());
        }
        System.out.println("# papers " + paperIds.size());
        
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int id : paperIds) {
            if(id > max) {
                max = id;
            }
            
            if(id < min) {
                min = id;
            }
        }
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        
        Set<Integer> completeRange = new HashSet<>();
        for(int k=min; k<=max; k++) {
            completeRange.add(k);
        }
        
        completeRange.removeAll(paperIds);
        System.out.println("Missing range elements: " + completeRange.size());
        
        
        HashMap<Integer, Set<Integer>> adjacencyMap = readGraph();
        System.out.println("Adjacency Map Size: " + adjacencyMap.size());
        
        matrixTest(adjacencyMap);
        
        System.out.println("End Matrix Test");
        
        HashMap<Integer, Set<Integer>> invertedAdjacencyMap = new HashMap<>();
        for(HashMap.Entry<Integer, Set<Integer>> entry : adjacencyMap.entrySet()) {
            for(int target : entry.getValue()) {
                
                Set<Integer> set;
                if(invertedAdjacencyMap.containsKey(target)) {
                    set = invertedAdjacencyMap.get(target);
                } else {
                    set = new HashSet<>();
                    invertedAdjacencyMap.put(target, set);
                }
                
                set.add(entry.getKey());
            }
        }
        
        List<NodeRank> nodeRanks = new ArrayList<>();
        for(HashMap.Entry<Integer, Set<Integer>> entry : invertedAdjacencyMap.entrySet()) {
            NodeRank nr = new NodeRank(entry.getKey(), entry.getValue());
            nodeRanks.add(nr);
        }
        
        Collections.sort(nodeRanks, new Comparator<NodeRank>() {
            @Override
            public int compare(NodeRank o1, NodeRank o2) {
                if(o1.neighbours.size() > o2.neighbours.size()) {
                    return -1;
                } else if(o1.neighbours.size() == o2.neighbours.size()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        
        for(int m=0; m<20; m++) {
            NodeRank nr = nodeRanks.get(m);
            System.out.println("paperId=" + nr.paperId + " has " + nr.neighbours.size() + " neighbours.");
        }
        
        
        System.out.println("Construct sparse matrix.");
        SparseMatrix sp = new SparseMatrix((int) (1.4 * max), max, max);
        
        System.out.println("Matrix Size: " + sp.getMatrix().size());
        
        HashSet<Integer> targets = new HashSet<>();
        // Add values to sparse matrix.
        for(HashMap.Entry<Integer, Set<Integer>> entry : adjacencyMap.entrySet()) {
           int key = entry.getKey();
           Set<Integer> group = entry.getValue();
           for(Integer i : group) {
               // - 1 to aligen the ids to be zero-indexed.
               sp.addValue(i - 1, key - 1, 1 / ((double) group.size()));
           }
           
           targets.addAll(group);
        }
        
        System.out.println("Targets Size: " + targets.size());
        
        System.out.println("Filled matrix size: " + sp.getMatrix().size());
        
        SparseMatrix vector = new SparseMatrix((int) (1.4 * max), 
                1, max);
        vector.addValue(0, 0, 1d);
        
        System.out.println("compute page rank");
        long start = System.currentTimeMillis();
        vector = sp.powerMethodIteration(vector, 50);
        long end = System.currentTimeMillis();
        
        System.out.println("PageRank computation in " + (end - start) + " ms.");
        
        System.out.println("Vector Size: " + vector.getMatrix().size());
        
        List<PaperRank> ranks = new ArrayList<>();
        for(int paperId : paperIds) {
            HashMap<Integer, Double> row = vector.getMatrix().get(paperId - 1);
            
            if(row != null) {
                if(row.size() > 1) {
                    System.err.println("Row size > 1");
                }

                double value = row.get(0);

                PaperRank pr = new PaperRank(paperId, value);
                ranks.add(pr);
            } else {
                PaperRank pr = new PaperRank(paperId, 0);
                ranks.add(pr);
            }
        }
        
        System.out.println("# ranks " + ranks.size());
        Collections.sort(ranks, new Comparator<PaperRank>() {
            @Override
            public int compare(PaperRank o1, PaperRank o2) {
                if(o1.pageRankScore > o2.pageRankScore) {
                    return -1;
                } else if(o1.pageRankScore == o2.pageRankScore) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        
        try(CSVPrinter printer = new CSVPrinter(new FileWriter("papers_pagerank3.csv"), CSVFormat.RFC4180)) {
            // Write headers.
            printer.print("paperId");
            printer.print("pageRankScore");
            printer.print("inDegree");
            printer.println();
            
            for(PaperRank pr : ranks) {
                printer.print(pr.paperId);
                printer.print(pr.pageRankScore);
                printer.print(invertedAdjacencyMap.get(pr.paperId));
                printer.println();
            }
        }
        
        System.out.println("Finished.");
    }
    
    private class PaperRank {
        private final int paperId;
        private final double pageRankScore;

        public PaperRank(int paperId, double pageRankScore) {
            this.paperId = paperId;
            this.pageRankScore = pageRankScore;
        }
    }
    
    private class NodeRank {
        private final int paperId;
        private final Set<Integer> neighbours;

        public NodeRank(int paperId, Set<Integer> neighbours) {
            this.paperId = paperId;
            this.neighbours = neighbours;
        }
    }
    
    private void matrixTest(HashMap<Integer, Set<Integer>> adjacencyMap) {
        System.out.println("Create large 2D array");
        double[][] matrix = new double[6603][6603];
        
        for(HashMap.Entry<Integer, Set<Integer>> entry : adjacencyMap.entrySet()) {
            
            int from = entry.getKey() - 1;
            
            for(int target : entry.getValue()) {
                int to = target - 1;
                
                matrix[to][from] = 1 / (double) entry.getValue().size();
            }
        }
        
        for(int i=0; i<matrix.length; i++) {
            // Check if row is empty.
            boolean isEmpty = true;
            int nonEmpty = 0;
            for(int j=0; j<matrix[i].length; j++) {
                if(matrix[i][j] != 0) {
                    isEmpty = false;
                    nonEmpty++;
                }
            }
            
            if(isEmpty) {
                int width = matrix[i].length;
                // Equal chance to go to any other node.
                for(int j=0; j<matrix[i].length; j++) {
                    matrix[i][j] = 1 / (double) width;
                }
            } else {
                int width = matrix[i].length;
                for(int j=0; j<matrix[i].length; j++) {
                    if(matrix[i][j] == 0) {
                        matrix[i][j] = (0.15 * (1 / (width - nonEmpty)));
                    } else {
                        matrix[i][j] = 0.85 * matrix[i][j];
                    }
                }
            }
        }
        
        double[] vector = new double[6603];
        vector[0] = 1;
        
        long start = System.currentTimeMillis();
        for(int i=0; i<50; i++) {
            vector = SparseMatrix.matrixPowerMethod(matrix, vector);
        }
        long end = System.currentTimeMillis();
        
        int nonEmpty = 0;
        for(int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix.length; j++) {
                if(matrix[i][j] > 0) {
                    nonEmpty++;
                }
            }
        }
        System.out.println("nonEmpty: " + nonEmpty);
        
        System.out.println("Time: " + (end - start) + " ms.");
        
        List<PaperRank> ranks = new ArrayList<>();
        for(int k=0; k<vector.length; k++) {
            PaperRank pr = new PaperRank(k + 1, vector[k]);
            ranks.add(pr);
        }
        
        System.out.println("Ranks: " + ranks.size());
        int nonEmptyVec = 0;
        for(int i=0; i<vector.length; i++) {
            if(vector[i] > 0) {
                nonEmptyVec++;
            }
        }
        
        System.out.println("nonEmptyVec: " + nonEmptyVec);
        
        Collections.sort(ranks, new Comparator<CitationsParser.PaperRank>() {
            @Override
            public int compare(PaperRank o1, PaperRank o2) {
                if(o1.pageRankScore > o2.pageRankScore) {
                    return -1;
                } else if(o1.pageRankScore == o2.pageRankScore) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        
        HashMap<String, Integer> titlesToId = readTitles();
        HashMap<Integer, String> idToTitles = new HashMap<>();
        for(HashMap.Entry<String, Integer> entry : titlesToId.entrySet()) {
            idToTitles.put(entry.getValue(), entry.getKey());
        }
        
        for(int m=0; m<20; m++) {
            //System.out.println("Rank" + m + ": " + ranks.get(m).paperId + " score= " + ranks.get(m).pageRankScore);
            System.out.println((m + 1) + ". " + idToTitles.get(ranks.get(m).paperId));
        }
    }
    
    private void graphStats() throws IOException {
        System.out.println("Compute stats");
        HashMap<Integer, Set<Integer>> adjacencyMap = readGraph();
        Set<Integer> nodes = new HashSet<>();
        for(HashMap.Entry<Integer, Set<Integer>> entry : adjacencyMap.entrySet()) {
            nodes.add(entry.getKey());
            nodes.addAll(entry.getValue());
        }
        
        System.out.println("# nodes " + nodes.size());
        
        matrixTest(adjacencyMap);
    }
    
    private HashMap<Integer, String> getAuthors() throws IOException {
        HashMap<Integer, String> authors = new HashMap<>();
        
        try(CSVParser parser = new CSVParser(new FileReader(authorsCSV), CSVFormat.RFC4180)) {
            Iterator<CSVRecord> records = parser.iterator();
            
            // Skip headers.
            records.next();
            
            while(records.hasNext()) {
                CSVRecord record = records.next();
                int authorId = Integer.parseInt(record.get(0));
                String authorName = record.get(1);
                
                authors.put(authorId, authorName);
            }
        }
        return authors;
    }
    
    private void authorsStats() throws IOException {
        HashMap<Integer, String> authors = getAuthors();
        System.out.println("Authors: " + authors.size());
        
        List<String> lines = Files.readAllLines(Paths.get("out.txt"));
        
        for(int i=0; i<20; i++) {
            String[] splits = lines.get(i).split(" ");
            int authorId = Integer.parseInt(splits[0]);
            
            System.out.println(authors.get(authorId));
        }
    }
}
