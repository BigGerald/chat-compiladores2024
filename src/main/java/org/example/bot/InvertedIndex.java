package org.example.bot;

import java.io.*;
import java.util.*;

public class InvertedIndex {

    private Map<String, List<String>> invertedIndex;

    public InvertedIndex() {
        invertedIndex = new HashMap<>();
    }

    public void add(String term, String document) {
        invertedIndex.computeIfAbsent(term, k -> new ArrayList<>()).add(document);
    }

    public void saveToFile(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Map.Entry<String, List<String>> entry : invertedIndex.entrySet()) {
                String term = entry.getKey();
                List<String> documents = entry.getValue();
                writer.write(term + ":" + String.join(",", documents));
                writer.newLine();
            }
        }
    }

    public void loadFromFile(String fileName) throws IOException {
        invertedIndex.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String term = parts[0].trim();
                    List<String> documents = Arrays.asList(parts[1].split(","));
                    invertedIndex.put(term, documents);
                }
            }
        }
    }

    public List<String> getDocuments(String term) {
        return invertedIndex.getOrDefault(term, new ArrayList<>());
    }
}
