// BollettaRequest.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BollettaRequest(
        @NotNull(message = "Il tipo è obbligatorio") TipoBolletta tipo,
        @NotBlank(message = "Il fornitore è obbligatorio")
        @Size(max = 120, message = "Il fornitore può avere al massimo 120 caratteri") String fornitore,
        @NotNull(message = "L'inizio del periodo è obbligatorio") LocalDate periodoDal,
        @NotNull(message = "La fine del periodo è obbligatoria") LocalDate periodoAl,
        LocalDate dataEmissione,
        @NotNull(message = "La scadenza è obbligatoria") LocalDate dataScadenza,
        @NotNull(message = "L'importo è obbligatorio") BigDecimal importo,
        BigDecimal consumo,
        @Size(max = 60, message = "Il numero documento può avere al massimo 60 caratteri") String numeroDocumento,
        NaturaBolletta natura,
        StatoBolletta stato,
        LocalDate dataPagamento,
        Long paganteId,
        @Size(max = 1000, message = "Le note possono avere al massimo 1000 caratteri") String note,
        Boolean registraMovimento
) {
}