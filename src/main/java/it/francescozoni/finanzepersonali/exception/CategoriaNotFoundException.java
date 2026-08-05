package it.francescozoni.finanzepersonali.exception;

public class CategoriaNotFoundException extends RuntimeException {

    public CategoriaNotFoundException(Long id) {
        super("Categoria con id " + id + " non trovata.");
    }
}
