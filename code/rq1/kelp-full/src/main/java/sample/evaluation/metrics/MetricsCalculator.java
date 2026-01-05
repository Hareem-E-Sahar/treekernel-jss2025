package sample.evaluation.metrics;
import sample.evaluation.util.CSVWriterUtil;
import java.util.ArrayList;
import java.util.Map;


public class MetricsCalculator {
    public static void computeAndSaveMetrics (
        String filteringCriteria,
        String kernel,
        String resultDir,
        Map<String, Map<String, Float>> similarityScores,
        Map<String, ArrayList<String>> sampledClones,
        float threshold,
        double totalTime,       
        long seed,
        String technique,
        String rqNum,
        String mode) {

        String metricsFile = resultDir + "metrics_"+rqNum+"_" + kernel + "_" + filteringCriteria + ".csv";
		    
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


   
