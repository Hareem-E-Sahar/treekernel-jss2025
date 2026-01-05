package sample.evaluation.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class LoggingUtil {

  public static void logRanks(
          String query,
          int relevantCount,
          int firstRelevantRank,
          List<Integer> relevantRanks,
          int denom,
          String outfile
  ) {
      try (FileWriter fw = new FileWriter(outfile, true);
           PrintWriter pw = new PrintWriter(fw)) {

          pw.println(
              query +
              "," + relevantCount +
              "," + firstRelevantRank +
              "," + relevantRanks +
              "," + denom 
              
          );

      } catch (IOException e) {
          e.printStackTrace();
      }
  }
}
