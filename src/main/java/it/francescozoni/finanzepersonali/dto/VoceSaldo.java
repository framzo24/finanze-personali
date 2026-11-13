// VoceSaldo.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una riga del dettaglio: tipo è BOLLETTA oppure RIMBORSO. L'effetto è positivo se lui ti deve di più. */
public record VoceSaldo(
        String tipo,
        Long id,
        LocalDate data,
        String titolo,
        String dettaglio,
        BigDecimal effetto,
        BigDecimal progressivo,
        TipoBolletta tipoBolletta,
        Long movimentoId
) {
}