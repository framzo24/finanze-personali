// RimborsoResponse.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.DirezioneRimborso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RimborsoResponse(
        Long id,
        Long coinquilinoId,
        String coinquilinoNome,
        DirezioneRimborso direzione,
        BigDecimal importo,
        LocalDate data,
        String nota,
        Long movimentoId
) {
}