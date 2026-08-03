package it.francescozoni.finanzepersonali.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movimento {

    private Long id;

    private String descrizione;

    private BigDecimal importo;

    private TipoMovimento tipo;

    private Categoria categoria;

    private LocalDate data;
}
