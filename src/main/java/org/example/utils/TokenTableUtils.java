package org.example.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenTableUtils {
    static final String regex = "[\\p{Punct}\\s]+";
    static final Pattern pattern = Pattern.compile(regex);

    public static Queue<String> getTokenTable(ArrayList<String> words) {
        Queue<String> tokens = new LinkedList<>();
        for (String palavra : words) {
            ArrayList<String> palavraTokens = splitTokens(palavra);
            tokens.addAll(palavraTokens);
        }
        return tokens;
    }

    private static ArrayList<String> splitTokens(String palavra) {
        ArrayList<String> tokens = new ArrayList<>();
        Matcher matcher = pattern.matcher(palavra);
        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                tokens.add(palavra.substring(lastIndex, matcher.start()));
            }
            tokens.add(matcher.group());
            lastIndex = matcher.end();
        }
        if (lastIndex < palavra.length()) {
            tokens.add(palavra.substring(lastIndex));
        }
        return tokens;
    }
}
