// TariffaRequest.java
package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TariffaRequest(
        @NotNull(message = "L'importo è obbligatorio")
        @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero") BigDecimal importo,
        @NotNull(message = "Il mese di partenza è obbligatorio") LocalDate validaDal
) {
}