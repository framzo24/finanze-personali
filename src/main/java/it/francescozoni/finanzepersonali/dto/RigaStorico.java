// RigaStorico.java
package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RigaStorico(
        @NotNull LocalDate periodoDal,
        @NotNull LocalDate periodoAl,
        @NotNull LocalDate dataScadenza,
        @NotNull BigDecimal importo,
        BigDecimal consumo,
        Long paganteId
) {
}