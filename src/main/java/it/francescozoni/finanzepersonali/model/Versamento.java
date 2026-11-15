package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Un invio di soldi al proprietario per la quota di una bolletta (tuo, oppure dichiarato da un coinquilino). */
@Entity
@Table(name = "versamenti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Versamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bolletta_id", nullable = false)
    private Bolletta bolletta;

    /** Chi ha inviato: null = tu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coinquilino_id")
    private Coinquilino coinquilino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(length = 500)
    private String nota;

    /** La spesa registrata nei movimenti (solo per i tuoi invii). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimento_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Movimento movimento;
}