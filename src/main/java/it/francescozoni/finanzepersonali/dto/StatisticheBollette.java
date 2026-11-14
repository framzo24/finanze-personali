package it.francescozoni.finanzepersonali.dto;

import java.time.LocalDate;
import java.util.List;

public record StatisticheBollette(
        int anno,
        LocalDate fino,
        LocalDate finoAnnoScorso,
        List<StatisticaTipo> tipi,
        List<CostoUnitario> costi
) {
}