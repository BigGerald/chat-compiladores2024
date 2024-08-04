package org.example.bot;

import org.example.utils.StopWordsUtils;

import java.io.*;
import java.util.*;

public class ResponseIndexer {

    private Map<String, Set<String>> invertedIndex = new HashMap<>();
    private final String indexFilePath;

    public ResponseIndexer(String indexFilePath) {
        this.indexFilePath = indexFilePath;
    }

    // Load the index from the file
    public void loadIndex() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(indexFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String term = parts[0];
                    Set<String> docs = new HashSet<>(Arrays.asList(parts[1].split(",")));
                    invertedIndex.put(term, docs);
                }
            }
        }
    }

    // Save the index to the file
    public void saveIndex() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexFilePath))) {
            for (Map.Entry<String, Set<String>> entry : invertedIndex.entrySet()) {
                writer.write(entry.getKey() + ":" + String.join(",", entry.getValue()));
                writer.newLine();
            }
        }
    }

    // Create index from documents
    public void createIndexFromDocuments() throws IOException {
        File folder = new File("src/files/documents");
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String content = reader.lines().reduce("", (acc, line) -> acc + " " + line).toLowerCase();
                    Set<String> terms = extractTerms(content);
                    for (String term : terms) {
                        invertedIndex.computeIfAbsent(term, k -> new HashSet<>()).add(file.getName());
                    }
                }
            }
        }
        saveIndex();
    }

    // Extract terms from content
    private Set<String> extractTerms(String content) {
        Set<String> terms = new HashSet<>();
        // Divida o conteúdo em palavras, mantendo a acentuação
        String[] words = content.toLowerCase().split("\\P{L}+"); // Utiliza \P{L} para separar por caracteres não-letra
        for (String word : words) {
            if (!word.isEmpty() && !StopWordsUtils.isStopWord(word)) {
                terms.add(word);
            }
        }
        return terms;
    }

    // Get the best response based on the query
    public String getBestResponse(String query) throws IOException {
        String[] words = query.toLowerCase().split("\\P{L}+"); // Utiliza \P{L} para separar por caracteres não-letra
        Map<String, Integer> docScores = new HashMap<>();
        for (String word : words) {
            if (invertedIndex.containsKey(word)) {
                for (String doc : invertedIndex.get(word)) {
                    docScores.put(doc, docScores.getOrDefault(doc, 0) + 1);
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

    // Dynamically update the index with new terms
    public void updateIndex(String term, String documentName) throws IOException {
        invertedIndex.computeIfAbsent(term, k -> new HashSet<>()).add(documentName);
        saveIndex();
    }

    // Dynamically update the index from new assignments
    public void updateIndexFromAssignment(String term) throws IOException {
        File folder = new File("src/files/documents");
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String content = reader.lines().reduce("", (acc, line) -> acc + " " + line).toLowerCase();
                    if (content.contains(term.toLowerCase())) {
                        updateIndex(term.toLowerCase(), file.getName());
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
