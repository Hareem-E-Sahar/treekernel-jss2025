package sample.evaluation.metrics;

import java.util.ArrayList;
import java.util.Map;
import java.nio.file.Path;
import sample.evaluation.util.CSVWriterUtil;

public class MetricsCalculator {
    public static void computeAndSaveMetrics (
        String filteringCriteria,
        String kernel,
        Path resultDirPath,
        Map<String, Map<String, Float>> similarityScores,
        Map<String, ArrayList<String>> sampledClones,
        float threshold,
        double totalTime,       
        long seed,
        String technique,
        String rqNum,
        String mode,
        String cloneType) {

     
        String metricsFile = resultDirPath.resolve("metrics_"+rqNum+"_"+cloneType+"_"+kernel+"_"+filteringCriteria+".csv").toString();    
        PrecisionCalculator precisionCalc =
                new PrecisionCalculator(similarityScores, sampledClones, threshold, filteringCriteria);

        float prec5  = precisionCalc.calculateMeanPrecision(5);
        float prec10 = precisionCalc.calculateMeanPrecision(10);

        MRRCalculator mrrCalc =
                new MRRCalculator(similarityScores, sampledClones, threshold, filteringCriteria);
        float mrr = mrrCalc.calculateOverallMRR();

        MAPCalculator mapCalc =
                new MAPCalculator(similarityScores, sampledClones, threshold, filteringCriteria);
        float map = mapCalc.calculateOverallMAP();

        System.out.print("\nPrec5:" + prec5 + " Prec10:" + prec10 +
                        " MRR:" + mrr + " MAP:" + map + " ");
        System.out.println(" time:" + totalTime + " sec" +
                        " threshold:" + threshold + " technique:" + technique);
        System.out.println(metricsFile);

        CSVWriterUtil.appendResultToFile(prec5, prec10, mrr, map,
                                 totalTime, metricsFile, seed, kernel, mode);
		
        
    }
}


   
