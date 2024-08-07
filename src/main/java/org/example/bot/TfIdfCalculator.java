package org.example.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TfIdfCalculator {

    private double idf(String term, int docCountContainingTerm, int totalDocuments) {
        return Math.log((double) totalDocuments / (1 + docCountContainingTerm));
    }

    public double tfIdf(String term, int termCount, int totalTerms, int docCountContainingTerm, int totalDocuments) {
        double tf = (double) termCount / totalTerms;
        double idf = idf(term, docCountContainingTerm, totalDocuments);
        return tf * idf;
    }

    public Map<String, Double> calculateTfIdfForDocuments(String term, Map<String, Integer> termCounts, Map<String, Map<String, Integer>> invertedIndex) {
        Map<String, Double> tfIdfScores = new HashMap<>();
        int totalDocuments = invertedIndex.size();
        int docCountContainingTerm = invertedIndex.getOrDefault(term, new HashMap<>()).size();

        for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
            String document = entry.getKey();
            int termCount = entry.getValue();
            int totalTerms = termCounts.size();
            double score = tfIdf(term, termCount, totalTerms, docCountContainingTerm, totalDocuments);
            tfIdfScores.put(document, score);
        }

        return tfIdfScores;
    }
}
