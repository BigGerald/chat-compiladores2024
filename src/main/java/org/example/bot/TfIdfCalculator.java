package org.example.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TfIdfCalculator {

    private Map<String, Integer> termFrequency(String document) {
        Map<String, Integer> termFreq = new HashMap<>();
        for (String term : document.split("\\s+")) {
            termFreq.put(term, termFreq.getOrDefault(term, 0) + 1);
        }
        return termFreq;
    }

    private double idf(String term, List<String> documents) {
        int docCountContainingTerm = 0;
        for (String doc : documents) {
            if (doc.contains(term)) {
                docCountContainingTerm++;
            }
        }
        return Math.log((double) documents.size() / (1 + docCountContainingTerm));
    }

    public double tfIdf(String term, String document, List<String> documents) {
        Map<String, Integer> termFreq = termFrequency(document);
        double tf = termFreq.getOrDefault(term, 0) / (double) termFreq.size();
        double idf = idf(term, documents);
        return tf * idf;
    }

    public Map<String, Double> calculateTfIdfForDocuments(String term, List<String> documents) {
        Map<String, Double> tfIdfScores = new HashMap<>();
        for (String document : documents) {
            double score = tfIdf(term, document, documents);
            tfIdfScores.put(document, score);
        }
        return tfIdfScores;
    }
}
