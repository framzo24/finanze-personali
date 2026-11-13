package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diario",
        uniqueConstraints = @UniqueConstraint(name = "uk_diario_utente_giorno", columnNames = {"utente_id", "giorno"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoceDiario extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(nullable = false)
    private LocalDate giorno;

    @Column(nullable = false, length = 20000)
    private String testo;
}