package it.francescozoni.finanzepersonali.model;

import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "utenti", uniqueConstraints = @UniqueConstraint(name = "uk_utenti_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utente extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, length = 60)
    private String cognome;

    @Column(nullable = false, length = 130)
    private String username;

    /** Hash BCrypt. Null = account ad accesso libero (solo il proprietario). */
    @Column(length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ModalitaBollette modalitaBollette;
}