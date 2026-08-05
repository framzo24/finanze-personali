package it.francescozoni.finanzepersonali.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CategoriaTotaleResponse(
        Long categoriaId,

        String categoria,

        BigDecimal totale
) {
}
