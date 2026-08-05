package it.francescozoni.finanzepersonali.util;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class InputReader {

    private final Scanner scanner = new Scanner(System.in);

    public String nextLine() {
        return scanner.nextLine();
    }
}