package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.CredenzialiNonValideException;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.exception.UtenteGiaEsistenteException;
import it.francescozoni.finanzepersonali.model.Utente;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.JwtService;
import it.francescozoni.finanzepersonali.security.UsernameUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
public class AuthService {

    public static final String USERNAME_PROPRIETARIO = "francesco.zoni";

    private final UtenteRepository utenti;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final boolean accessoLibero;
    private final String hashFinto;

    public AuthService(UtenteRepository utenti,
                       PasswordEncoder encoder,
                       JwtService jwtService,
                       @Value("${app.security.accesso-libero:false}") boolean accessoLibero) {
        this.utenti = utenti;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.accessoLibero = accessoLibero;
        // serve a rendere uguale il tempo di risposta quando l'utente non esiste
        this.hashFinto = encoder.encode("password-finta");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest richiesta) {
        String username = richiesta.username() == null ? "" : richiesta.username().trim().toLowerCase(Locale.ROOT);
        String password = richiesta.password() == null ? "" : richiesta.password();

        Utente utente;
        if (username.isEmpty() && password.isEmpty()) {
            // accesso rapido: solo l'account proprietario, e solo se abilitato
            if (!accessoLibero) {
                throw new CredenzialiNonValideException();
            }
            utente = utenti.findByUsername(USERNAME_PROPRIETARIO)
                    .filter(u -> u.getPasswordHash() == null)
                    .orElseThrow(CredenzialiNonValideException::new);
        } else {
            utente = utenti.findByUsername(username).orElse(null);
            boolean conPassword = utente != null && utente.getPasswordHash() != null;
            boolean corretta = encoder.matches(password, conPassword ? utente.getPasswordHash() : hashFinto);
            if (!conPassword || !corretta) {
                throw new CredenzialiNonValideException();
            }
        }

        log.info("Accesso utente {}", utente.getUsername());
        return risposta(utente);
    }

    @Transactional
    public UtenteResponse registra(RegistrazioneRequest richiesta) {
        String nome = pulisci(richiesta.nome());
        String cognome = pulisci(richiesta.cognome());

        String username = UsernameUtils.costruisci(nome, cognome)
                .orElseThrow(() -> new DatiNonValidiException("Nome e cognome devono contenere almeno una lettera o un numero"));

        if (utenti.existsByUsername(username)) {
            throw new UtenteGiaEsistenteException(username);
        }

        Utente utente = Utente.builder()
                .nome(nome)
                .cognome(cognome)
                .username(username)
                .passwordHash(encoder.encode(richiesta.password()))
                .build();

        try {
            utenti.saveAndFlush(utente);
        } catch (DataIntegrityViolationException e) {
            // due registrazioni contemporanee con lo stesso nome
            throw new UtenteGiaEsistenteException(username);
        }

        log.info("Nuovo utente registrato: {}", username);
        return converti(utente);
    }

    @Transactional(readOnly = true)
    public DisponibilitaResponse disponibilita(String nome, String cognome) {
        return UsernameUtils.costruisci(nome, cognome)
                .map(u -> new DisponibilitaResponse(u, !utenti.existsByUsername(u)))
                .orElse(new DisponibilitaResponse("", false));
    }

    private AuthResponse risposta(Utente utente) {
        JwtService.TokenEmesso emesso = jwtService.emetti(utente);
        return new AuthResponse(emesso.token(), emesso.scadenzaMs(), converti(utente));
    }

    private UtenteResponse converti(Utente u) {
        return new UtenteResponse(u.getId(), u.getNome(), u.getCognome(), u.getUsername());
    }

    private String pulisci(String valore) {
        return valore == null ? "" : valore.trim().replaceAll("\\s+", " ");
    }
}