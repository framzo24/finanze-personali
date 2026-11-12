// CanoneRequest.java
package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CanoneRequest(
        @NotBlank(message = "Il fornitore è obbligatorio")
        @Size(max = 120, message = "Il fornitore può avere al massimo 120 caratteri") String fornitore,
        @Min(value = 1, message = "Il giorno deve essere tra 1 e 28")
        @Max(value = 28, message = "Il giorno deve essere tra 1 e 28") int giornoScadenza,
        Long paganteId,
        boolean pagamentoAutomatico,
        boolean attivo
) {
}