package sample.evaluation.elasticsearch;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import sample.evaluation.util.FileMapper;
import sample.evaluation.util.NewSampler;
import sample.evaluation.metrics.MetricsCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.CSVWriterUtil;

public class ElasticsearchBCEval implements AutoCloseable {
	  private String indexName;
		private final RestHighLevelClient client;

    public ElasticsearchBCEval(String indexName) {
        this.client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
		    this.indexName = indexName; 
    }

	  @Override
    public void close() throws IOException {
        client.close();
    }

	 
	public Map<String, Map<String, Float>> evaluate(List<File> sampledFiles, List<File> sexprFiles, List<File> funStrFiles, int numDocs ) {
	
		MoreLikeThisSearch esSimilarity = new MoreLikeThisSearch(this.client);
		//esSimilarity.deleteIndex(this.indexName);
		//esSimilarity.populateIndex(funStrDirPath, this.indexName, this.client);
		esSimilarity.findSimilarDocs(numDocs, this.indexName, sampledFiles, sexprFiles, funStrFiles);
		Map<String, Map<String, Float>> similarityScores = esSimilarity.getSimMap().getSimilarityScores();    	    
	  return similarityScores; 
	}
	 
	
	public static void evaluateOnSeed(long seed) {
 	
 		float threshold   = 0; //threshold is not relevant;
 		String technique = "elasticsearch_evaluation_random_sample";
 		String rqNum = "RQ1";
 		String subDir = "0";  
		int sampleSize = 100;
	  int numDocs = 100;
		String mainDir   = System.getProperty("user.home") + "/treekernel-jss2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   =  mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir  = dataDir + "bcb_reduced/"+subDir+"/";
		String funStrDirPath    = dataDir + "functionStr/"+subDir+"/";
		String sexprDir  = dataDir + "sexpr_from_gumtree/"+subDir+"/";
		String resultDir = mainDir + "results/RQ1/final/";
	
		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
  	try {
  		refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
  	} catch (Exception e) {
	    e.printStackTrace();
    }
			
	  System.out.println("Ref clones map size:"+refClones.size());	
   
	  List<File> funStrFiles = FileMapper.getAllFiles(new File(funStrDirPath));  //Get all files recursively
		assertNotEquals(0,funStrFiles.size());
			    
		ArrayList<File> sexprFiles = new ArrayList<>(Arrays.asList(new File(sexprDir).listFiles()));
		List<File> finalSample = new ArrayList<File>();
    		
		try {
			finalSample = NewSampler.selectPositiveSample(sexprFiles,refClones,seed,sampleSize);
		
			String outputfile = resultDir+String.valueOf(seed)+".txt";
			NewSampler.saveFinalSample(finalSample,outputfile);
	       
		} catch (Exception e) {
			e.printStackTrace();
		}

    Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
		
		long startTime = System.currentTimeMillis();
		//String indexName  = "bigclone_index_0_for_sampled_evaluation";
		String indexName = "bigclone_index_0_only_parsable_files";
		
		Map<String, Map<String, Float>> similarityScores;

		try(ElasticsearchBCEval evalobj = new ElasticsearchBCEval(indexName)) {
			similarityScores = evalobj.evaluate(finalSample, sexprFiles, funStrFiles, numDocs);
		} catch (IOException e) {
    		throw new RuntimeException("Failed to close Elasticsearch client", e);
		}	  
	  
		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime)/1000; 
	    
		System.out.println("Sim scores map size :"+similarityScores.size()); 
		System.out.println("Total time:"+totalTime); 
		System.out.println("=".repeat(90));
		
		System.out.println("Calculating metrics now!");
		MetricsCalculator.computeAndSaveMetrics( "TFIDF", resultDir,
												similarityScores, sampledClones,
												totalTime, seed, technique, rqNum, numDocs);
												
		CSVWriterUtil.saveSimilarityScores(similarityScores,resultDir+"RQ1_elasticsearch_scores_"+seed+".csv");
												
	}
	
	 public static void main(String[] args) {
	 	long [] all_seeds= {6251,9080,8241,8828,55,2084,1375,2802,3501,3389}; //from Util.generate_seed();
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
			System.out.println("=".repeat(80));
			
	 }
}
