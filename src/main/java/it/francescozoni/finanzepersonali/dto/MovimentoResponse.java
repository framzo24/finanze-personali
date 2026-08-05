package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimentoResponse (

    Long id,

    String descrizione,

    BigDecimal importo,

    TipoMovimento tipo,

    String categoria,

    LocalDateTime data
){


}
