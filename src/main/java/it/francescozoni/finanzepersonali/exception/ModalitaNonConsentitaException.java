// ModalitaNonConsentitaException.java
package it.francescozoni.finanzepersonali.exception;

public class ModalitaNonConsentitaException extends RuntimeException {
    public ModalitaNonConsentitaException() {
        super("Questa funzione non è disponibile con la modalità di gestione delle bollette che hai scelto");
    }
}