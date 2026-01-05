package sample.evaluation.eskelp;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import sample.evaluation.elasticsearch.ESFileIndexer;
import sample.evaluation.kelp.SimilarityMap;
import sample.evaluation.kelp.TKSimilarity;
import sample.evaluation.util.FileMapper;
import sample.evaluation.util.Preprocessor;

import org.elasticsearch.search.SearchHit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ESKelpSimilarity {
    @SuppressWarnings("deprecation")
	private RestHighLevelClient client;
    TKSimilarity tk;
	
   	
	@SuppressWarnings("deprecation")
	public ESKelpSimilarity(RestHighLevelClient client, String kernelName) {
        this.client = client;
        this.tk = new TKSimilarity(kernelName);
       
    }
    
    public void closeClient() {
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
  @SuppressWarnings("deprecation")
	public void populateIndex(String funStr, String sexprDir, String indexName, RestHighLevelClient client) {
		 ESFileIndexer fileIndexer = new ESFileIndexer(this.client);
		 fileIndexer.indexDocs(funStr, sexprDir, indexName);
	}
    
    public List<SearchHit> executeMLTQuery(int numDocs, String indexName, String queryContent) throws IOException {
        // The fields to search against
        String[] fields = {"content"};

        MoreLikeThisQueryBuilder moreLikeThisQuery = QueryBuilders.moreLikeThisQuery(
            fields, 
            new String[] { queryContent }, 
            null               // Optional query builder for additional parameters
        ).minTermFreq(2)
        .minDocFreq(3)
        .maxQueryTerms(25)
        .minimumShouldMatch("30%");
        
        // Build the search request
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(moreLikeThisQuery);
        sourceBuilder.size(numDocs); 
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // Execute the search
        SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
        return List.of(searchResponse.getHits().getHits());
        
    }
   
    public void findSimilarDocs(int numDocs,String indexName,List<File> funStrFiles,List<File> sampledFiles,String sexprDir,List<File> sexprFiles,String kernel) { 
    	Map<File, File> mapping = FileMapper.getMapping(sexprFiles,funStrFiles);
		  tk.computeSelfSimilarity(sexprFiles);
                    
    	for (File queryfile : sampledFiles) {
    		    System.out.println("\nProcessing file: " + queryfile.getName()+" "+queryfile.getAbsolutePath());
		
            //Need funStr path instead of sexpr path - method2 
            //Optional<File> result = FileMapper.findFileByName(funStrFiles, queryfile.getName());
            //Path esQueryPath = result.orElse(null).toPath();  // Will be null if not found
            
            //Here we need funStr path instead of sexpr path - method1 
            File file = FileMapper.getFileByName(mapping,queryfile.getName()); // gives funstr path
            Path esQueryPath = file.toPath();
             System.out.println(esQueryPath);
            try {
                String queryContent = new String(Files.readAllBytes(esQueryPath), StandardCharsets.UTF_8);
               // queryContent = Preprocessor.removeWrapper(queryContent);

                List<SearchHit> results = executeMLTQuery(numDocs,indexName,queryContent);
                System.out.println("Hits:"+results.size());
                
                for (SearchHit hit : results) { 
                    //System.out.println("Found document with ID: " + hit.getId());
                    //System.out.println("Source: " + hit.getSourceAsString());
                    Map<String, Object> source = hit.getSourceAsMap();
                    String absolutePath = (String) source.get("filepath");
                    Path path = Paths.get(absolutePath);
                    String fileName = path.getFileName().toString();
                    //float esSimilarityScore = hit.getScore(); 
                    
                    if (queryfile.getName().equals(fileName)) {
                        continue;	 //exclude self matches
                    }
                    //KeLP re-ranking 
                    float kelpSimilarity = tk.computeSimilarity(sexprDir+queryfile.getName(),sexprDir+fileName);
                    tk.getSimMap().addSimilarity(queryfile.getName(), fileName, kelpSimilarity);
                    
                     
                }
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }     
    }
    
    @SuppressWarnings("deprecation")
	public void deleteIndex(String indexName) {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        try {
        	AcknowledgedResponse deleteResponse = this.client.indices().delete(request, RequestOptions.DEFAULT);
            System.out.println("Index deleted: " + deleteResponse.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
