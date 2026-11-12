package it.francescozoni.finanzepersonali.model;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "impostazioni_bolletta",
        uniqueConstraints = @UniqueConstraint(name = "uk_impostazioni_utente_tipo", columnNames = {"utente_id", "tipo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpostazioneBolletta extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBolletta tipo;

    @Column(nullable = false)
    private int periodicitaMesi;

    @Column(nullable = false)
    private int preavvisoGiorni;
}