package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.UtenteResponse;
import it.francescozoni.finanzepersonali.exception.CredenzialiNonValideException;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.model.Utente;
import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class ProfiloService {

    private final UtenteRepository utenti;
    private final UtenteCorrente utenteCorrente;
    private final QuoteService quoteService;

    public ProfiloService(UtenteRepository utenti, UtenteCorrente utenteCorrente, QuoteService quoteService) {
        this.utenti = utenti;
        this.utenteCorrente = utenteCorrente;
        this.quoteService = quoteService;
    }

    @Transactional(readOnly = true)
    public UtenteResponse profilo() {
        return risposta(corrente());
    }

    public UtenteResponse scegliModalita(ModalitaBollette modalita) {
        Utente utente = corrente();
        if (utente.getModalitaBollette() != null) {
            throw new DatiNonValidiException("La modalità è già stata scelta e non si può cambiare");
        }

        utente.setModalitaBollette(modalita);
        if (modalita == ModalitaBollette.IRENE) {
            quoteService.creaPersone(utente);
        }
        utenti.save(utente);

        log.info("L'utente {} ha scelto la modalità {}", utente.getUsername(), modalita);
        return risposta(utente);
    }

    private Utente corrente() {
        return utenti.findById(utenteCorrente.id()).orElseThrow(CredenzialiNonValideException::new);
    }

    private UtenteResponse risposta(Utente u) {
        return new UtenteResponse(u.getId(), u.getNome(), u.getCognome(), u.getUsername(), u.getModalitaBollette());
    }
}