package sample.evaluation.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import sample.evaluation.util.ScoreUtils;
import sample.evaluation.util.Util;

public class PrecisionCalculator {
	//Prec@K is the proportion of recommended items in the top-k set that are relevant
	 private Map<String, Map<String, Float>> similarityScores;
	 private Map<String, ArrayList<String>> referenceClones;
	 float threshold;
	 String filter;
	 
	public PrecisionCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh, String filter) {
		this.similarityScores = simScores;
		this.referenceClones  = refClones;
		this.threshold = thresh;
		this.filter = filter;
	}
	 
	private float calculatePrecision(String query, ArrayList<String> groundTruth, int K) {
	 	//System.out.println("\nQuery:"+query+" Ground truth size:"+groundTruth.size());
			Util.printSortedSimilarityScores(similarityScores);
	    Map<String, Float> queryScores = similarityScores.get(query);
		
	     	List<Map.Entry<String, Float>> sortedScores = null;	   
			if(this.filter.equals("topk_threshold")) {
				//filter by threshold and keep only top 100
				sortedScores = ScoreUtils.getTopScores(queryScores, 100, threshold);
			} else if (this.filter.equals("topk")) { 
				//use only top 100
				sortedScores = ScoreUtils.getTopScores(queryScores, 100);
			} else if (this.filter.equals("threshold")) {
				//filter by threshold only
				sortedScores = ScoreUtils.getTopScores(queryScores, threshold);  
			} else if (this.filter.equals("no_filter")) {
				sortedScores = ScoreUtils.getTopScores(queryScores);
			} else {
				System.out.println("No filter specified");
			}

		 
	     	if (sortedScores.size() > queryScores.size()) {
	     		System.out.println("Error: Filtered list size is larger than original list size!");
	         	return 0.0f;
	     	}
	     
	     	if (sortedScores == null || sortedScores.isEmpty()) {
	            return 0.0f;  // No scores available for this query
	     	}
	     
	     	int relevantItems = 0;
	     	int examinedItems = 0;
	     	for (Entry<String, Float> scoreEntry : sortedScores) {
	     		if (examinedItems >= K) {
            break;
          }
        	
      		if (groundTruth.contains(scoreEntry.getKey())) {
      				relevantItems++;
      		}
      		examinedItems++;            
        }
         
		    // If no valid clones are examined, precision is zero
		    if (examinedItems == 0) {
		        return 0.0f;
		    }
        return (float) relevantItems / K;  // Calculate precision at k
	
	 }
	 
	public float calculateMeanPrecision(int K) {
		float totalPrecision = 0;
		int queriesCount = 0;
		for (String query: referenceClones.keySet()) {	
			ArrayList<String> groundTruth = referenceClones.get(query);
			totalPrecision += calculatePrecision(query,groundTruth,K);
	    queriesCount++;
		}
		return queriesCount > 0 ? totalPrecision / queriesCount : 0;
		
	 }	 
}

