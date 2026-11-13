package it.francescozoni.finanzepersonali.model;

import it.francescozoni.finanzepersonali.model.enums.DirezioneRimborso;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "rimborsi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rimborso extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coinquilino_id", nullable = false)
    private Coinquilino coinquilino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DirezioneRimborso direzione;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 500)
    private String nota;

    /** La spesa registrata nei movimenti (solo per i rimborsi che hai dato tu). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimento_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Movimento movimento;
}