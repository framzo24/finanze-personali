// BollettePageResponse.java
package it.francescozoni.finanzepersonali.dto;

import java.util.List;

public record BollettePageResponse(
        List<BollettaResponse> contenuto,
        int pagina,
        int dimensione,
        long totaleElementi,
        int totalePagine
) {
}