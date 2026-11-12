// CanoneResponse.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.math.BigDecimal;
import java.util.List;

public record CanoneResponse(
        Long id,
        TipoBolletta tipo,
        String fornitore,
        int giornoScadenza,
        Long paganteId,
        String paganteNome,
        boolean pagamentoAutomatico,
        boolean attivo,
        BigDecimal importoAttuale,
        List<TariffaResponse> tariffe
) {
}