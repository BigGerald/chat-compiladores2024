package org.example;


import org.example.bot.Chatbot;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Bem-vindo ao chatbot! Digite suas perguntas ou atribuições.");

        while (true) {
            System.out.print("Digite sua pergunta: ");
            String input = scanner.nextLine();

            Chatbot.processUserInput(input);
        }
    }
}
