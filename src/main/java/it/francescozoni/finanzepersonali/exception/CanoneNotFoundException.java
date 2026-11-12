// exception/CanoneNotFoundException.java
package it.francescozoni.finanzepersonali.exception;

public class CanoneNotFoundException extends RuntimeException {
    public CanoneNotFoundException(Long id) {
        super("Canone non trovato (id=" + id + ")");
    }
}