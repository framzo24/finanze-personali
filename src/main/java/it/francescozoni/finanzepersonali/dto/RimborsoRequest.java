// RimborsoRequest.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.DirezioneRimborso;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RimborsoRequest(
        @NotNull(message = "Scegli il coinquilino") Long coinquilinoId,
        @NotNull(message = "Scegli la direzione del rimborso") DirezioneRimborso direzione,
        @NotNull(message = "L'importo è obbligatorio")
        @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero") BigDecimal importo,
        @NotNull(message = "La data è obbligatoria") LocalDate data,
        @Size(max = 500, message = "La nota può avere al massimo 500 caratteri") String nota,
        Boolean registraMovimento,
        TipoBolletta tipo
) {
}