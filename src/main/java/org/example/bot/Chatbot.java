package org.example.bot;

import static org.example.utils.StopWordsUtils.removeStopWords;
import static org.example.utils.SimilarityUtils.checkSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Chatbot {

    private static final Set<String> instrumentos = new HashSet<>(Set.of(
            "violão", "piano", "teclado", "guitarra", "bateria", "flauta", "violino", "saxofone", "clarinete", "trompete"));
    private static final Set<String> generos = new HashSet<>(Set.of(
            "rock", "jazz", "clássico", "blues", "pop", "samba", "MPB", "bossa nova", "eletrônica", "folk"));
    private static final Set<String> metodos = new HashSet<>(Set.of(
            "escalas", "acordes", "partituras", "improvisação", "técnicas básicas"));

    private static final Map<String, String> perguntas = Map.of(
            "quantas notas estão presentes em um piano padrão?", "<instrumento>",
            "quero comprar um", "<instrumento>",
            "qual é o melhor tipo de", "<instrumento>",
            "como devo começar a aprender a tocar", "<instrumento>"
    );

    private static final Map<String, String> atribuicoes = Map.of(
            "quero um", "<instrumento>",
            "recomendo um", "<instrumento>",
            "sim, quero começar com um", "<instrumento>",
            "escolhi um", "<instrumento>",
            "quero informações sobre o", "<instrumento>",
            "quero aprender a tocar", "<gênero>",
            "você pode começar estudando", "<método>",
            "prefiro estudar", "<método>",
            "meu gênero musical preferido é", "<gênero>"
    );

    public static final Map<String, String> context = new HashMap<>();
    private static final ArrayList<String> symbolTable = new ArrayList<>();
    private static String previousInput = "";
    private static boolean isPreviousAssignmentComplete = true;
    private static boolean waitingForConfirmation = false;

    private static ResponseIndexer responseIndexer;

    static {
        try {
            responseIndexer = new ResponseIndexer("src/files/inverted_index.txt");
            File indexFile = new File("src/files/inverted_index.txt");
            if (indexFile.exists()) {
                responseIndexer.loadIndex();
            } else {
                responseIndexer.createIndexFromDocuments();
                responseIndexer.saveIndex();
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar ou criar o índice: " + e.getMessage());
        }
    }

    public static void processUserInput(String input) {
        input = input.toLowerCase().trim();

        if (input.equals("sair")) {
            System.out.println("Encerrando o chatbot. Até logo!");
            System.exit(0);
        }

        if (input.isEmpty()) {
            System.out.println("Entrada vazia. Tente novamente.");
            return;
        }

        // Verifica se a atribuição está completa
        if (!isPreviousAssignmentComplete) {
            handleSingleWordInput(input);
            isPreviousAssignmentComplete = true;

            // Após tratar a entrada do termo faltante, verifica se a atribuição está completa
            if (context.containsKey("instrumento") || context.containsKey("gênero") || context.containsKey("método")) {
                String completeQuery = previousInput + " " + input;
                System.out.println("Resposta para a consulta" + ":");
                try {
                    String bestResponse = responseIndexer.getBestResponse(completeQuery);
                    System.out.println(bestResponse);
                } catch (IOException e) {
                    System.err.println("Erro ao obter a melhor resposta: " + e.getMessage());
                }
            }

            return; // Após tratar a entrada do termo faltante e buscar a resposta, retorna
        }

        // Processa a entrada se a atribuição anterior estiver completa
        ArrayList<String> wordsList = new ArrayList<>(Set.of(input.split(" ")));
        ArrayList<String> wordsListWithoutStopWords = removeStopWords(wordsList);
        ArrayList<String> uniqueWords = checkSimilarity(wordsListWithoutStopWords);
        updateSymbolTable(uniqueWords);

        boolean recognized = false;
        String recognizedType = null;

        // Verifica se a entrada corresponde a alguma pergunta ou atribuição
        for (Map.Entry<String, String> entry : perguntas.entrySet()) {
            if (input.contains(entry.getKey())) {
                recognized = true;
                recognizedType = entry.getValue();
                break;
            }
        }

        if (!recognized) {
            for (Map.Entry<String, String> entry : atribuicoes.entrySet()) {
                if (input.contains(entry.getKey())) {
                    recognized = true;
                    recognizedType = entry.getValue();
                    break;
                }
            }
        }

        if (recognized) {
            String termToUse = "";
            if (recognizedType.equals("<instrumento>")) {
                termToUse = extractTerm(input, instrumentos);
            } else if (recognizedType.equals("<gênero>")) {
                termToUse = extractTerm(input, generos);
            } else if (recognizedType.equals("<método>")) {
                termToUse = extractTerm(input, metodos);
            }

            if (!termToUse.isEmpty()) {
                context.put(recognizedType.substring(1, recognizedType.length() - 1), termToUse);
                addToSymbolTable(termToUse, recognizedType); // Adiciona o termo reconhecido à tabela de símbolos
                System.out.println("Opa, entendi sua requisição sobre " + termToUse);
                System.out.println("Tabela de símbolos: " + symbolTable);

                // Atualiza o índice com o novo termo de atribuição
                try {
                    responseIndexer.updateIndexFromAssignment(termToUse);
                    String bestResponse = responseIndexer.getBestResponse(previousInput + " " + termToUse);
                    System.out.println("Resposta para a consulta" + ": " + bestResponse);
                } catch (IOException e) {
                    System.err.println("Erro ao obter a melhor resposta: " + e.getMessage());
                }

                previousInput = input;
                isPreviousAssignmentComplete = !checkMissingElements(input, recognizedType);
            } else {
                // Solicita o termo faltante se a atribuição não estiver completa
                String missingTermType = identifyMissingTermType();
                System.out.println("Qual " + missingTermType + " você deseja saber?");
                previousInput = input; // Armazena a entrada anterior
                isPreviousAssignmentComplete = false; // Marca como incompleta até o termo ser fornecido
            }
        } else {
            if (context.containsKey("instrumento") || context.containsKey("gênero") || context.containsKey("método")) {
                String currentType = getContextType(input);
                if (currentType.equals("")) {
                    System.out.println("Não entendi.");
                } else {
                    if (!isPreviousAssignmentComplete) {
                        // Processa a entrada do termo isolado
                        String termType = identifyMissingTermType();
                        if (termType.equals(currentType)) {
                            context.put(currentType, input);
                            addToSymbolTable(input, "<" + currentType + ">"); // Adiciona o termo reconhecido à tabela de símbolos
                            System.out.println("Opa, entendi sua requisição sobre " + input);
                            System.out.println("Tabela de símbolos: " + symbolTable);

                            // Atualiza o índice com o novo termo de atribuição
                            try {
                                responseIndexer.updateIndexFromAssignment(input);
                                String bestResponse = responseIndexer.getBestResponse(previousInput + " " + input);
                                System.out.println("Resposta para a consulta" + ": " + bestResponse);
                            } catch (IOException e) {
                                System.err.println("Erro ao obter a melhor resposta: " + e.getMessage());
                            }

                            isPreviousAssignmentComplete = true;
                        } else {
                            System.out.println("Ainda estamos falando sobre " + context.get(currentType) + "?");
                        }
                    } else {
                        System.out.println("Não entendi.");
                    }
                }
            } else {
                System.out.println("Não entendi.");
                System.out.println("Tabela de símbolos: " + symbolTable);
            }
        }
    }


    private static void handleSingleWordInput(String input) {
        if (instrumentos.contains(input)) {
            context.put("instrumento", input);
            addToSymbolTable(input, "<instrumento>"); // Adiciona o termo reconhecido à tabela de símbolos
            System.out.println("Opa, entendi sua requisição sobre " + input);
            System.out.println("Tabela de símbolos: " + symbolTable);
        } else if (generos.contains(input)) {
            context.put("gênero", input);
            addToSymbolTable(input, "<gênero>"); // Adiciona o termo reconhecido à tabela de símbolos
            System.out.println("Opa, entendi sua requisição sobre " + input);
            System.out.println("Tabela de símbolos: " + symbolTable);
        } else if (metodos.contains(input)) {
            context.put("método", input);
            addToSymbolTable(input, "<método>"); // Adiciona o termo reconhecido à tabela de símbolos
            System.out.println("Opa, entendi sua requisição sobre " + input);
            System.out.println("Tabela de símbolos: " + symbolTable);
        } else {
            System.out.println("Não entendi.");
        }
    }

    private static void addToSymbolTable(String term, String type) {
        String termWithTag = term + type;
        if (!symbolTable.contains(termWithTag)) {
            symbolTable.add(termWithTag);
        }
    }

    private static String identifyMissingTermType() {
        if (!context.containsKey("instrumento")) {
            return "<instrumento>";
        } else if (!context.containsKey("gênero")) {
            return "<gênero>";
        } else if (!context.containsKey("método")) {
            return "<método>";
        }
        return "<instrumento>";
    }

    private static String extractTerm(String input, Set<String> terms) {
        for (String term : terms) {
            if (input.contains(term)) {
                return term;
            }
        }
        return "";
    }

    private static void updateSymbolTable(ArrayList<String> uniqueWords) {
        ArrayList<String> updatedSymbolTable = uniqueWords.stream()
                .map(word -> {
                    if (instrumentos.contains(word)) {
                        return word + "<instrumento>";
                    } else if (generos.contains(word)) {
                        return word + "<gênero>";
                    } else if (metodos.contains(word)) {
                        return word + "<método>";
                    } else {
                        return word;
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));

        symbolTable.addAll(updatedSymbolTable.stream()
                .filter(word -> !symbolTable.contains(word))
                .collect(Collectors.toList()));
    }

    private static boolean checkMissingElements(String input, String type) {
        boolean missing = false;

        if (type.equals("<instrumento>")) {
            if (!containsInstrument(input) && !waitingForConfirmation) {
                System.out.println("Qual instrumento você deseja saber?");
                missing = true;
            }
        } else if (type.equals("<gênero>")) {
            if (!containsGenero(input)) {
                System.out.println("Qual gênero musical você deseja saber?");
                missing = true;
            }
        } else if (type.equals("<método>")) {
            if (!containsMetodo(input)) {
                System.out.println("Qual método você deseja saber?");
                missing = true;
            }
        }

        return missing;
    }

    private static boolean containsInstrument(String input) {
        for (String instrumento : instrumentos) {
            if (input.contains(instrumento)) {
                context.put("instrumento", instrumento);
                return true;
            }
        }
        return false;
    }

    private static boolean containsGenero(String input) {
        for (String genero : generos) {
            if (input.contains(genero)) {
                context.put("gênero", genero);
                return true;
            }
        }
        return false;
    }

    private static boolean containsMetodo(String input) {
        for (String metodo : metodos) {
            if (input.contains(metodo)) {
                context.put("método", metodo);
                return true;
            }
        }
        return false;
    }

    private static String getContextType(String input) {
        if (context.containsKey("instrumento") && input.contains(context.get("instrumento"))) {
            return "instrumento";
        } else if (context.containsKey("gênero") && input.contains(context.get("gênero"))) {
            return "gênero";
        } else if (context.containsKey("método") && input.contains(context.get("método"))) {
            return "método";
        }
        return "";
    }

}
