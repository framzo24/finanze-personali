// NotificaResponse.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.LivelloScadenza;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.time.LocalDate;

public record NotificaResponse(
        String id,
        LivelloScadenza livello,
        TipoBolletta tipo,
        String titolo,
        String messaggio,
        Long bollettaId,
        LocalDate data
) {
}