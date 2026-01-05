package sample.evaluation.util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//read a sampled file to re-run experiment in previously sampled files 
public class SampleFileReader {
    public static List<File> readFileNames(String inputFilePath) {
        List<File> finalSample = new ArrayList<>();
        System.out.println(inputFilePath);
      
                 
        try (Stream<String> lines = Files.lines(Paths.get(inputFilePath))) {
        lines.map(String::trim)
             .filter(line -> !line.isEmpty())
             .map(File::new)          // recreate File with full path
             .forEach(finalSample::add);
        } catch (IOException e) {
          e.printStackTrace();
        }
        return finalSample;
    }

}

