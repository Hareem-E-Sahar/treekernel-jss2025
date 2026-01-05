package sample.evaluation.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import sample.evaluation.util.ScoreUtils;

public class MAPCalculator {
	 private Map<String, Map<String, Float>> similarityScores;
	 private Map<String, ArrayList<String>>  referenceClones;
	 private int numDocs ;
	
	 public MAPCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, int numDocs) {
		 this.similarityScores = simScores;
		 this.referenceClones  = refClones;
		 this.numDocs = numDocs;
		
	 }
	 
	 private float calculateAveragePrecision(String query, ArrayList<String> groundTruth) {
		System.out.println("\nQuery:"+query+" Ground truth size:"+groundTruth.size());
		Map<String, Float> queryScores = similarityScores.get(query);
		
		if (queryScores == null || queryScores.isEmpty()) {
			return 0.0f;  // No scores available for this query
			
		}
    
    List<Map.Entry<String, Float>> sortedScores = ScoreUtils.getTopScores(queryScores);
		    	
		assert sortedScores.size() <= queryScores.size() : "Error: Filtered list size is larger than original list size!";
	    
		float sumPrecision = 0.0f;
		int relevantCount = 0;
		int rank = 0;
		for (Entry<String, Float> entry : sortedScores) {
		    rank++;
		    if (groundTruth.contains(entry.getKey())) {
		    	//System.out.println("Rank of "+entry.getKey()+" is "+rank);
		        relevantCount++;
		        sumPrecision += (float) relevantCount / rank;
		        //System.out.println("Relevant:"+relevantCount+","+rank+","+sumPrecision);
		    }
    }
        
    int denom = Math.min(groundTruth.size(), numDocs);
    return relevantCount > 0 ? sumPrecision / denom : 0.0f;
	 }
		    

	public float calculateOverallMAP() {
    float sum =  0.0f;
    int count = 0;

    for (String query : referenceClones.keySet()) {
        ArrayList<String> groundTruth = referenceClones.get(query);
        sum += calculateAveragePrecision(query, groundTruth);
        count++;
    }
    return count > 0 ? sum / count : 0.0f;
  }
}
