package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.DiarioRequest;
import it.francescozoni.finanzepersonali.dto.DiarioResponse;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.model.VoceDiario;
import it.francescozoni.finanzepersonali.repository.DiarioRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class DiarioService {

    private static final int MAX_LIMITE = 100;
    private static final long MAX_GIORNI_PERIODO = 366;

    private final DiarioRepository repository;
    private final UtenteRepository utenti;
    private final UtenteCorrente utenteCorrente;

    public DiarioService(DiarioRepository repository, UtenteRepository utenti, UtenteCorrente utenteCorrente) {
        this.repository = repository;
        this.utenti = utenti;
        this.utenteCorrente = utenteCorrente;
    }

    @Transactional(readOnly = true)
    public DiarioResponse giorno(LocalDate giorno) {
        return repository.findByUtenteIdAndGiorno(utenteCorrente.id(), giorno)
                .map(this::risposta)
                .orElse(new DiarioResponse(giorno, "", null));
    }

    /**
     * Un solo blocco per giorno: se esiste lo aggiorna, altrimenti lo crea.
     * Un testo vuoto elimina il blocco, perché un giorno può anche non averlo.
     */
    public DiarioResponse salva(LocalDate giorno, DiarioRequest richiesta) {
        if (giorno.isAfter(LocalDate.now())) {
            throw new DatiNonValidiException("Non puoi scrivere in un giorno che non è ancora arrivato");
        }

        Long utenteId = utenteCorrente.id();
        String testo = normalizza(richiesta.testo());
        Optional<VoceDiario> esistente = repository.findByUtenteIdAndGiorno(utenteId, giorno);

        if (testo.isEmpty()) {
            esistente.ifPresent(repository::delete);
            log.info("Diario: svuotato il giorno {}", giorno);
            return new DiarioResponse(giorno, "", null);
        }

        VoceDiario voce = esistente.orElseGet(() -> VoceDiario.builder()
                .utente(utenti.getReferenceById(utenteId))
                .giorno(giorno)
                .build());
        voce.setTesto(testo);

        log.info("Diario: salvato il giorno {}", giorno);
        return risposta(repository.saveAndFlush(voce));
    }

    /** I giorni del mese che hanno un pensiero, per segnarli nel calendario. */
    @Transactional(readOnly = true)
    public List<LocalDate> giorniConTesto(int anno, int mese) {
        YearMonth ym;
        try {
            ym = YearMonth.of(anno, mese);
        } catch (DateTimeException e) {
            throw new DatiNonValidiException("Mese non valido");
        }
        return repository.findGiorni(utenteCorrente.id(), ym.atDay(1), ym.atEndOfMonth());
    }

    /** Gli ultimi pensieri scritti, dal più recente. */
    @Transactional(readOnly = true)
    public List<DiarioResponse> recenti(int limite) {
        int n = Math.min(Math.max(limite, 1), MAX_LIMITE);
        return repository.findByUtenteIdOrderByGiornoDesc(utenteCorrente.id(), PageRequest.of(0, n))
                .stream()
                .map(this::risposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiarioResponse> periodo(LocalDate da, LocalDate a) {
        if (a.isBefore(da)) {
            throw new DatiNonValidiException("La fine del periodo non può precedere l'inizio");
        }
        if (ChronoUnit.DAYS.between(da, a) > MAX_GIORNI_PERIODO) {
            throw new DatiNonValidiException("Il periodo può durare al massimo un anno");
        }
        return repository.findAllByUtenteIdAndGiornoBetweenOrderByGiornoDesc(utenteCorrente.id(), da, a)
                .stream()
                .map(this::risposta)
                .toList();
    }

    private DiarioResponse risposta(VoceDiario v) {
        return new DiarioResponse(v.getGiorno(), v.getTesto(), v.getUpdatedAt());
    }

    private String normalizza(String testo) {
        return testo == null ? "" : testo.replace("\r\n", "\n").replace('\r', '\n').strip();
    }
}