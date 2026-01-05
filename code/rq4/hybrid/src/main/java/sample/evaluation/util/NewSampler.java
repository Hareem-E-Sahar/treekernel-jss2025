package sample.evaluation.util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class NewSampler {

    /**
     * Returns a list of files that have at least one clone.
     */
    public static List<File> getPositiveFiles(List<File> allFiles, Map<String, ArrayList<String>> refClones) {
        List<File> positiveFiles = new ArrayList<>();
        for (File f : allFiles) {
            if (refClones.containsKey(f.getName()) && !refClones.get(f.getName()).isEmpty()) {

                positiveFiles.add(f);
            }
        }
        return positiveFiles;
    }

    /**
     * Samples n random files from the given list using a seed.
     * Sampling is without replacement: i.e. everything can be selected once.
     */
    public static List<File> sampleFiles(List<File> files, long seed, int sampleSize) {
        List<File> copy = new ArrayList<>(files); // avoid mutating original
        Random random = new Random(seed);
        Collections.shuffle(copy, random);
        // return a subset of the first 'sampleSize' elements
        return new ArrayList<>(copy.subList(0, Math.min(sampleSize, copy.size())));
    }


    /**
     * Select n positive files directly from the main list 
     */
    public static List<File> selectPositiveSample(List<File> allFiles, Map<String, ArrayList<String>> refClones, long seed, int sampleSize) {
        List<File> positiveFiles = getPositiveFiles(allFiles, refClones);
        return sampleFiles(positiveFiles, seed, sampleSize);
    }

    /**
     * Save the sampled files
     * @param finalSample
     * @param outputFile
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