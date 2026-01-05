package sample.evaluation.elasticsearch;

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
import sample.evaluation.util.ComplexityUtils;
import sample.evaluation.util.FileMapper;
import sample.evaluation.util.SamplerRQ3;

import sample.evaluation.metrics.MetricsCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import sample.evaluation.util.SampleFileReader;
import sample.evaluation.util.DirectoryProcessor;
import java.util.stream.Collectors;


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

	public Map<String, Map<String, Float>> evaluate( List<File> sampledFiles, List<File> sexprFiles, List<File> funStrFiles, int numDocs) {
		MoreLikeThisSearch esSimilarity = new MoreLikeThisSearch(this.client);
		//esSimilarity.deleteIndex(this.indexName);
		//esSimilarity.populateIndex(funStrDir,this.indexName, this.client);
		
		esSimilarity.findSimilarDocs(numDocs, this.indexName, sampledFiles, sexprFiles, funStrFiles);
		Map<String, Map<String, Float>> similarityScores = esSimilarity.getSimMap().getSimilarityScores();    	    
		//esSimilarity.closeClient();
		return similarityScores; 
	}
	 
	
	public static void evaluateOnSeed(long seed) {
		String rqNum = "RQ3";
		int numDocs = 100;
		String mode = "Large";
		String subDir = "0";	
 		String userhome  = System.getProperty("user.home");
		String mainDir   = userhome + "/treekernel-jss2025/";
 		String dataDir   = mainDir + "data/BigCloneEval/ijadataset/";
 		String refFile   =  mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
 		String codesDir    = dataDir + "bcb_reduced/"+subDir+"/";
		String funStr      = dataDir + "functionStr/"+subDir+"/";
		String sexprDir    = dataDir + "sexpr_from_gumtree/"+subDir+"/";
		String resultDir   = mainDir + "results/"+rqNum+"/";
 		String complexityFile = mainDir + "data/checkstyle_complexity_all.csv";
		String metricsFile = resultDir + "metrics_elasticsearch_"+rqNum+"_"+mode+"_complexity.csv"; 

		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
		try {
			refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Ref clones map size:"+refClones.size());	
	  
	  	File funStrfolder = new File(funStr);
		  List<File> funStrFiles = FileMapper.getAllFiles(funStrfolder);  //Get all files recursively	    
	  	List<File> sexprFiles = DirectoryProcessor.getAllFiles(new File(sexprDir));
		  if (funStrFiles.isEmpty() || sexprFiles.isEmpty())
			  throw new IllegalStateException("No files found");
		
		  int sampleSize = 100;
	  	List<File> finalSample = new ArrayList<>();
	  	if (mode.equals("High") || mode.equals("Low")) {
			  System.out.println("Complexity Eval");
		  	Map<String, Integer> complexityMap = ComplexityUtils.populateComplexityMap(complexityFile);
		
			  //use pre-sampled files from fos or anisha for evaluation

			  String dirPath = resultDir+"complexity/"+mode+"/P14/sampled/";
			  String fileName = seed + "_Complexity_"+mode+".txt";
			  String filePath = dirPath + fileName;
			  finalSample =  SampleFileReader.readFileNames(filePath);
			  finalSample.forEach(f -> System.out.println(f.getAbsolutePath()));
			  //finalSample = SamplerRQ3.sampleComplexitywisePositiveSample(refClones, complexityMap, sexprFiles, sampleSize, seed, mode); 
			  String outputFile = resultDir+seed+"_Complexity_"+mode+"_1.txt";
			  SamplerRQ3.saveFinalSample(finalSample,outputFile);     	
	  
		} else if (mode.equals("Large") || mode.equals("Small")) {
			System.out.println("LOC Eval");
			String dirPath = resultDir+"function_size/"+mode+"/P14/sampled/"; //large
			//String dirPath   = resultDir+"function_size/"+mode+"/sampled/"; //small
			String fileName = seed + "_Size_"+mode+".txt";
			String filePath = dirPath + fileName;
			finalSample =  SampleFileReader.readFileNames(filePath);
			finalSample.forEach(f -> System.out.println(f.getAbsolutePath()));			
			//finalSample = SamplerRQ3.sampleSizewisePositiveSample(refClones, sexprFiles, sampleSize, seed, mode); 
			String outputFile = resultDir+seed+"_Size_"+mode+".txt";
			SamplerRQ3.saveFinalSample(finalSample,outputFile);
		}
		
    	Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
		  sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		  System.out.println("Sampled clones map size:"+sampledClones.size());
    
		

		  //String indexName  = "bigclone_index_0_for_sampled_evaluation";
		  String indexName = "bigclone_index_0_only_parsable_files";
		  long startTime = System.currentTimeMillis();  
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
		  MetricsCalculator.computeAndSaveMetrics("TFIDF", resultDir,
										  similarityScores, sampledClones,
										  totalTime, seed, rqNum, mode, numDocs);
	
	}
	
	public static void main(String[] args) {
		long [] all_seeds= {6251, 9080, 8241,8828,55,2084,1375,2802,3501,3389}; //from Util.generate_seed();//{6251,9080}
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
	}
}
