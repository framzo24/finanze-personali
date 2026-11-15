// VersamentoNotFoundException.java
package it.francescozoni.finanzepersonali.exception;

public class VersamentoNotFoundException extends RuntimeException {
    public VersamentoNotFoundException(Long id) {
        super("Invio non trovato (id=" + id + ")");
    }
}