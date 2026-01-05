package sample.evaluation.kelp;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertNotEquals;
import sample.evaluation.metrics.MetricsCalculator;
import sample.evaluation.util.NewSampler;
import sample.evaluation.util.FileMapper;
import sample.evaluation.util.NormalizedScore;
import sample.evaluation.util.ReferenceClones;
import sample.evaluation.util.CSVWriterUtil;

public class TKBigCloneEval {

	public SimilarityMap evaluate(List<File> sexprFiles, List<File> sampledFiles, String kernel) {
		TKSimilarity tkSimilarity = new TKSimilarity(kernel);
		tkSimilarity.findClones(sexprFiles, sampledFiles);
		return tkSimilarity.getSimMap();
	}
  
	public static void evaluateOnSeed(long seed) {
		System.out.println("Seed:" + seed);
		String subDir = "0";
		final float threshold = 0.3f;
		final int sampleSize = 100;
		String rqNum  = "RQ1";
		final String kernel = "SSTK";
		String technique = "kelp_evaluation_random_sample";
		String userhome = System.getProperty("user.home");
		String mainDir = userhome + "/treekernel-jss2025/";
		String dataDir = mainDir + "data/BigCloneEval/ijadataset/";
		String refFile = mainDir + "data/TestH2Database/bigclonedb_clones_alldir_8584153.txt";
		String codeDir = dataDir + "bcb_reduced/" + subDir + "/";
		String sexprDir = dataDir + "sexpr_from_gumtree/" + subDir + "/";
		String resultDir = mainDir + "results/" + rqNum + "/" ;
		String similarityFile = resultDir+kernel+"_pairwise_sim_"+seed+".csv"; 	//saves pairwise similarities
		String selfSimilarityFile = resultDir+kernel+"_self_sim_"+seed+".csv";  //saves self similarities
		String normSimilarityFile = resultDir+kernel+"_norm_sim_"+seed+".csv"; 	//saves normalized similarities
		
		Map<String, ArrayList<String>> refClones = new HashMap<String, ArrayList<String>>();
		try {
			refClones = ReferenceClones.lookupClonePairs(refFile, codeDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
    List<File> sexprFiles = FileMapper.getAllFiles(new File(sexprDir));

		assertNotEquals(0, sexprFiles.size());
		List<File> finalSample = NewSampler.selectPositiveSample(sexprFiles, refClones, seed, sampleSize);
    		
		NewSampler.saveFinalSample(finalSample, resultDir + seed + ".txt");
		Map<String, ArrayList<String>> sampledClones = new HashMap<String, ArrayList<String>>();
		sampledClones = ReferenceClones.getSpecificClones(refClones, finalSample);
		System.out.println("Sampled clones map size:" + sampledClones.size());

		long startTime = System.currentTimeMillis();
		TKBigCloneEval tkeval = new TKBigCloneEval();
		SimilarityMap simMap = tkeval.evaluate(sexprFiles, finalSample, kernel);
		Map<String, Map<String, Float>> similarityScores = simMap.getSimilarityScores();
		Map<String, Float> selfSimilarities = simMap.getSelfSimilarities();
		
		//CSVWriterUtil.saveSimilarityScores(similarityScores, similarityFile);
		//CSVWriterUtil.saveSelfSimilarityScores(selfSimilarities, selfSimilarityFile);

		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime) / 1000; // millisecs to second

		System.out.println("Total time:" + totalTime);
		System.out.println("Calculating metrics now!");		

		/*MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDir,
        										similarityScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "Not normalized");

  	System.out.println("-".repeat(100));
		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDir,
       								 			similarityScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "Not normalized");
    System.out.println("-".repeat(100));
		MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDir,
												similarityScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "Not normalized");

    System.out.println("-".repeat(100));
		MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDir,
												similarityScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "Not normalized");*/
												
		//NORMALIZED SCORES										
		Map<String, Map<String, Float>> normScores = NormalizedScore.computeNormalizedScores(similarityScores, selfSimilarities);
		System.out.println("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*= NORMALIZATION *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=");
		CSVWriterUtil.saveSimilarityScores(normScores, normSimilarityFile);
									
		MetricsCalculator.computeAndSaveMetrics("topk_threshold", kernel, resultDir,
        										normScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "normalized");

  	System.out.println("-".repeat(100));
		MetricsCalculator.computeAndSaveMetrics("topk", kernel, resultDir,
       								 			normScores, sampledClones, threshold,
        										totalTime, seed, technique, rqNum, "normalized");
    System.out.println("-".repeat(100));
		MetricsCalculator.computeAndSaveMetrics("threshold", kernel, resultDir,
												normScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "normalized");

    System.out.println("-".repeat(100));
		MetricsCalculator.computeAndSaveMetrics("no_filter", kernel, resultDir,
												normScores, sampledClones, threshold,
												totalTime, seed, technique, rqNum, "normalized");
	}

	public static void main(String[] args) {
		long[] all_seeds = {6251};//, 9080, 8241, 8828, 55, 2084, 1375, 2802, 3501, 3389}; //from Util.generate_seed();
		System.out.println("=".repeat(50));
		for (int i = 0; i < all_seeds.length; i++)
			evaluateOnSeed(all_seeds[i]);
	}
}
