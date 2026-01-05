package sample.evaluation.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVWriterUtil {    
    
	public static void saveSimilarityScores(Map<String, Map<String, Float>> pairwiseSimilarities, String filename) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {  
			for (Map.Entry<String, Map<String, Float>> entry : pairwiseSimilarities.entrySet()) {
				String file1 = entry.getKey();
				Map<String, Float> scores = entry.getValue();
				// Sorting the inner map by values (similarity scores) in descending order
				List<Map.Entry<String, Float>> sortedScores = new ArrayList<>(scores.entrySet());
				sortedScores.sort(Map.Entry.<String, Float>comparingByValue().reversed());
				for (Map.Entry<String, Float> scoreEntry : sortedScores) {
					String line = file1 + "," + scoreEntry.getKey() + "," + scoreEntry.getValue() + "\n";
					writer.write(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveSelfSimilarityScores(Map<String, Float> selfSimilarities, String filename) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
			for (Map.Entry< String, Float> entry : selfSimilarities.entrySet()) {
				String line = entry.getKey() + "," + entry.getValue() + "\n";
				writer.write(line);   
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	 }

	public static void saveTopNSimilarityScores(Map<String, Map<String, Float>> pairwiseSimilarities,
		                                String filename, int topN) {
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) { // overwrite
		    for (Map.Entry<String, Map<String, Float>> entry : pairwiseSimilarities.entrySet()) {
		        String file1 = entry.getKey();
		        Map<String, Float> scores = entry.getValue();

		        // Sort descending by similarity
		        List<Map.Entry<String, Float>> sortedScores = new ArrayList<>(scores.entrySet());
		        sortedScores.sort(Map.Entry.<String, Float>comparingByValue().reversed());

		        // Write only top N
		        for (int i = 0; i < Math.min(topN, sortedScores.size()); i++) {
		            Map.Entry<String, Float> scoreEntry = sortedScores.get(i);
		            writer.write(file1 + "," + scoreEntry.getKey() + "," + scoreEntry.getValue() + "\n");
		        }
		    }
	    } catch (IOException e) {
		    e.printStackTrace();
	  }
	}
	
	   
    public static void appendResultToFile(float prec5, float prec10, float mrr, float map, double time,
                                        String filename,long seed, String kernel, String mode) {
        try (FileWriter fw = new FileWriter(filename, true);
            PrintWriter pw = new PrintWriter(fw)) {
       	    pw.print(prec5);
       	    pw.print(",");
       	    pw.print(prec10);
       	    pw.print(",");
	       	pw.print(mrr);
	       	pw.print(",");
	       	pw.print(map);
	       	pw.print(",");
	       	pw.print(kernel);
	    	pw.print(",");
	       	pw.print(time);
	       	pw.print(",");
	       	pw.print(seed);
            pw.print(",");
            pw.print(mode);
        
       	    pw.println();

       	} catch (IOException e) {
       	    e.printStackTrace();
       	}
    }

 
}
