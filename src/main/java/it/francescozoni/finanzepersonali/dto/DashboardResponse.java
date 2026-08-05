package it.francescozoni.finanzepersonali.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DashboardResponse(
        BigDecimal saldo,

        BigDecimal totaleEntrate,

        BigDecimal totaleUscite,

        BigDecimal entrateMeseCorrente,

        BigDecimal usciteMeseCorrente,

        List<CategoriaTotaleResponse> spesePerCategoria,

        List<AndamentoMensileResponse> andamentoMensile
) {

}
