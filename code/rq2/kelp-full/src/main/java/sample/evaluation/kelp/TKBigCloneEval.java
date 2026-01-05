package sample.evaluation.kelp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import sample.evaluation.util.NewSampler;
import sample.evaluation.metrics.MetricsCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import sample.evaluation.util.FileMapper;
import sample.evaluation.util.TypeClones;
import sample.evaluation.util.NewSampler;
import sample.evaluation.util.NormalizedScore;
import sample.evaluation.util.ScoreUtils;
import sample.evaluation.util.CSVWriterUtil;

import sample.evaluation.util.SampleFileReader;


public class TKBigCloneEval {
	
	public SimilarityMap evaluate(List<File> sexprFiles, List<File> sampledFiles, String kernel) {
		TKSimilarity tkSimilarity = new TKSimilarity(kernel);
		tkSimilarity.findClones(sexprFiles,sampledFiles);
		return tkSimilarity.getSimMap();
	}
	
	public static void evaluateOnSeed(long seed) {
		  System.out.println("Seed:"+seed);
		  String subDir = "0";  
 		  final float threshold = 0.3f;	
		  final int sampleSize = 100;
	  	final String cloneType = "T";
	  	final String kernel = "SSTK"; //re-running because the earlier seeds were run using the old code.
	  	String rqNum = "RQ2";
	  	String technique = "kelp_evaluation_random_sample";
	  	
		  Path mainDirPath = Paths.get(System.getProperty("user.home"), "treekernel-jss2025");
		  Path dataDirPath = Paths.get(System.getProperty("user.home"), "treekernel-jss2025", "data", "BigCloneEval","ijadataset");
		  Path resultDirPath = mainDirPath.resolve("results").resolve(rqNum);
	    	
		  Path BCDirPath   = Paths.get(System.getProperty("user.home"),"treekernel-jss2025", "data","TestH2Database");
		  Path newBCDirPath= Paths.get(System.getProperty("user.home"),"treekernel-jss2025","data","BigCloneEval", "bigclone_groundtruth_v3");
		
		  String codesDir = dataDirPath.resolve("bcb_reduced").resolve(subDir).toString()+"/";
		  String sexprDir = dataDirPath.resolve("sexpr_from_gumtree/").resolve(subDir).toString()+"/";
		
		  String refFile = BCDirPath.resolve("bigclonedb_clones_alldir_8584153.txt").toString(); //it has selected cols
		  String typewiseFile = NewSampler.resolveRefFile(cloneType, newBCDirPath);
	
	
		  Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
		  Set<String> typewiseClones = new HashSet<String>(); 
		  try {
			  refClones = ReferenceClones.lookupClonePairs(typewiseFile,codesDir);//corrected
		  	typewiseClones = TypeClones.getTypewiseFiles(typewiseFile,codesDir);	
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		
		  List<File> sexprFiles = FileMapper.getAllFiles(new File(sexprDir));
			
  	  List<File> finalSample = NewSampler.sampleTypewisePositiveSample(refClones, typewiseClones, 
																	                            sexprFiles, sampleSize, seed);
      
		  String outputFinalSample = resultDirPath.resolve(cloneType+"_"+seed+".txt").toString();
		  NewSampler.saveFinalSample(finalSample, outputFinalSample);
		
      Map<String, ArrayList<String>> sampledClones  = new HashMap<String, ArrayList<String>>();
		  sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		   
	 	  long startTime = System.currentTimeMillis();
		  TKBigCloneEval tkeval = new TKBigCloneEval();
		  SimilarityMap simMap = tkeval.evaluate(sexprFiles, finalSample, kernel);
		  Map<String, Map<String, Float>> similarityScores = simMap.getSimilarityScores();
		  Map<String, Float> selfSimilarities = simMap.getSelfSimilarities();
		
		  String similarityFile = resultDirPath.resolve(kernel+"_pairwise_sim_"+seed+".csv").toString();
		  String normSimilarityFile = resultDirPath.resolve(kernel+"_norm_sim_"+seed+".csv").toString();
		  String selfSimilarityFile = resultDirPath.resolve(kernel+"_self_sim_"+seed+".csv").toString();
		  //CSVWriterUtil.saveSimilarityScores(similarityScores, similarityFile);

		  long endTime = System.currentTimeMillis();
		  double totalTime = (endTime - startTime)/1000; // millisecs to second
		  System.out.println("Sim scores size :"+similarityScores.size()); 
		  System.out.println("Total time:"+totalTime); 
		  System.out.println("Calculating metrics now!");
			
		  /*MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDirPath,
          										similarityScores, sampledClones, threshold,
          										totalTime, seed, technique, rqNum, "Not normalized", cloneType);

		  MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDirPath,
         								 			similarityScores, sampledClones, threshold,
          										totalTime, seed, technique, rqNum, "Not normalized", cloneType);
      	
		  MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDirPath,
												  similarityScores, sampledClones, threshold,
												  totalTime, seed, technique, rqNum, "Not normalized", cloneType);

      	MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDirPath,
												  similarityScores, sampledClones, threshold,
												  totalTime, seed, technique, rqNum, "Not normalized", cloneType);
		  */
		  //NORMALIZED SCORES										
		  Map<String, Map<String, Float>> normScores = NormalizedScore.computeNormalizedScores(similarityScores, selfSimilarities);
		  
		  CSVWriterUtil.saveSimilarityScores(normScores, normSimilarityFile);
									  
		  MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDirPath,
        										normScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "normalized", cloneType);

  		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDirPath,
       								 			normScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "normalized", cloneType);

     	MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDirPath,
												normScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "normalized", cloneType);

    	MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDirPath,
												normScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "normalized", cloneType);														
	}

	public static void main(String[] args) { 
		long [] all_seeds= {2084};//{6251,9080,8241,8828,55,1375,2084,2802,3501,3389}; 
		for (int i=0; i<all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]); 
	}
}
