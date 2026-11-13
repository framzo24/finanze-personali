// SaldoCoinquilino.java
package it.francescozoni.finanzepersonali.dto;

import java.math.BigDecimal;

/** saldo positivo = ti deve dei soldi, negativo = glieli devi tu. */
public record SaldoCoinquilino(
        Long coinquilinoId,
        String nome,
        BigDecimal saldo,
        BigDecimal daBollette,
        BigDecimal daRimborsi
) {
}