package it.francescozoni.finanzepersonali.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
}