// ImpostazioneRequest.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ImpostazioneRequest(
        @NotNull(message = "Il tipo è obbligatorio") TipoBolletta tipo,
        @Min(value = 1, message = "La periodicità deve essere da 1 a 3 mesi")
        @Max(value = 3, message = "La periodicità deve essere da 1 a 3 mesi") int periodicitaMesi,
        @Min(value = 0, message = "Il preavviso deve essere da 0 a 30 giorni")
        @Max(value = 30, message = "Il preavviso deve essere da 0 a 30 giorni") int preavvisoGiorni
) {
}