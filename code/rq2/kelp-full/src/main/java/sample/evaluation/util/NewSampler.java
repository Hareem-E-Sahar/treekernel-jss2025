package sample.evaluation.util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class NewSampler {

    /**
     * Returns a list of files that have at least one clone.
     */
    private static List<File> getPositiveFiles(List<File> allFiles, Map<String, ArrayList<String>> refClones) {
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
    public static List<File> selectPositiveSample(List<File> allFiles, Map<String, ArrayList<String>> refClones, 
                                                                                long seed, int sampleSize) {
        List<File> positiveFiles = getPositiveFiles(allFiles, refClones);
        return sampleFiles(positiveFiles, seed, sampleSize);
    }

    //for RQ2
    public static String resolveRefFile(String cloneType, Path base) {
        switch (cloneType) {
            case "T1":
                return base.resolve("T1-clones-selected-columns.txt").toString();
            case "T2":
                return base.resolve("T2-clones-selected-columns.txt").toString();
            case "T3":
                return base.resolve("ST3-VST3-clones-simtoken-selected-columns.txt").toString();

            default:
                throw new IllegalArgumentException("Invalid cloneType: " + cloneType);
        }
    }

    
    /**
     * Select files with at least one clone, filter by type, shuffle, pick 100 
     */
    public static List<File> sampleTypewisePositiveSample(Map<String, ArrayList<String>> refClones,
                                                      Set<String> typewiseClones,
                                                      List<File> allFiles,
                                                      int sampleSize,
                                                      long seed) {
        List<File> positiveFiles = getPositiveFiles(allFiles, refClones);
        List<File> candidates = new ArrayList<>();

        for (File file : positiveFiles) {
            if (typewiseClones.contains(file.getName())) {
                candidates.add(file);
            }
        }
        return sampleFiles(candidates, seed, sampleSize);
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
