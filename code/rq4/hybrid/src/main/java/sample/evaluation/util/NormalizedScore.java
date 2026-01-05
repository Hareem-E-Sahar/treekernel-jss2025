package sample.evaluation.util;
import java.util.Map;
import java.util.HashMap;

public class NormalizedScore {

    public static Map<String, Map<String, Float>> computeNormalizedScores(
            Map<String, Map<String, Float>> rawScores,
            Map<String, Float> selfSimilarities) {

        Map<String, Map<String, Float>> normScores = new HashMap<>();

        for (Map.Entry<String, Map<String, Float>> entry : rawScores.entrySet()) {
            String f1 = entry.getKey();
            Map<String, Float> inner = entry.getValue();

            Float self1 = selfSimilarities.get(f1);
            if (self1 == null || self1 <= 0) {
                continue; // or handle NaN/0 issues here
            }

            for (Map.Entry<String, Float> innerEntry : inner.entrySet()) {
                String f2 = innerEntry.getKey();
                float sim = innerEntry.getValue();

                Float self2 = selfSimilarities.get(f2);
                if (self2 == null || self2 <= 0) {
                    continue;
                }

                // K_norm = K / sqrt(K(T1,T1) * K(T2,T2))

                // compute normalization factor
                float denom = (float) (Math.sqrt(self1) * Math.sqrt(self2));
                if (denom == 0.0f) {  //add a small epsilon?
                    continue;
                }

                float normSim = sim / denom;

                normScores
                    .computeIfAbsent(f1, k -> new HashMap<>())
                    .put(f2, normSim);
            }
        }

        return normScores;
    }

}