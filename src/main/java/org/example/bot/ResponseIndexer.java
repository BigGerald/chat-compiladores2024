package org.example.bot;

import org.example.utils.StopWordsUtils;

import java.io.*;
import java.util.*;

import static org.example.utils.StopWordsUtils.getStopWordsList;

public class ResponseIndexer {

    private Map<String, Map<String, Integer>> invertedIndex = new HashMap<>();
    private final String indexFilePath;
    private TfIdfCalculator tfIdfCalculator;

    public ResponseIndexer(String indexFilePath) {
        this.indexFilePath = indexFilePath;
        this.tfIdfCalculator = new TfIdfCalculator();
    }


    public void loadIndex() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(indexFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String term = parts[0];
                    String[] docCounts = parts[1].split(",");
                    Map<String, Integer> docs = new HashMap<>();
                    for (String docCount : docCounts) {
                        String[] docAndCount = docCount.split("-");
                        if (docAndCount.length == 2) {
                            docs.put(docAndCount[0], Integer.parseInt(docAndCount[1]));
                        }
                    }
                    invertedIndex.put(term, docs);
                }
            }
        }
    }

    public void saveIndex() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexFilePath))) {
            for (Map.Entry<String, Map<String, Integer>> entry : invertedIndex.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey()).append(":");
                for (Map.Entry<String, Integer> docEntry : entry.getValue().entrySet()) {
                    sb.append(docEntry.getKey()).append("-").append(docEntry.getValue()).append(",");
                }

                sb.setLength(sb.length() - 1);
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }


    public void createIndexFromDocuments() throws IOException {
        File folder = new File("src/files/documents");
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String content = reader.lines().reduce("", (acc, line) -> acc + " " + line).toLowerCase();
                    Set<String> terms = extractTerms(content);
                    for (String term : terms) {
                        if (!getStopWordsList().contains(term)) { // Verifica se o termo não está na lista de stopwords
                            int count = countOccurrences(term, content);
                            invertedIndex.computeIfAbsent(term, k -> new HashMap<>()).put(file.getName(), count);
                        }
                    }
                }
            }
        }
        saveIndex();
    }


    private Set<String> extractTerms(String content) {
        Set<String> terms = new HashSet<>();
        String[] words = content.toLowerCase().split("\\P{L}+"); // Utiliza \P{L} para separar por caracteres não-letra
        for (String word : words) {
            if (!word.isEmpty() && !StopWordsUtils.isStopWord(word)) {
                terms.add(word);
            }
        }
        return terms;
    }


    private int countOccurrences(String term, String content) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(term, index)) != -1) {
            count++;
            index += term.length();
        }
        return count;
    }


    public String getBestResponse(String query) throws IOException {
        String[] words = query.toLowerCase().split("\\P{L}+");
        Map<String, Double> docScores = new HashMap<>();

        for (String word : words) {
            if (invertedIndex.containsKey(word)) {
                Map<String, Double> tfIdfScores = tfIdfCalculator.calculateTfIdfForDocuments(word, invertedIndex.get(word), invertedIndex);
                for (Map.Entry<String, Double> entry : tfIdfScores.entrySet()) {
                    docScores.put(entry.getKey(), docScores.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
                }
            }
        }

        String bestDoc = docScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (bestDoc != null) {
            return readDocument(bestDoc);
        } else {
            return "Desculpe, não encontrei uma resposta adequada.";
        }
    }


    public void updateIndex(String term, String documentName, int count) throws IOException {
        invertedIndex.computeIfAbsent(term, k -> new HashMap<>()).put(documentName, count);
        saveIndex();
    }


    public void updateIndexFromAssignment(String term) throws IOException {
        File folder = new File("src/files/documents");
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String content = reader.lines().reduce("", (acc, line) -> acc + " " + line).toLowerCase();
                    if (content.contains(term.toLowerCase())) {
                        int count = countOccurrences(term, content);
                        updateIndex(term.toLowerCase(), file.getName(), count);
                    }
                }
            }
        }
    }

    // Read the content of a document
    public static String readDocument(String docName) {
        StringBuilder content = new StringBuilder();
        File file = new File("src/files/documents/" + docName);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o documento: " + e.getMessage());
        }

        return content.toString();
    }
}
