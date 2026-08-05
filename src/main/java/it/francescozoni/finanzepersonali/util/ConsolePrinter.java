package it.francescozoni.finanzepersonali.util;

import org.springframework.stereotype.Component;

@Component
public class ConsolePrinter {

    public void println(String testo) {
        System.out.println(testo);
    }

    public void print(String testo) {
        System.out.print(testo);
    }

}