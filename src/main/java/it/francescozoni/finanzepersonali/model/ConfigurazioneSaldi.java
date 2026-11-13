package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "configurazioni_saldi",
        uniqueConstraints = @UniqueConstraint(name = "uk_conf_saldi_utente", columnNames = "utente_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurazioneSaldi extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    /** Le bollette pagate e i rimborsi prima di questa data non entrano nei saldi. Null = tutto lo storico. */
    private LocalDate saldiDal;

    /** Falso finché non hai scelto: serve a mostrare l'avviso. */
    @Column(nullable = false)
    private boolean configurato;
}