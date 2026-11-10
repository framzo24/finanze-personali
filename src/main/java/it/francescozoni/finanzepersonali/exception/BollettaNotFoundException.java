// BollettaNotFoundException.java
package it.francescozoni.finanzepersonali.exception;

public class BollettaNotFoundException extends RuntimeException {
    public BollettaNotFoundException(Long id) {
        super("Bolletta non trovata (id=" + id + ")");
    }
}