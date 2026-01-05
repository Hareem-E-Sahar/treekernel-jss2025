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

	public PrecisionCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones) {
		this.similarityScores = simScores;
		this.referenceClones  = refClones;

	}
	
	private float calculatePrecision(String query, ArrayList<String> groundTruth, int K) {
		System.out.println("Query:"+query);
		Map<String, Float> queryScores = similarityScores.get(query);
		
	 
		if (queryScores == null || queryScores.isEmpty()) {
				System.out.println();
				return 0.0f;  // No scores available for this query		
		}
		List<Map.Entry<String, Float>> sortedScores = ScoreUtils.getTopScores(queryScores);
		System.out.println("Original scores size:"+queryScores.size()+" Filtered & Sorted scores:"+sortedScores.size());
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
		return (float) relevantItems / K ;  // Calculate precision at k
	}
	
	public float calculateMeanPrecision(int K) {
		float totalPrecision = 0.0f;
		int queriesCount = 0;
		for (String query: referenceClones.keySet()) {	
			ArrayList<String> groundTruth = referenceClones.get(query);
			totalPrecision += calculatePrecision(query,groundTruth,K);;
			queriesCount++;
		}
		System.out.println("# of queries:"+queriesCount);
		return queriesCount > 0 ? totalPrecision / queriesCount : 0;
	}	
}

