// SaldiResponse.java
package it.francescozoni.finanzepersonali.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SaldiResponse(
        boolean configurato,
        LocalDate saldiDal,
        List<SaldoCoinquilino> coinquilini,
        BigDecimal totale
) {
}