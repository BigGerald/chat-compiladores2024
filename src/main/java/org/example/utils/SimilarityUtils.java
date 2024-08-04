package org.example.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimilarityUtils {
    public static ArrayList<String> checkSimilarity(ArrayList<String> words) {
        ArrayList<String> uniqueWords = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        boolean similarFound;
        double jaccardMinimum = 0.5;
        for (String word : words) {
            if (!processed.contains(word)) {
                similarFound = false;
                for (String uniqueWord : uniqueWords) {
                    double jaccard = calcularJaccard(word, uniqueWord);
                    if (jaccard >= jaccardMinimum) {
                        similarFound = true;
                        break;
                    }
                }
                if (!similarFound) {
                    uniqueWords.add(word);
                }
                processed.add(word);
            }
        }
        return uniqueWords;
    }

    private static double calcularJaccard(String palavra1, String palavra2) {
        Set<Character> conjunto1 = new HashSet<>();
        Set<Character> conjunto2 = new HashSet<>();

        for (char c : palavra1.toCharArray()) {
            conjunto1.add(c);
        }

        for (char c : palavra2.toCharArray()) {
            conjunto2.add(c);
        }

        int intersecao = 0;
        int uniao = conjunto1.size() + conjunto2.size();

        for (char c : conjunto1) {
            if (conjunto2.contains(c)) {
                intersecao++;
                uniao--;
            }
        }

        return (double) intersecao / uniao;
    }
}
