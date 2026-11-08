package it.francescozoni.finanzepersonali.exception;

public class UtenteGiaEsistenteException extends RuntimeException {
    public UtenteGiaEsistenteException(String username) {
        super("Esiste già un utente con nome utente \"" + username + "\"");
    }
}