// BollettaResponse.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.OrigineBolletta;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BollettaResponse(
        Long id,
        TipoBolletta tipo,
        String fornitore,
        LocalDate periodoDal,
        LocalDate periodoAl,
        LocalDate dataEmissione,
        LocalDate dataScadenza,
        BigDecimal importo,
        BigDecimal consumo,
        String unitaConsumo,
        BigDecimal costoUnitario,
        String numeroDocumento,
        NaturaBolletta natura,
        StatoBolletta stato,
        boolean scaduta,
        LocalDate dataPagamento,
        Long paganteId,
        String paganteNome,
        BigDecimal quotaTua,
        String note,
        OrigineBolletta origine
) {
}