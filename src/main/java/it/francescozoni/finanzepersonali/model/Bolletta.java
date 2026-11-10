// Bolletta.java
package it.francescozoni.finanzepersonali.model;

import it.francescozoni.finanzepersonali.model.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bollette")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bolletta extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBolletta tipo;

    @Column(nullable = false, length = 120)
    private String fornitore;

    @Column(nullable = false)
    private LocalDate periodoDal;

    @Column(nullable = false)
    private LocalDate periodoAl;

    private LocalDate dataEmissione;

    @Column(nullable = false)
    private LocalDate dataScadenza;

    /** Importo totale della bolletta (negativo solo per i conguagli a credito). */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importo;

    @Column(precision = 15, scale = 3)
    private BigDecimal consumo;

    @Column(length = 60)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NaturaBolletta natura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoBolletta stato;

    private LocalDate dataPagamento;

    /** Chi ha pagato (o pagherà). Null = tu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagante_id")
    private Coinquilino pagante;

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigineBolletta origine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canone_id")
    private Canone canone;
}