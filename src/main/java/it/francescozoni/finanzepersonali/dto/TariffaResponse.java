// TariffaResponse.java
package it.francescozoni.finanzepersonali.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TariffaResponse(Long id, BigDecimal importo, LocalDate validaDal, boolean inVigore) {
}