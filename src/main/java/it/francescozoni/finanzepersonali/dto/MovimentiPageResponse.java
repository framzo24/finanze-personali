package it.francescozoni.finanzepersonali.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MovimentiPageResponse (

    List<MovimentoResponse> contenuto,

    int pagina,

    int dimensione,

    long totaleElementi,

    int totalePagine
){}
