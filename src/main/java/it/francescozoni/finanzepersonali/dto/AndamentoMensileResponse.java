package it.francescozoni.finanzepersonali.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AndamentoMensileResponse(
        String mese,
        BigDecimal entrate,
        BigDecimal uscite
) {
}
