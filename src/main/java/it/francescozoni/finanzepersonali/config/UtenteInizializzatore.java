package it.francescozoni.finanzepersonali.config;

import it.francescozoni.finanzepersonali.model.Utente;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UtenteInizializzatore implements ApplicationRunner {

    private final UtenteRepository utenti;
    private final JdbcTemplate jdbc;

    public UtenteInizializzatore(UtenteRepository utenti, JdbcTemplate jdbc) {
        this.utenti = utenti;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        Utente proprietario = utenti.findByUsername(AuthService.USERNAME_PROPRIETARIO)
                .orElseGet(() -> {
                    log.info("Creo l'account proprietario {}", AuthService.USERNAME_PROPRIETARIO);
                    return utenti.save(Utente.builder()
                            .nome("Francesco")
                            .cognome("Zoni")
                            .username(AuthService.USERNAME_PROPRIETARIO)
                            .passwordHash(null)
                            .build());
                });

        // i dati creati prima dell'autenticazione diventano tuoi
        assegna("movimenti", proprietario);
        assegna("categorie", proprietario);

        rimuoviUnicitaGlobaleNomeCategoria();
    }

    private void assegna(String tabella, Utente proprietario) {
        int righe = jdbc.update("UPDATE " + tabella + " SET utente_id = ? WHERE utente_id IS NULL",
                proprietario.getId());
        if (righe > 0) {
            log.info("Assegnati {} record di {} a {}", righe, tabella, proprietario.getUsername());
        }
    }

    /**
     * Il nome della categoria era univoco per tutti. Ora lo è per utente (controllo nel servizio),
     * ma Hibernate non toglie da solo un vincolo già presente nel database.
     */
    private void rimuoviUnicitaGlobaleNomeCategoria() {
        try {
            List<String> vincoli = jdbc.queryForList("""
                    SELECT tc.CONSTRAINT_NAME
                    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                    JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE k
                      ON k.CONSTRAINT_SCHEMA = tc.CONSTRAINT_SCHEMA
                     AND k.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
                    WHERE tc.CONSTRAINT_TYPE = 'UNIQUE'
                      AND UPPER(tc.TABLE_NAME) = 'CATEGORIE'
                      AND UPPER(k.COLUMN_NAME) = 'NOME'
                    """, String.class);

            for (String vincolo : vincoli) {
                jdbc.execute("ALTER TABLE categorie DROP CONSTRAINT \"" + vincolo.replace("\"", "\"\"") + "\"");
                log.info("Rimosso il vincolo di unicità globale sul nome delle categorie ({})", vincolo);
            }
        } catch (DataAccessException e) {
            log.warn("Non sono riuscito a rimuovere il vincolo di unicità sul nome delle categorie: {}. "
                    + "Dalla console H2 esegui: ALTER TABLE categorie DROP CONSTRAINT <nome del vincolo>", e.getMessage());
        }
    }
}