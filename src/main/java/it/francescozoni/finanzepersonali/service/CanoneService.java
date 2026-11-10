package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Canone;
import it.francescozoni.finanzepersonali.model.TariffaCanone;
import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.OrigineBolletta;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.repository.BollettaRepository;
import it.francescozoni.finanzepersonali.repository.CanoneRepository;
import it.francescozoni.finanzepersonali.repository.TariffaCanoneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Genera le bollette dei canoni fissi (il Wi-Fi), una per ogni mese. */
@Slf4j
@Service
@Transactional
public class CanoneService {

    private final CanoneRepository canoni;
    private final TariffaCanoneRepository tariffe;
    private final BollettaRepository bollette;

    public CanoneService(CanoneRepository canoni, TariffaCanoneRepository tariffe, BollettaRepository bollette) {
        this.canoni = canoni;
        this.tariffe = tariffe;
        this.bollette = bollette;
    }

    /**
     * Crea le bollette mancanti, dal primo mese della tariffa fino a quello corrente.
     * I mesi passati risultano pagati, quello corrente da pagare. È sicuro chiamarla più volte.
     */
    public void generaScadenze(Long utenteId) {
        YearMonth corrente = YearMonth.now();

        for (Canone canone : canoni.findAllByUtenteIdAndAttivoTrueAndDeletedFalse(utenteId)) {
            List<TariffaCanone> storico = tariffe.findAllByCanoneIdOrderByValidaDalAsc(canone.getId());
            if (storico.isEmpty()) {
                continue;
            }

            Set<LocalDate> esistenti = new HashSet<>(bollette.findPeriodiDalByCanoneId(canone.getId()));
            int creati = 0;

            for (YearMonth mese = YearMonth.from(storico.getFirst().getValidaDal());
                 !mese.isAfter(corrente);
                 mese = mese.plusMonths(1)) {

                if (esistenti.contains(mese.atDay(1))) {
                    continue;
                }

                LocalDate scadenza = mese.atDay(Math.min(canone.getGiornoScadenza(), mese.lengthOfMonth()));
                boolean passato = mese.isBefore(corrente);

                bollette.save(Bolletta.builder()
                        .utente(canone.getUtente())
                        .tipo(canone.getTipo())
                        .fornitore(canone.getFornitore())
                        .periodoDal(mese.atDay(1))
                        .periodoAl(mese.atEndOfMonth())
                        .dataEmissione(scadenza)
                        .dataScadenza(scadenza)
                        .importo(tariffaInVigore(storico, scadenza).getImporto())
                        .natura(NaturaBolletta.EFFETTIVA)
                        .stato(passato ? StatoBolletta.PAGATA : StatoBolletta.DA_PAGARE)
                        .dataPagamento(passato ? scadenza : null)
                        .pagante(canone.getPagante())
                        .origine(OrigineBolletta.CANONE)
                        .canone(canone)
                        .build());
                creati++;
            }

            if (creati > 0) {
                log.info("Canone {} {}: generate {} bollette", canone.getTipo(), canone.getFornitore(), creati);
            }
        }
    }

    private TariffaCanone tariffaInVigore(List<TariffaCanone> storico, LocalDate data) {
        TariffaCanone risultato = storico.getFirst();
        for (TariffaCanone t : storico) {
            if (!t.getValidaDal().isAfter(data)) {
                risultato = t;
            }
        }
        return risultato;
    }
}