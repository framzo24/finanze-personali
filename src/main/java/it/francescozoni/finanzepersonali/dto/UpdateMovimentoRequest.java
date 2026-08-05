package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMovimentoRequest {

    @NotNull(message = "L'importo è obbligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "L'importo deve essere maggiore di zero")
    private BigDecimal importo;

    @NotNull(message = "La data è obbligatoria")
    private LocalDate data;

    @NotBlank(message = "La descrizione è obbligatoria")
    @Size(max = 255, message = "La descrizione non può superare i 255 caratteri")
    private String descrizione;

    @NotNull(message = "La categoria è obbligatoria")
    private Long categoriaId;

    @NotNull(message = "Il tipo movimento è obbligatorio")
    private TipoMovimento tipo;

    @Size(max = 1000, message = "Le note non possono superare i 1000 caratteri")
    private String note;
}
