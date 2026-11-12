package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.CanoneNotFoundException;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Canone;
import it.francescozoni.finanzepersonali.model.Coinquilino;
import it.francescozoni.finanzepersonali.model.TariffaCanone;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.repository.BollettaRepository;
import it.francescozoni.finanzepersonali.repository.CanoneRepository;
import it.francescozoni.finanzepersonali.repository.CoinquilinoRepository;
import it.francescozoni.finanzepersonali.repository.TariffaCanoneRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Gestione del canone (il Wi-Fi): dettagli e storico dei prezzi. */
@Slf4j
@Service
@Transactional
public class CanoneGestioneService {

    private final CanoneRepository canoni;
    private final TariffaCanoneRepository tariffe;
    private final BollettaRepository bollette;
    private final CoinquilinoRepository coinquilini;
    private final UtenteCorrente utenteCorrente;

    public CanoneGestioneService(CanoneRepository canoni,
                                 TariffaCanoneRepository tariffe,
                                 BollettaRepository bollette,
                                 CoinquilinoRepository coinquilini,
                                 UtenteCorrente utenteCorrente) {
        this.canoni = canoni;
        this.tariffe = tariffe;
        this.bollette = bollette;
        this.coinquilini = coinquilini;
        this.utenteCorrente = utenteCorrente;
    }

    @Transactional(readOnly = true)
    public List<CanoneResponse> elenco() {
        return canoni.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utenteCorrente.id())
                .stream()
                .map(this::risposta)
                .toList();
    }

    public CanoneResponse modifica(Long id, CanoneRequest richiesta) {
        Canone canone = trovaMio(id);

        Coinquilino pagante = null;
        if (richiesta.paganteId() != null) {
            pagante = coinquilini.findByIdAndUtenteIdAndDeletedFalse(richiesta.paganteId(), utenteCorrente.id())
                    .orElseThrow(() -> new DatiNonValidiException("Il pagante scelto non esiste"));
        }

        canone.setFornitore(richiesta.fornitore().trim());
        canone.setGiornoScadenza(richiesta.giornoScadenza());
        canone.setPagante(pagante);
        canone.setPagamentoAutomatico(richiesta.pagamentoAutomatico());
        canone.setAttivo(richiesta.attivo());
        canoni.save(canone);

        // le bollette ancora da pagare seguono le nuove impostazioni; quelle pagate restano com'erano
        for (Bolletta b : bollette.findAllByCanoneIdAndDeletedFalseAndStato(id, StatoBolletta.DA_PAGARE)) {
            YearMonth mese = YearMonth.from(b.getPeriodoDal());
            LocalDate scadenza = mese.atDay(Math.min(richiesta.giornoScadenza(), mese.lengthOfMonth()));
            b.setFornitore(canone.getFornitore());
            b.setPagante(pagante);
            b.setDataScadenza(scadenza);
            b.setDataEmissione(scadenza);
        }

        log.info("Canone {} aggiornato", id);
        return risposta(canone);
    }

    /** Nuovo prezzo dal mese indicato in poi. Se per quel mese esiste già una tariffa, ne cambia l'importo. */
    public TariffaEsito aggiungiTariffa(Long canoneId, TariffaRequest richiesta) {
        Canone canone = trovaMio(canoneId);
        YearMonth mese = YearMonth.from(richiesta.validaDal());
        LocalDate dal = mese.atDay(1);

        List<TariffaCanone> storico = tariffe.findAllByCanoneIdOrderByValidaDalAsc(canoneId);
        if (!storico.isEmpty()) {
            YearMonth inizio = YearMonth.from(storico.get(0).getValidaDal());
            if (mese.isBefore(inizio)) {
                throw new DatiNonValidiException("Il nuovo prezzo non può partire prima dell'inizio del canone ("
                        + inizio.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALY)) + ")");
            }
        }

        BigDecimal importo = richiesta.importo().setScale(2, RoundingMode.HALF_UP);
        Optional<TariffaCanone> stessa = storico.stream()
                .filter(t -> YearMonth.from(t.getValidaDal()).equals(mese))
                .findFirst();

        if (stessa.isPresent()) {
            stessa.get().setImporto(importo);
        } else {
            tariffe.save(TariffaCanone.builder().canone(canone).importo(importo).validaDal(dal).build());
        }

        log.info("Canone {}: prezzo {} dal {}", canoneId, importo, dal);
        return esito(canone, dal);
    }

    public TariffaEsito eliminaTariffa(Long canoneId, Long tariffaId) {
        Canone canone = trovaMio(canoneId);
        List<TariffaCanone> storico = tariffe.findAllByCanoneIdOrderByValidaDalAsc(canoneId);

        TariffaCanone tariffa = storico.stream()
                .filter(t -> t.getId().equals(tariffaId))
                .findFirst()
                .orElseThrow(() -> new DatiNonValidiException("Tariffa non trovata"));

        if (tariffa.getId().equals(storico.get(0).getId())) {
            throw new DatiNonValidiException("La prima tariffa non si può eliminare: puoi cambiarne l'importo");
        }

        LocalDate dal = tariffa.getValidaDal();
        tariffe.delete(tariffa);

        log.info("Canone {}: eliminata la tariffa dal {}", canoneId, dal);
        return esito(canone, dal);
    }

    // ---------- appoggio ----------

    private Canone trovaMio(Long id) {
        return canoni.findByIdAndUtenteIdAndDeletedFalse(id, utenteCorrente.id())
                .orElseThrow(() -> new CanoneNotFoundException(id));
    }

    /** Riallinea le bollette ancora da pagare dal mese indicato in poi e prepara la risposta. */
    private TariffaEsito esito(Canone canone, LocalDate dal) {
        List<TariffaCanone> storico = tariffe.findAllByCanoneIdOrderByValidaDalAsc(canone.getId());
        int aggiornate = 0;
        int nonModificate = 0;

        for (Bolletta b : bollette.findAllByCanoneIdAndDeletedFalseAndPeriodoDalGreaterThanEqual(canone.getId(), dal)) {
            BigDecimal dovuto = CanoneService.tariffaInVigore(storico, b.getDataScadenza()).getImporto();
            if (dovuto.compareTo(b.getImporto()) == 0) {
                continue;
            }
            if (b.getStato() == StatoBolletta.DA_PAGARE) {
                b.setImporto(dovuto);
                aggiornate++;
            } else {
                nonModificate++;
            }
        }

        return new TariffaEsito(risposta(canone), aggiornate, nonModificate);
    }

    private CanoneResponse risposta(Canone c) {
        List<TariffaCanone> storico = tariffe.findAllByCanoneIdOrderByValidaDalAsc(c.getId());
        TariffaCanone inVigore = storico.isEmpty() ? null : CanoneService.tariffaInVigore(storico, LocalDate.now());
        Coinquilino pagante = c.getPagante();

        return new CanoneResponse(
                c.getId(),
                c.getTipo(),
                c.getFornitore(),
                c.getGiornoScadenza(),
                pagante != null ? pagante.getId() : null,
                pagante != null ? pagante.getNome() : "Tu",
                Boolean.TRUE.equals(c.getPagamentoAutomatico()),
                c.isAttivo(),
                inVigore != null ? inVigore.getImporto() : BigDecimal.ZERO,
                storico.stream()
                        .map(t -> new TariffaResponse(t.getId(), t.getImporto(), t.getValidaDal(),
                                inVigore != null && t.getId().equals(inVigore.getId())))
                        .toList());
    }
}