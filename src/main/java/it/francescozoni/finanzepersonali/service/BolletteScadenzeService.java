package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.mapper.BollettaMapper;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Coinquilino;
import it.francescozoni.finanzepersonali.model.ImpostazioneBolletta;
import it.francescozoni.finanzepersonali.model.enums.LivelloScadenza;
import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.repository.BollettaRepository;
import it.francescozoni.finanzepersonali.repository.ImpostazioneBollettaRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class BolletteScadenzeService {

    /** Le bollette previste oltre questo orizzonte non vengono mostrate. */
    private static final int ORIZZONTE_GIORNI = 90;
    private static final DateTimeFormatter GIORNO_MESE = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALY);

    private final BollettaRepository bollette;
    private final ImpostazioneBollettaRepository impostazioni;
    private final UtenteRepository utenti;
    private final UtenteCorrente utenteCorrente;

    public BolletteScadenzeService(BollettaRepository bollette,
                                   ImpostazioneBollettaRepository impostazioni,
                                   UtenteRepository utenti,
                                   UtenteCorrente utenteCorrente) {
        this.bollette = bollette;
        this.impostazioni = impostazioni;
        this.utenti = utenti;
        this.utenteCorrente = utenteCorrente;
    }

    // ---------- scadenze ----------

    public List<ScadenzaResponse> scadenze() {
        Long utenteId = utenteCorrente.id();
        LocalDate oggi = LocalDate.now();
        Map<TipoBolletta, ImpostazioneBolletta> imp = impostazioniDi(utenteId);
        List<Bolletta> tutte = bollette.findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(utenteId);

        List<ScadenzaResponse> risultato = new ArrayList<>();

        for (Bolletta b : tutte) {
            if (b.getStato() == StatoBolletta.DA_PAGARE) {
                risultato.add(daBolletta(b, oggi, imp.get(b.getTipo()).getPreavvisoGiorni()));
            }
        }

        for (TipoBolletta tipo : TipoBolletta.values()) {
            if (tipo != TipoBolletta.WIFI) {
                previsione(tipo, tutte, imp.get(tipo), oggi).ifPresent(risultato::add);
            }
        }

        risultato.sort(Comparator.comparing(ScadenzaResponse::data).thenComparing(ScadenzaResponse::prevista));
        return risultato;
    }

    private ScadenzaResponse daBolletta(Bolletta b, LocalDate oggi, int preavviso) {
        long giorni = ChronoUnit.DAYS.between(oggi, b.getDataScadenza());
        LivelloScadenza livello = giorni < 0 ? LivelloScadenza.SCADUTA
                : giorni <= preavviso ? LivelloScadenza.IN_SCADENZA
                : LivelloScadenza.PROSSIMA;
        Coinquilino pagante = b.getPagante();

        return new ScadenzaResponse(
                b.getId(), b.getTipo(), b.getFornitore(), b.getDataScadenza(),
                b.getImporto(), BollettaMapper.quotaTua(b),
                false, giorni, livello,
                pagante != null ? pagante.getId() : null,
                pagante != null ? pagante.getNome() : "Tu",
                b.getPeriodoDal(), b.getPeriodoAl());
    }

    /**
     * La prossima bolletta attesa: il periodo parte dal giorno dopo la fine dell'ultima,
     * dura quanto la periodicità, e la scadenza segue lo stesso scarto dell'ultima bolletta.
     */
    private Optional<ScadenzaResponse> previsione(TipoBolletta tipo, List<Bolletta> tutte,
                                                  ImpostazioneBolletta imp, LocalDate oggi) {
        List<Bolletta> dellTipo = tutte.stream().filter(b -> b.getTipo() == tipo).toList();
        Optional<Bolletta> ultimaOpt = dellTipo.stream()
                .max(Comparator.comparing(Bolletta::getPeriodoAl).thenComparing(Bolletta::getId));
        if (ultimaOpt.isEmpty()) {
            return Optional.empty();
        }
        Bolletta ultima = ultimaOpt.get();

        LocalDate dal = ultima.getPeriodoAl().plusDays(1);
        LocalDate al = dal.plusMonths(imp.getPeriodicitaMesi()).minusDays(1);
        long scarto = Math.min(Math.max(ChronoUnit.DAYS.between(ultima.getPeriodoAl(), ultima.getDataScadenza()), 20), 90);
        LocalDate scadenza = al.plusDays(scarto);

        long giorni = ChronoUnit.DAYS.between(oggi, scadenza);
        if (giorni > ORIZZONTE_GIORNI) {
            return Optional.empty();
        }

        BigDecimal stima = stima(dellTipo);
        BigDecimal quota = stima == null ? null : stima.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        Coinquilino pagante = ultima.getPagante();

        return Optional.of(new ScadenzaResponse(
                null, tipo, ultima.getFornitore(), scadenza, stima, quota,
                true, giorni,
                giorni < 0 ? LivelloScadenza.MANCANTE : LivelloScadenza.PROSSIMA,
                pagante != null ? pagante.getId() : null,
                pagante != null ? pagante.getNome() : "Tu",
                dal, al));
    }

    /** Media delle ultime tre bollette, conguagli esclusi. */
    private BigDecimal stima(List<Bolletta> lista) {
        List<BigDecimal> importi = lista.stream()
                .filter(b -> b.getNatura() != NaturaBolletta.CONGUAGLIO && b.getImporto().signum() > 0)
                .sorted(Comparator.comparing(Bolletta::getPeriodoAl).reversed())
                .limit(3)
                .map(Bolletta::getImporto)
                .toList();
        if (importi.isEmpty()) {
            return null;
        }
        BigDecimal somma = importi.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return somma.divide(BigDecimal.valueOf(importi.size()), 2, RoundingMode.HALF_UP);
    }

    // ---------- notifiche ----------

    /** Solo quello che riguarda te: se una bolletta la paga un coinquilino non serve avvisarti. */
    public List<NotificaResponse> notifiche() {
        NumberFormat euro = NumberFormat.getCurrencyInstance(Locale.ITALY);
        List<NotificaResponse> risultato = new ArrayList<>();

        for (ScadenzaResponse s : scadenze()) {
            if (s.livello() == LivelloScadenza.PROSSIMA || s.paganteId() != null) {
                continue;
            }

            String nome = etichetta(s.tipo());
            long g = s.giorni();
            String titolo;
            String messaggio;
            String id;

            if (s.livello() == LivelloScadenza.SCADUTA) {
                titolo = "Bolletta " + nome + " scaduta";
                messaggio = euro.format(s.importo()) + " · scaduta " + (g == -1 ? "ieri" : "da " + (-g) + " giorni");
                id = "b" + s.bollettaId() + "-scaduta";
            } else if (s.livello() == LivelloScadenza.IN_SCADENZA) {
                titolo = "Bolletta " + nome + " in scadenza";
                messaggio = euro.format(s.importo()) + " · scade "
                        + (g == 0 ? "oggi" : g == 1 ? "domani" : "tra " + g + " giorni");
                id = "b" + s.bollettaId() + "-scadenza";
            } else {
                titolo = "Bolletta " + nome + " non registrata";
                messaggio = "Era attesa intorno al " + s.data().format(GIORNO_MESE) + ". Se è arrivata, aggiungila.";
                id = "p-" + s.tipo() + "-" + s.data();
            }

            risultato.add(new NotificaResponse(id, s.livello(), s.tipo(), titolo, messaggio, s.bollettaId(), s.data()));
        }

        risultato.sort(Comparator.comparing(NotificaResponse::livello).thenComparing(NotificaResponse::data));
        return risultato;
    }

    private String etichetta(TipoBolletta tipo) {
        return switch (tipo) {
            case LUCE -> "luce";
            case GAS -> "gas";
            case ACQUA -> "acqua";
            case WIFI -> "Wi-Fi";
        };
    }

    // ---------- impostazioni ----------

    public List<ImpostazioneResponse> impostazioni() {
        return impostazioniDi(utenteCorrente.id()).values().stream()
                .map(i -> new ImpostazioneResponse(i.getTipo(), i.getPeriodicitaMesi(), i.getPreavvisoGiorni()))
                .toList();
    }

    public List<ImpostazioneResponse> salvaImpostazioni(ImpostazioniRequest richiesta) {
        Map<TipoBolletta, ImpostazioneBolletta> mappa = impostazioniDi(utenteCorrente.id());

        for (ImpostazioneRequest r : richiesta.impostazioni()) {
            ImpostazioneBolletta i = mappa.get(r.tipo());
            i.setPeriodicitaMesi(r.tipo() == TipoBolletta.WIFI ? 1 : r.periodicitaMesi());
            i.setPreavvisoGiorni(r.preavvisoGiorni());
        }

        impostazioni.saveAll(mappa.values());
        return impostazioni();
    }

    /** Le impostazioni dell'utente; quelle mancanti vengono create con i valori di partenza. */
    private Map<TipoBolletta, ImpostazioneBolletta> impostazioniDi(Long utenteId) {
        Map<TipoBolletta, ImpostazioneBolletta> mappa = new EnumMap<>(TipoBolletta.class);
        impostazioni.findAllByUtenteId(utenteId).forEach(i -> mappa.put(i.getTipo(), i));

        for (TipoBolletta tipo : TipoBolletta.values()) {
            if (!mappa.containsKey(tipo)) {
                mappa.put(tipo, impostazioni.save(ImpostazioneBolletta.builder()
                        .utente(utenti.getReferenceById(utenteId))
                        .tipo(tipo)
                        .periodicitaMesi(tipo == TipoBolletta.WIFI ? 1 : 2)
                        .preavvisoGiorni(5)
                        .build()));
            }
        }
        return mappa;
    }
}