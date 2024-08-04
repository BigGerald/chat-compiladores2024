package org.example.utils;

import java.util.ArrayList;
import java.util.List;

public class LexicalCheckUtils {
    public static ArrayList<String> getAlphabet() {
        ArrayList<String> alphabet = new ArrayList<>();
        for (char i = 'A'; i <= 'Z'; i++) {
            alphabet.add(String.valueOf(i));
        }
        for (char i = 'a'; i <= 'z'; i++) {
            alphabet.add(String.valueOf(i));
        }
        for (char i = '0'; i <= '9'; i++) {
            alphabet.add(String.valueOf(i));
        }
        List<Character> characters = getCharacters();
        for (char c : characters) {
            alphabet.add(String.valueOf(c));
        }
        alphabet.add("...");
        alphabet.add("∵∴");
        return alphabet;
    }
    private static List<Character> getCharacters() {
        char[] chars = {
                'ç', 'á', 'é', 'í', 'ó', 'ú', 'ã', 'õ', 'ê', 'î', 'ô', 'û', 'à', 'è', 'ì', 'ò', 'ù', ',', '.', '!', '?', ':', ';', '(', ')', '-', '"', '\'', '/', '`', '\\', '~', '^', '[', ']', '{', '}', '@', '#', '$', '%', '&', '*', '_', '=', '+', '|', '\\',
                '§', 'ª', 'º', '±', '×', '÷', '√', '>', '<', '≤', '≥', '∞', '∂', '∆', '∑', '∏', '∫', '∮', 'Ω', '∓', '∓', '←', '↑', '→', '↓', '↔', '↵', '⇐', '⇑', '⇒', '⇓', '⇔', '∀', '∃', '∈', '∉', '∠', '⊥', '∘', '∩', '∪', '∧', '∨', '¬', '∫', '∬', '∭', '∮', '∵', '∴', '∎'
        };
        List<Character> characters = new ArrayList<>();
        for (char c : chars) {
            characters.add(c);
        }
        return characters;
    }
    public static ArrayList<String> analiseInput(String input) {
        ArrayList<String> words = new ArrayList<>();
        StringBuilder actualWord = new StringBuilder();

        for (char character : input.toLowerCase().toCharArray()) {
            if (isValidCharacter(character)) {
                actualWord.append(character);
            } else {
                // Se encontro um caracter inválido, terminamo a palavra atual e a adiciono à lista
                if (actualWord.length() > 0) {
                    words.add(actualWord.toString());
                    actualWord.setLength(0); // Limpa a StringBuilder para a próxima palavra
                }
            }
        }
        if (actualWord.length() > 0) {
            words.add(actualWord.toString());
        }
        return words;
    }
    private static boolean isValidCharacter(char c) {
        ArrayList<String> alphabet = getAlphabet();
        return alphabet.contains(String.valueOf(c));
    }
}
