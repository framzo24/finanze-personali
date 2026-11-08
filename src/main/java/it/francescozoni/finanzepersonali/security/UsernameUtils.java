package it.francescozoni.finanzepersonali.security;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

public final class UsernameUtils {

    private UsernameUtils() {
    }

    /** Minuscolo, senza accenti, solo lettere e numeri. */
    public static String normalizza(String valore) {
        if (valore == null) {
            return "";
        }
        return Normalizer.normalize(valore.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]", "");
    }

    /** "nome.cognome", oppure vuoto se uno dei due non contiene caratteri validi. */
    public static Optional<String> costruisci(String nome, String cognome) {
        String n = normalizza(nome);
        String c = normalizza(cognome);
        return n.isEmpty() || c.isEmpty() ? Optional.empty() : Optional.of(n + "." + c);
    }
}