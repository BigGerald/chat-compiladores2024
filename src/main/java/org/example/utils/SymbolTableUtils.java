package org.example.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolTableUtils {
    public static ArrayList<String> getSymbolTable(ArrayList<String> inputWithoutStopwords){
        ArrayList<String> symbolTable = new ArrayList<>();
        for (String word : inputWithoutStopwords) {
            if (!word.matches("[\\p{Punct}]+")) {
                symbolTable.add(word);
            }
        }
        return removePunctuation(symbolTable);
    }

    public static ArrayList<String> removePunctuation(ArrayList<String> symbols) {
        ArrayList<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("[\\p{L}\\d-]+");
        for (String c : symbols) {
            Matcher matcher = pattern.matcher(c);
            while (matcher.find()) {
                result.add(matcher.group());
            }
        }
        Collections.sort(result);
        return result;
    }
}
