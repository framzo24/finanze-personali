// exception/RimborsoNotFoundException.java
package it.francescozoni.finanzepersonali.exception;

public class RimborsoNotFoundException extends RuntimeException {
    public RimborsoNotFoundException(Long id) {
        super("Rimborso non trovato (id=" + id + ")");
    }
}