package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentoFilter {
    private LocalDate dataDa;

    private LocalDate dataA;

    private Long categoriaId;

    private TipoMovimento tipo;

    private BigDecimal importoMin;

    private BigDecimal importoMax;

    private String descrizione;
}
