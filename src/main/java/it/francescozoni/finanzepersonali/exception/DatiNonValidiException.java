package it.francescozoni.finanzepersonali.exception;

public class DatiNonValidiException extends RuntimeException {
    public DatiNonValidiException(String messaggio) {
        super(messaggio);
    }
}