package sample.evaluation.eskelp;

import static org.junit.Assert.assertNotEquals;
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

import sample.evaluation.util.*;
import sample.evaluation.kelp.SimilarityMap;
import sample.evaluation.kelp.TKSimilarity;
import sample.evaluation.metrics.MetricsCalculator;

public class ElasticsearchKelp implements AutoCloseable {
	private String indexName;
	private final RestHighLevelClient client;

    public ElasticsearchKelp(String indexName) {
        this.client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http"))
        );
		    this.indexName = indexName; 
    }

	@Override
    public void close() throws IOException {
        client.close();
    }

	public SimilarityMap evaluate(List<File> funStrFiles, List<File> sampledFiles, String funStr, 
	                                      String sexprDir, List<File> sexprFiles, String kernel, int numDocs){
		
		  ESKelpSimilarity esSimilarity = new ESKelpSimilarity(this.client, kernel);
		  //esSimilarity.deleteIndex(this.indexName);
		  //esSimilarity.populateIndex(funStr, sexprDir, this.indexName, this.client);
		  esSimilarity.findSimilarDocs(numDocs, this.indexName, funStrFiles, sampledFiles, sexprDir, sexprFiles, kernel);
		  return esSimilarity.tk.getSimMap();
	}
	 
	public static void evaluateOnSeed(long seed) {
		String subDir = "0";  
 		float threshold = 0.3f;
 		String kernel = "STK";
 		int sampleSize = 100;
 		int numDocs  = 1000; //thousand 
 		String rqNum = "RQ4";
 		String technique  = "es_kelp_evaluation_random_sample";
 		//String indexName = "bigclone_index_0_for_sampled_evaluation";
	  	String indexName = "bigclone_index_0_only_parsable_files";
		String userhome  = System.getProperty("user.home");
		String mainDir   = userhome + "/treekernel-jss2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   = mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir  = dataDir + "bcb_reduced/"+subDir+"/";
		String funStr    = dataDir + "functionStr/"+subDir+"/";
		String sexprDir  = dataDir + "sexpr_from_gumtree/"+subDir+"/";
		String resultDir = mainDir + "results/" + rqNum + "_v3/";
		String simFile   = resultDir+ kernel+"_simscores_" + seed+".csv";  
		String normSimFile = resultDir+ kernel+"_norm_simscores_" + seed+".csv";
		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
		System.out.println("Seed:"+seed+" numDocs:"+numDocs);
		try {
			  refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
		} catch (Exception e) {
			  e.printStackTrace();
		}

	  System.out.println("Ref clones map size:"+refClones.size());
	  
		File funStrFolder = new File(funStr);
		List<File> funStrFiles = FileMapper.getAllFiles(funStrFolder); // Get all files recursively	
		assertNotEquals("Error: Dir shall have some files!",funStrFiles.size(),0);
    	
	 	List<File> sexprFiles = FileMapper.getAllFiles(new File(sexprDir));
		List<File> finalSample = NewSampler.selectPositiveSample(sexprFiles, refClones, seed, sampleSize);
		String outputfile = resultDir+seed+".txt";
		//NewSampler.saveFinalSample(finalSample,outputfile);
		
		System.out.println("Final sample size: " + finalSample.size());
    	Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
		long startTime = System.currentTimeMillis();
		SimilarityMap simMap;

		try(ElasticsearchKelp evalobj = new ElasticsearchKelp(indexName)) {
			  simMap = evalobj.evaluate(funStrFiles, finalSample, funStr, sexprDir, sexprFiles, kernel, numDocs );
		  } catch (IOException e) {
      		throw new RuntimeException("Failed to close Elasticsearch client", e);
		  }
		
		
		Map<String, Map<String, Float>> similarityScores = simMap.getSimilarityScores();
		Map<String, Float> selfSimilarityScores = simMap.getSelfSimilarities();		
		System.out.println("Self Similarity Scores size:"+selfSimilarityScores.size());
		//Util.saveSimilarityScores(similarityScores,simFile);
		
		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime)/1000; // millisecs to seconds
		System.out.println("Sim scores map size :"+similarityScores.size()); 
		System.out.println("Total time:"+totalTime); 
		System.out.println("Calculating metrics now!");
		
		MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDir,
        										similarityScores, sampledClones, threshold,
        								    totalTime, seed, technique, rqNum, "not normalized",numDocs);

		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDir,
       								 			similarityScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "not normalized",numDocs);

		MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDir,
												similarityScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "not normalized",numDocs);

		MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDir,
												similarityScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "not normalized",numDocs);

		//NORMALIZATION
		Map<String, Map<String, Float>> normScores = NormalizedScore.computeNormalizedScores(similarityScores, selfSimilarityScores);
		//Util.saveSimilarityScores(normScores,normSimFile);
		
		MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDir,
        										normScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "normalized", numDocs);

		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDir,
       								 			normScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "normalized",numDocs);

		MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDir,
												normScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "normalized",numDocs);

		MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDir,
												normScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "normalized",numDocs);
		
	}
	
	public static void main(String[] args) {

		long [] all_seeds= {6251, 9080, 8241, 8828, 55, 2084, 1375, 2802, 3501, 3389}; //from Util.generate_seed();  
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
		
	}
}
