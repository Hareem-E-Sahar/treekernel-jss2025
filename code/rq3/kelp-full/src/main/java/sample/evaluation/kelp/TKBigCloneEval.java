package sample.evaluation.kelp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Arrays;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import sample.evaluation.util.ComplexityUtils;
import sample.evaluation.util.SamplerRQ3;
import sample.evaluation.metrics.MetricsCalculator;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.Util;
import sample.evaluation.util.CSVWriterUtil;
import sample.evaluation.util.DirectoryProcessor;
import sample.evaluation.util.NormalizedScore;
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
 		float threshold   = 0.3f;	
		int sampleSize = 100;
	  String kernel  = "STK"; 
	  String technique  = "kelp_evaluation_RQ3";
		String rqNum  = "RQ3";
		String mode = "Low";  
		String similarityFile = null;
		
		  /* Complexity can be Low, High ;
		     Size can be Large or Small
		  */	
		
  	Path mainDirPath = Paths.get(System.getProperty("user.home"), "treekernel-jss2025");
  	Path dataDirPath = Paths.get(System.getProperty("user.home"), "treekernel-jss2025", "data", "BigCloneEval","ijadataset");
  	Path resultDirPath = Paths.get(System.getProperty("user.home"), "treekernel-jss2025","results").resolve(rqNum);
    	
    Path BCDirPath   = Paths.get(System.getProperty("user.home"),"treekernel-jss2025", "data","TestH2Database");
    String codesDir = dataDirPath.resolve("bcb_reduced").resolve(subDir).toString()+"/";
	  String sexprDir = dataDirPath.resolve("sexpr_from_gumtree/").resolve(subDir).toString()+"/";
	  String complexityFile = Paths.get(System.getProperty("user.home"), "treekernel-jss2025", "data").resolve("checkstyle_complexity_all.csv").toString();
	  String refFile = BCDirPath.resolve("bigclonedb_clones_alldir_8584153.txt").toString();
		
		List<File> sexprFiles = DirectoryProcessor.getAllFiles(new File(sexprDir));
		System.out.println("Sexpr Files:"+sexprFiles.size());
		
		Map<String, ArrayList<String>> refClones = new HashMap<>();	
		try {
			refClones = ReferenceClones.lookupClonePairs(refFile,codesDir);
			System.out.println("refClones:"+refFile+"  "+refClones.size());
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
    		
    	List<File> finalSample = new ArrayList<>();
		  if (mode.equals("High") || mode.equals("Low")) { 
			  System.out.println("Complexity Eval");
			  Map<String, Integer> complexityMap = ComplexityUtils.populateComplexityMap(complexityFile);
		    
		    //use pre-sampled files from fos or anisha for evaluation
		    String dirPath = resultDirPath.resolve("complexity/Low/fos/sampled/").toString();
        String fileName = seed + "_Complexity_"+mode+".txt";
			  String filePath = dirPath + fileName;
			  finalSample =  SampleFileReader.readFileNames(filePath);
			  finalSample.forEach(f -> System.out.println(f.getAbsolutePath()));
			
        
			  //finalSample = SamplerRQ3.sampleComplexitywisePositiveSample(refClones, complexityMap, sexprFiles, sampleSize, seed, mode); 
			  String outputFile = resultDirPath.resolve(seed+"_Complexity_"+mode+"_1.txt").toString();
			  SamplerRQ3.saveFinalSample(finalSample,outputFile);
			  similarityFile = resultDirPath.resolve("Pairwise_simscores_" + seed +"_"+ kernel +"_Complexity_" + mode + ".txt").toString();

		  } else if (mode.equals("Large") || mode.equals("Small")) {
			  System.out.println("LOC Eval");
			  finalSample = SamplerRQ3.sampleSizewisePositiveSample(refClones, sexprFiles, sampleSize, seed, mode); 
			  String outputFile = resultDirPath.resolve(seed+"_Size_"+mode+".txt").toString();
			  SamplerRQ3.saveFinalSample(finalSample,outputFile);
			  similarityFile = resultDirPath.resolve("Pairwise_simscores_" + seed +"_"+ kernel +"_Size_" + mode + ".txt").toString();
		  }
		
	    
		Map<String, ArrayList<String>> sampledClones  = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:"+sampledClones.size());
		
		
		TKBigCloneEval tkeval = new TKBigCloneEval();
    long startTime = System.currentTimeMillis();
		SimilarityMap simMap = tkeval.evaluate(sexprFiles,finalSample,kernel);
		Map<String, Map<String, Float>> similarityScores = simMap.getSimilarityScores();    
		System.out.println(" sim scores:"+similarityScores.size());
			
		Map<String, Float> selfSimilarities = simMap.getSelfSimilarities();
	  System.out.println("Self sim:"+selfSimilarities.size());
		//Util.saveSimilarityScores(similarityScores, similarityFile);
		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime)/1000; // millisecs to second
		
		System.out.println("Calculating metrics now!");
		
		

	/*	MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDirPath,
         										similarityScores, sampledClones, threshold,
         										totalTime, seed, technique, rqNum, "Not normalized", mode);

		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDirPath,
       	 							 			similarityScores, sampledClones, threshold,
         										totalTime, seed, technique, rqNum, "Not normalized", mode);
    	
		MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDirPath,
		 										similarityScores, sampledClones, threshold,
		 										totalTime, seed, technique, rqNum, "Not normalized", mode);

    	MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDirPath,
		 										similarityScores, sampledClones, threshold,
		 										totalTime, seed, technique, rqNum, "Not normalized", mode);
  */
		System.out.print("===============================================");
		Map<String, Map<String, Float>>  normScores = NormalizedScore.computeNormalizedScores(similarityScores, selfSimilarities);
		String normSimFile = resultDirPath.resolve("Norm_scores_" + seed +"_"+ kernel +"_Complexity_" + mode + ".txt").toString();
	
    Util.saveSimilarityScores(normScores, normSimFile);

		
		MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDirPath,
         										normScores, sampledClones, threshold,
         										totalTime, seed, technique, rqNum, "Normalized", mode);

		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDirPath,
       	 							 			normScores, sampledClones, threshold,
         										totalTime, seed, technique, rqNum, "Normalized", mode);
    	
		MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDirPath,
		 										normScores, sampledClones, threshold,
		 										totalTime, seed, technique, rqNum, "Normalized", mode);

    	MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDirPath,
		 										normScores, sampledClones, threshold,
		 										totalTime, seed, technique, rqNum, "Normalized", mode);
	}
	
	public static void main(String[] args) {
		long [] all_seeds= {6251, 9080, 8241, 8828, 55, 2084, 1375, 2802, 3501, 3389}; //from Util.generate_seed();//6251, 9080, 8241, 8828, 55, 2084, 1375, 2802, 3501, 3389
		for (int i=0; i<all_seeds.length; i++) {
			evaluateOnSeed(all_seeds[i]); 
		}				
	}
}
