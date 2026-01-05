package sample.evaluation.metrics;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import sample.evaluation.util.ScoreUtils;

public class MRRCalculator {
    private Map<String, Map<String, Float>> similarityScores;
    private Map<String, ArrayList<String>> referenceClones;
    float threshold;
    String filter;
    
    public MRRCalculator(Map<String, Map<String, Float>> simScores, Map<String, ArrayList<String>> refClones, float thresh, String filter) {
        this.similarityScores = simScores;
        this.referenceClones  = refClones;
        this.threshold = thresh;
        this.filter = filter;
      
    }
   
    public float calculateMRR(String query, ArrayList<String> groundTruth) {
		
	    Map<String, Float> queryScores = similarityScores.get(query);
		
		  //sort and filter (originally used in all evaluations of RQ2 so far)
	    /*List<Map.Entry<String, Float>> sortedScores = queryScores.entrySet().stream()
	    		    .filter(entry -> entry.getValue() >= threshold)
	    		    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
	    		    .collect(Collectors.toList());*/
         	
        //filter by threshold and keep only top 100
        List<Map.Entry<String, Float>> sortedScores = null;
		    if (this.filter.equals("topk_threshold")) { 
			    sortedScores = ScoreUtils.getTopScores(queryScores,100, threshold);
		    } else if (this.filter.equals("topk")) {
			    //use only top 100
			    sortedScores = ScoreUtils.getTopScores(queryScores, 100);
		    } else if(this.filter.equals("threshold")) {
			    //filter by threshold only
			    sortedScores = ScoreUtils.getTopScores(queryScores, threshold); 
		    } else if (this.filter.equals("no_filter")) {
			    sortedScores = ScoreUtils.getTopScores(queryScores);
		    } else {
			    System.out.println("No filter specified");
		    }
	       
	       
	      if (sortedScores.size() > queryScores.size()) {
	            //System.out.println("Error: Filtered list size is larger than original list size!");
	            return 0.0f;
	      }
	        
		    if (sortedScores == null || sortedScores.isEmpty()) {
                return 0.0f;  // No scores available for this query
	      } 
      
        int rank = 0;
        // Finding the rank of the first relevant item from the ground truth
        for (Entry<String, Float> entry : sortedScores) {
            	rank++;
	            String foundClone = entry.getKey();
	            if (groundTruth.contains(foundClone)) {
	            	//System.out.println("found clone "+foundClone+" rank "+rank);
		            return (float) (1.0 / (rank)); //Reciprocal rank of the first relevant item
	            }
            }
        	return 0.0f; // No relevant scores found
    }
    
    public float calculateOverallMRR() {
        float sum = 0.0f;
        int count = 0;
        for (String query : referenceClones.keySet()) {
    		    ArrayList<String> groundTruth = referenceClones.get(query);
            sum += calculateMRR(query,groundTruth);
            count++;    
        }
        return count > 0 ? sum / count : 0.0f;
    }
}
