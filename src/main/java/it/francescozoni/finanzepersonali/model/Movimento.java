package it.francescozoni.finanzepersonali.model;

import it.francescozoni.finanzepersonali.model.enums.StatoMovimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimenti")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movimento extends BaseEntity {

    @Column(nullable = false)
    private String descrizione;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoMovimento stato;

    @Column(nullable = false)
    private LocalDateTime dataMovimento;

    @Column(length = 1000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}
