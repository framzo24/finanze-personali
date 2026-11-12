// ScadenzaResponse.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.LivelloScadenza;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Una bolletta da pagare oppure la prossima attesa ("prevista"). */
public record ScadenzaResponse(
        Long bollettaId,
        TipoBolletta tipo,
        String fornitore,
        LocalDate data,
        BigDecimal importo,
        BigDecimal quotaTua,
        boolean prevista,
        long giorni,
        LivelloScadenza livello,
        Long paganteId,
        String paganteNome,
        LocalDate periodoDal,
        LocalDate periodoAl
) {
}