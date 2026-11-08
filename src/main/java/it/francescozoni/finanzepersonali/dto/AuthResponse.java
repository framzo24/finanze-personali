package it.francescozoni.finanzepersonali.dto;

public record AuthResponse(String token, long scadenzaMs, UtenteResponse utente) {
}