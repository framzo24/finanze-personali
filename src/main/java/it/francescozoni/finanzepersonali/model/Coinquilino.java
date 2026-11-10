// Coinquilino.java
package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coinquilini")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coinquilino extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @Column(nullable = false, length = 60)
    private String nome;
}