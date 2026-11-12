// Canone.java
package it.francescozoni.finanzepersonali.model;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "canoni")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Canone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBolletta tipo;

    @Column(nullable = false, length = 120)
    private String fornitore;

    /** Giorno del mese in cui arriva la bolletta (1-28). */
    @Column(nullable = false)
    private int giornoScadenza;

    /** Chi paga di solito. Null = tu. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagante_id")
    private Coinquilino pagante;

    @Column(nullable = false)
    private boolean attivo;

    /** Addebito automatico: alla scadenza la bolletta risulta pagata. Null = no. */
    private Boolean pagamentoAutomatico;
}