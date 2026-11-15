package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;

public record UtenteResponse(Long id, String nome, String cognome, String username, ModalitaBollette modalitaBollette) {
}