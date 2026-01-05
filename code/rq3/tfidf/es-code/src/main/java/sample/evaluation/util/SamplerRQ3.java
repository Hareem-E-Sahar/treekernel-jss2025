package sample.evaluation.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import sample.evaluation.util.ComplexityUtils;

public class SamplerRQ3 {

    /**
     * RQ3 (size-based): sample files that
     *   - have at least one clone, and
     *   - satisfy a LOC condition:
     *        mode = "Less"  -> LOC < 6
     *        mode = "More"  -> LOC > 10
     */
    public static List<File> sampleSizewisePositiveSample(Map<String, ArrayList<String>> refClones,
                                                          List<File> allFiles,
                                                          int sampleSize,
                                                          long seed,
                                                          String mode) {
        List<File> positiveFiles = getPositiveFiles(allFiles, refClones);
        List<File> candidates = new ArrayList<>();
        System.out.println("Positive files:"+positiveFiles.size());                       
        for (File file : positiveFiles) {
            int loc = getLOC(file);
            if ("Small".equals(mode)) {
                if (loc < 6) {
                    candidates.add(file);
                }
            } else if ("Large".equals(mode)) {
                if (loc > 10) {
                    candidates.add(file);
                }
            }
        }

        return sampleFiles(candidates, seed, sampleSize);
    }

    /**
     * RQ3 (complexity-based): sample files that
     *   - have at least one clone, and
     *   - satisfy a complexity condition:
     *        mode = "Low"   -> complexity <= 10
     *        mode = "High"  -> complexity > 10
     */
    public static List<File> sampleComplexitywisePositiveSample(Map<String, ArrayList<String>> refClones,
                                                            Map<String, Integer> complexityMap,
                                                            List<File> allFiles,
                                                            int sampleSize,
                                                            long seed,
                                                            String mode) {
        List<File> positiveFiles = getPositiveFiles(allFiles, refClones);
        List<File> candidates = new ArrayList<>();

        for (File file : positiveFiles) {
            int complexity = ComplexityUtils.getComplexity(file, complexityMap);

            if ("Low".equals(mode)) {
                if (complexity <= 10) {
                    candidates.add(file);
                }
            } else if ("High".equals(mode)) {
                if (complexity > 10) {
                    candidates.add(file);
                }
            }
        }

        return sampleFiles(candidates, seed, sampleSize);
    }

    /**
     * Returns a list of files that have at least one clone.
     * (exact same pattern as your typewise helper)
     */
    private static List<File> getPositiveFiles(List<File> allFiles,
                                               Map<String, ArrayList<String>> refClones) {
        List<File> positiveFiles = new ArrayList<>();
        for (File f : allFiles) {
            ArrayList<String> clones = refClones.get(f.getName());
            if (clones != null && !clones.isEmpty()) {
                positiveFiles.add(f);
            }
        }
        return positiveFiles;
    }

    /**
     * Samples n random files from the given list using a seed.
     * Sampling is without replacement.
     * (matches your example closely)
     */
    public static List<File> sampleFiles(List<File> files, long seed, int sampleSize) {
        List<File> copy = new ArrayList<>(files); // avoid mutating original
        Random random = new Random(seed);
        Collections.shuffle(copy, random);
        return new ArrayList<>(copy.subList(0, Math.min(sampleSize, copy.size())));
    }
    
    /**
     * Calculates number of lines of java code 
     * - uses start and end line to estimate LOC
     * @param file: filename in the format file_1_10.java
     * @return: LOC
     */
    public static int getLOC(File file) {
	    String fileName = file.getName();

	    // Remove the extension
	    String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

	    // Split the string by underscores
	    String[] parts = baseName.split("_");

	    // Ensure the file has at least two parts for line numbers
	    if (parts.length < 3) {
	    	throw new IllegalArgumentException("Filename does not contain sufficient parts: " + fileName);
	    }

	    // Extract the last two parts
	    String startLineStr = parts[parts.length - 2];
	    String endLineStr   = parts[parts.length - 1];

	    try {
		    int startLine1 = Integer.parseInt(startLineStr);
            int endLine1   = Integer.parseInt(endLineStr);
		    // Calculate and return the function length
		    return endLine1 - startLine1;
	    } catch (NumberFormatException e) {
		    throw new IllegalArgumentException("Invalid line numbers in filename: " + fileName, e);
	    }
	}
	 /**
     * Save the final sample
     */
    public static void saveFinalSample(List<File> finalSample, String outputFile) {
	    System.out.println("Saving sampled files in..."+outputFile);
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
		    for (File file : finalSample) {
			    writer.write(file.getAbsolutePath() + "\n");
		    } 
	    } catch (IOException e) {
		    e.printStackTrace();
	    }
    }
	
}

