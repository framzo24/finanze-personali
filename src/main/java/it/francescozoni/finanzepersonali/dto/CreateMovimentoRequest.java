package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.StatoMovimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovimentoRequest {

    @NotBlank(message = "La descrizione è obbligatoria")
    private String descrizione;

    @NotNull(message = "L'importo è obbligatorio")
    @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero")
    private BigDecimal importo;

    @NotNull(message = "Il tipo movimento è obbligatorio")
    private TipoMovimento tipo;

    @NotNull(message = "La categoria è obbligatoria")
    private Long categoriaId;

    private LocalDateTime dataMovimento;

    private StatoMovimento stato;

    private String note;
}
