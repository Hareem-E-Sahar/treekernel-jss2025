package sample.evaluation.elasticsearch;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.apache.http.HttpHost;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;


public class ESFileIndexer {
    private RestHighLevelClient client;
    public ESFileIndexer(RestHighLevelClient client) {
        this.client = client;
    }

   
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }


    public void indexFile(String indexName, Path filePath) throws IOException {
        // Read file content
        String content = new String(Files.readAllBytes(filePath));

        // Create a unique ID for each document
        String documentId = UUID.randomUUID().toString();

        // Create a map for the document source
        Map<String, Object> document = new HashMap<>();
        document.put("content", content);
        document.put("filepath",filePath.toString());
        //document.put("filename", filePath.getFileName().toString());

        // Create an index request
        IndexRequest indexRequest = new IndexRequest(indexName)
                .id(documentId)
                .source(document); // Use a map as the document source

        // Index the document and print response
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("Indexed file: " + filePath.toAbsolutePath());
        System.out.println("Response: " + indexResponse.toString());
    }
    
    
    private void processDirectory(Path funStr, String sexprDir, String indexName) throws IOException {
    		// Load all parsed filenames into a Set<String>
        Set<String> sexprFiles = Arrays.stream(new File(sexprDir).listFiles())
                                   .map(f -> f.getName())
                                   .collect(Collectors.toSet());

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(funStr)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    processDirectory(path, sexprDir, indexName); // Recurse to subdirectories
                } 

                String fileName   = path.getFileName().toString();    // Foo.java
                //only index if it is a parsable file
                if (sexprFiles.contains(fileName)) {
                    indexFile(indexName, path);
                }  
            }
        }
    }
    
    public void indexDocs(String funStr, String sexprDir, String indexName) {
    	 try {
             // Directory containing the files to index
             Path dirPath = Paths.get(funStr);
             this.processDirectory(dirPath, sexprDir, indexName);
         } catch (IOException e) {
             e.printStackTrace();
         } 
    }

    public static void main(String[] args) {
    	RestHighLevelClient client = new RestHighLevelClient(
                 RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
      ESFileIndexer fileIndexer = new ESFileIndexer(client);
    	//String indexName = "bigclone_index_0_for_sampled_evaluation";
    	String indexName = "bigclone_index_0_only_parsable_files";

     String funStr = Paths.get(System.getProperty("user.home"),"treekernel-jss2025","data",
                  "BigCloneEval","ijadataset","functionStr","0").toString();
      
      String sexprDir = Paths.get(System.getProperty("user.home"),"treekernel-jss2025","data",
                  "BigCloneEval","ijadataset","sexpr_from_gumtree","0").toString();
        

    	
    	fileIndexer.indexDocs(funStr, sexprDir, indexName);
        try {
            fileIndexer.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
