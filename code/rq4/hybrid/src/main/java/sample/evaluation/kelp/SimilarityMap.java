package sample.evaluation.kelp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimilarityMap {
    private Map<String, Map<String, Float>> similarityScores;
    Map <String, Float> selfSimilarities;
    
    public SimilarityMap() {
        this.similarityScores = new HashMap<>();
        this.selfSimilarities = new HashMap<>();
    }

    /**
     * Adds or updates the similarity score between two files.
     *
     * @param file1 the first file name
     * @param file2 the second file name
     * @param score the similarity score between file1 and file2
    */
    public void addSimilarity(String file1, String file2, Float score) {
        this.similarityScores.putIfAbsent(file1, new HashMap<String, Float>());
        this.similarityScores.get(file1).put(file2, score);
        // To ensure both way access you can add file2-file1 too but it makes a big filek
        // similarityScores.putIfAbsent(file2, new HashMap<String, Float>());
        // similarityScores.get(file2).put(file1, score);  
    }

    /**
     * Retrieves the similarity score between two files.
     *
     * @param file1 the first file name
     * @param file2 the second file name
     * @return the similarity score, or null if no score is present
     */
    public Float getSimilarity(String file1, String file2) {
        if (this.similarityScores.containsKey(file1) && this.similarityScores.get(file1).containsKey(file2)) {
            return this.similarityScores.get(file1).get(file2);
        }
        return null;  // or throw an exception 
    }

   
    public List<Map.Entry<String, Float>> sortSimilarityScores(String key) {
        // Retrieve the scores for the specified key and sort them
        Map<String, Float> scores = this.similarityScores.get(key);
        if (scores == null) {
            System.out.println("No scores available for " + key);
            return new ArrayList<>(); // Return an empty list 
        }

        // Create a list from the entry set of the map and sort it
        List<Map.Entry<String, Float>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort(Map.Entry.<String, Float>comparingByValue().reversed());
        return sortedScores;
    }

    public Map<String, Map<String, Float>> getSimilarityScores() {
		return this.similarityScores;
	}

    public Map<String, Float> getSelfSimilarities() {
        return this.selfSimilarities;
    }


    public Float getSelfSimilarity(String file) {
        return this.selfSimilarities.get(file);
    }

    public void addSelfSimilarity(String file1,  Float score) {
        this.selfSimilarities.put(file1, score);
    }

}
