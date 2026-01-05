package sample.evaluation.elasticsearch;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.stream.Collectors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.HashSet;
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
import sample.evaluation.util.SampleFileReader;
import sample.evaluation.metrics.MetricsCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.CSVWriterUtil;
import sample.evaluation.util.TypeClones;


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
	public Map<String, Map<String, Float>> evaluate(List<File> sampledFiles, List<File> sexprFiles, List<File> funStrFiles, int numDocs) {
		MoreLikeThisSearch esSimilarity = new MoreLikeThisSearch(this.client);
		//esSimilarity.deleteIndex(this.indexName);
		//esSimilarity.populateIndex(funStrDir,this.indexName, this.client);
		esSimilarity.findSimilarDocs(numDocs,this.indexName,funStrFiles,sexprFiles,sampledFiles);
		Map<String, Map<String, Float>> similarityScores = esSimilarity.getSimMap().getSimilarityScores();    	    
	  return similarityScores; 
	}
	 
	
	 public static void evaluateOnSeed(long seed) {
 		String technique = "elasticsearch_evaluation_random_sample_RQ2";
		String rqNum = "RQ2";
		String subDir = "0";  
		String cloneType = "T3";
		int sampleSize = 100;
		int numDocs = 100;
		String mainDir   = System.getProperty("user.home") + "/treekernel-jss2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   =  mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codesDir  = dataDir + "bcb_reduced/"+subDir+"/";	
		String funStr      = dataDir + "functionStr/"+subDir+"/";
		String sexprDir    = dataDir + "sexpr_from_gumtree/"+subDir+"/";
		String resultDir   = mainDir + "results/RQ2/"; 
		
		Path newBCDirPath = Paths.get(System.getProperty("user.home"),
									"treekernel-jss2025",
									"data",
									"BigCloneEval",
									"bigclone_groundtruth_v3");

		
		System.out.println("Type:"+cloneType);
		String typewiseFile = NewSampler.resolveRefFile(cloneType, newBCDirPath);
			
		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
    Set<String> typewiseClones = new HashSet<String>(); 
    		
		try {
			refClones = ReferenceClones.lookupClonePairs(typewiseFile,codesDir);//corrected
			typewiseClones = TypeClones.getTypeWiseFiles(typewiseFile, codesDir) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Type wise clones size:"+typewiseClones.size());
		System.out.println("Ref clones map size:"+refClones.size());	

		List<File> funStrFiles = FileMapper.getAllFiles( new File(funStr));  //Get all files recursively	    	
		List<File> sexprFiles  = FileMapper.getAllFiles(new File(sexprDir)); 
		List<File> finalSample = SampleFileReader.readFileNames(resultDir+"/sampled/T3_"+seed+".txt");

		/*NewSampler.sampleTypewisePositiveSample(refClones, typewiseClones, 
																		sexprFiles, sampleSize, seed);*/
		String outputFinalSample = resultDir+cloneType+"_"+seed+".txt";
		NewSampler.saveFinalSample(finalSample, outputFinalSample);
	
		Map<String, ArrayList<String>> sampledClones  = new HashMap<String, ArrayList<String>>();
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
		System.out.println("Calculating metrics now!");

		MetricsCalculator.computeAndSaveMetrics("TF-IDF", resultDir,
												similarityScores, sampledClones,
												totalTime, seed, technique, rqNum, 
												cloneType, numDocs);													
	  }
	 
	
	public static void main(String[] args) {
	 	long [] all_seeds= {8241};//{6251,9080,8241,8828,55,2084,1375,2802,3501,3389}; //from Util.generate_seed();
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
	}
}
