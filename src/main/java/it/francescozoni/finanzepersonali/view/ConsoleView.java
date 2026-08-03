package it.francescozoni.finanzepersonali.view;

import it.francescozoni.finanzepersonali.model.Movimento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleView {

    private final Scanner scanner = new Scanner(System.in);

    public void stampaMenu() {
        System.out.println();
        System.out.println("=================================");
        System.out.println("     GESTIONE FINANZE PERSONALI");
        System.out.println("=================================");
        System.out.println("1. Inserisci movimento");
        System.out.println("2. Elenca movimenti");
        System.out.println("3. Visualizza saldo");
        System.out.println("4. Totale entrate");
        System.out.println("5. Totale uscite");
        System.out.println("6. Elimina movimento");
        System.out.println("0. Esci");
        System.out.println("---------------------------------");
        System.out.print("Scelta: ");
    }

    public String leggiInput() {
        return scanner.nextLine();
    }

    public void stampaMessaggio(String messaggio) {
        System.out.println(messaggio);
    }

    public String leggiStringa(String messaggio) {
        System.out.print(messaggio);
        return scanner.nextLine();
    }

    public Long leggiId() {
        System.out.print("Inserisci ID movimento: ");
        return Long.parseLong(scanner.nextLine());
    }

    public void stampaMovimentoSalvato() {
        System.out.println("Movimento salvato con successo.");
    }

    public void stampaMovimenti(List<Movimento> movimenti) {

        if (movimenti.isEmpty()) {
            System.out.println("Nessun movimento presente.");

            return;
        }

        System.out.println();
        System.out.println("========== MOVIMENTI ==========");

        movimenti.forEach(m -> {
            System.out.println(
                    "ID: " + m.getId()
                            + " | "
                            + m.getData()
                            + " | "
                            + m.getDescrizione()
                            + " | "
                            + m.getTipo()
                            + " | "
                            + m.getCategoria()
                            + " | € "
                            + m.getImporto()
            );
        });

        System.out.println("===============================");
    }
}
