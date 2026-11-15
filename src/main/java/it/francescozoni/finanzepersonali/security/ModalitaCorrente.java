package it.francescozoni.finanzepersonali.security;

import it.francescozoni.finanzepersonali.exception.ModalitaNonConsentitaException;
import it.francescozoni.finanzepersonali.model.Utente;
import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import org.springframework.stereotype.Component;

@Component
public class ModalitaCorrente {

    private final UtenteRepository utenti;
    private final UtenteCorrente utenteCorrente;

    public ModalitaCorrente(UtenteRepository utenti, UtenteCorrente utenteCorrente) {
        this.utenti = utenti;
        this.utenteCorrente = utenteCorrente;
    }

    /** La modalità dell'utente della richiesta, oppure null se non l'ha ancora scelta. */
    public ModalitaBollette get() {
        return utenti.findById(utenteCorrente.id()).map(Utente::getModalitaBollette).orElse(null);
    }

    public void richiedi(ModalitaBollette modalita) {
        if (get() != modalita) {
            throw new ModalitaNonConsentitaException();
        }
    }
}