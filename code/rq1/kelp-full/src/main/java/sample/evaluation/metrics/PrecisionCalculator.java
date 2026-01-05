package sample.evaluation.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import sample.evaluation.util.ScoreUtils;


public class PrecisionCalculator {
	//Prec@K is the proportion of recommended items in the top-k set that are relevant
	private Map<String, Map<String, Float>> similarityScores;
	private Map<String, ArrayList<String>> referenceClones;
	float threshold ;
	String filter;

	public PrecisionCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh, String filter ) {
		this.similarityScores = simScores;
		this.referenceClones  = refClones;
		this.threshold = thresh;
		this.filter = filter;
	}
	
	private float calculatePrecision(String query, ArrayList<String> groundTruth, int K) {
		Map<String, Float> queryScores = similarityScores.get(query);
	 
		if (queryScores == null || queryScores.isEmpty()) {
				return 0.0f;  // No scores available for this query		
		}
		List<Map.Entry<String, Float>> sortedScores = null;
		if (this.filter.equals("topk_threshold")) { 
			sortedScores = ScoreUtils.getTopScores(queryScores, 100, threshold);
		} else if (this.filter.equals("topk")) {
			sortedScores = ScoreUtils.getTopScores(queryScores, 100);         //use only top 100
		} else if(this.filter.equals("threshold")) {
			sortedScores = ScoreUtils.getTopScores(queryScores, threshold);  	//filter by threshold only
		} else if (this.filter.equals("no_filter")) {
			sortedScores = ScoreUtils.getTopScores(queryScores);
		} else {
			System.out.println("No filter specified");
		}
		assert sortedScores.size() <= queryScores.size() : "Error: Filtered list size is larger than original list size!";
			
		int relevantItems = 0;
		int examinedItems = 0;
		for (Entry<String, Float> scoreEntry : sortedScores) {
			if (examinedItems >= K) {
				break;
			}
			if (groundTruth.contains(scoreEntry.getKey())) {
				//System.out.println("Score key:"+scoreEntry.getKey()+" "+scoreEntry.getValue());
				relevantItems++;
			}
			examinedItems++;            
		}
		// If no valid clones are examined, precision is zero
		if (examinedItems == 0) {
			return 0.0f;
		}
		System.out.println("Query:"+query+" Relevant/Examined "+relevantItems+"/"+K+" @ K "+K);	
		return (float) relevantItems / K;
	}
	
	public float calculateMeanPrecision(int K) {
		float totalPrecision = 0.0f;
		int queriesCount = 0;
		for (String query: referenceClones.keySet()) {	
			ArrayList<String> groundTruth = referenceClones.get(query);
			float precisionAtK = calculatePrecision(query,groundTruth,K);
			System.out.println(query+"  "+precisionAtK);
			totalPrecision += precisionAtK;
			queriesCount++;
		}
		 return queriesCount > 0 ? totalPrecision / queriesCount : 0.0f;
		
	}	
}

