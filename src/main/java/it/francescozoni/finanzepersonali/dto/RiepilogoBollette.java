// RiepilogoBollette.java
package it.francescozoni.finanzepersonali.dto;

import java.math.BigDecimal;
import java.util.List;

public record RiepilogoBollette(
        List<RiepilogoTipo> tipi,
        int daPagare,
        BigDecimal daPagareImporto,
        BigDecimal daPagareQuota
) {
}