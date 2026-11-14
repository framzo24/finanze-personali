package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.math.BigDecimal;

public record StatisticaTipo(
        TipoBolletta tipo,
        BigDecimal annoCorrente,
        BigDecimal annoCorrenteQuota,
        BigDecimal annoScorso,
        BigDecimal annoScorsoQuota,
        int numeroAnnoCorrente,
        int numeroAnnoScorso
) {
}