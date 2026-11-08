package it.francescozoni.finanzepersonali.exception;

public class CredenzialiNonValideException extends RuntimeException {
    public CredenzialiNonValideException() {
        super("Nome utente o password non corretti");
    }
}