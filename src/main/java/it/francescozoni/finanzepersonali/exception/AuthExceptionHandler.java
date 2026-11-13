package it.francescozoni.finanzepersonali.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    @ExceptionHandler(CredenzialiNonValideException.class)
    ResponseEntity<Map<String, List<String>>> credenziali(CredenzialiNonValideException e) {
        return risposta(HttpStatus.UNAUTHORIZED, e);
    }

    @ExceptionHandler(UtenteGiaEsistenteException.class)
    ResponseEntity<Map<String, List<String>>> esistente(UtenteGiaEsistenteException e) {
        return risposta(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(DatiNonValidiException.class)
    ResponseEntity<Map<String, List<String>>> nonValidi(DatiNonValidiException e) {
        return risposta(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(BollettaNotFoundException.class)
    ResponseEntity<Map<String, List<String>>> bollettaNonTrovata(BollettaNotFoundException e) {
        return risposta(HttpStatus.NOT_FOUND, e);
    }

    private ResponseEntity<Map<String, List<String>>> risposta(HttpStatus stato, RuntimeException e) {
        return ResponseEntity.status(stato).body(Map.of("messages", List.of(e.getMessage())));
    }

    @ExceptionHandler(CanoneNotFoundException.class)
    ResponseEntity<Map<String, List<String>>> canoneNonTrovato(CanoneNotFoundException e) {
        return risposta(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(RimborsoNotFoundException.class)
    ResponseEntity<Map<String, List<String>>> rimborsoNonTrovato(RimborsoNotFoundException e) {
        return risposta(HttpStatus.NOT_FOUND, e);
    }
}