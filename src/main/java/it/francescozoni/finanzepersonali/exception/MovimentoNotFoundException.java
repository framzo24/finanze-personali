package it.francescozoni.finanzepersonali.exception;

public class MovimentoNotFoundException extends RuntimeException {

    public MovimentoNotFoundException(Long id) {
        super("Movimento con id " + id + " non trovato.");
    }
}
