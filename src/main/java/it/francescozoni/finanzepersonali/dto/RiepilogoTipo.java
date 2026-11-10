// RiepilogoTipo.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.math.BigDecimal;
import java.util.List;

public record RiepilogoTipo(
        TipoBolletta tipo,
        BollettaResponse ultima,
        BigDecimal totaleAnno,
        BigDecimal totaleAnnoQuota,
        BigDecimal media,
        BigDecimal mediaQuota,
        int daPagare,
        List<PuntoAndamento> andamento
) {
}