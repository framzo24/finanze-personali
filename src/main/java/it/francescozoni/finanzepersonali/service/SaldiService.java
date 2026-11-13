package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.exception.RimborsoNotFoundException;
import it.francescozoni.finanzepersonali.model.*;
import it.francescozoni.finanzepersonali.model.enums.DirezioneRimborso;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.repository.*;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Il saldo con un coinquilino è positivo se ti deve dei soldi:
 * + un terzo di ogni bolletta che hai pagato tu,
 * − un terzo di ogni bolletta che ha pagato lui,
 * − i rimborsi che ti ha dato, + quelli che gli hai dato tu.
 */
@Slf4j
@Service
@Transactional
public class SaldiService {

    private static final DateTimeFormatter GG_MM = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter GG_MM_AAAA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private record Riga(LocalDate data, String tipo, Long id, String titolo, String dettaglio,
                        BigDecimal effetto, TipoBolletta tipoBolletta, Long movimentoId) {
    }

    private final BollettaRepository bollette;
    private final RimborsoRepository rimborsi;
    private final CoinquilinoRepository coinquilini;
    private final ConfigurazioneSaldiRepository configurazioni;
    private final UtenteRepository utenti;
    private final UtenteCorrente utenteCorrente;
    private final BolletteMovimentiService movimentiBollette;

    public SaldiService(BollettaRepository bollette,
                        RimborsoRepository rimborsi,
                        CoinquilinoRepository coinquilini,
                        ConfigurazioneSaldiRepository configurazioni,
                        UtenteRepository utenti,
                        UtenteCorrente utenteCorrente,
                        BolletteMovimentiService movimentiBollette) {
        this.bollette = bollette;
        this.rimborsi = rimborsi;
        this.coinquilini = coinquilini;
        this.configurazioni = configurazioni;
        this.utenti = utenti;
        this.utenteCorrente = utenteCorrente;
        this.movimentiBollette = movimentiBollette;
    }

    // ---------- saldi ----------

    @Transactional(readOnly = true)
    public SaldiResponse saldi() {
        Long utenteId = utenteCorrente.id();
        Optional<ConfigurazioneSaldi> cfg = configurazioni.findByUtenteId(utenteId);
        LocalDate dal = cfg.map(ConfigurazioneSaldi::getSaldiDal).orElse(null);
        boolean configurato = cfg.map(ConfigurazioneSaldi::isConfigurato).orElse(false);

        List<Bolletta> pagate = bollettePagate(utenteId, dal);
        List<Rimborso> rimborsiConteggiati = rimborsiDi(utenteId, dal);

        List<SaldoCoinquilino> righe = coinquilini.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utenteId).stream()
                .map(c -> saldo(c, pagate, rimborsiConteggiati))
                .toList();

        BigDecimal totale = righe.stream().map(SaldoCoinquilino::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SaldiResponse(configurato, dal, righe, totale);
    }

    private SaldoCoinquilino saldo(Coinquilino c, List<Bolletta> pagate, List<Rimborso> rimborsiConteggiati) {
        BigDecimal daBollette = pagate.stream()
                .map(b -> effettoBolletta(b, c.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal daRimborsi = rimborsiConteggiati.stream()
                .filter(r -> r.getCoinquilino().getId().equals(c.getId()))
                .map(SaldiService::effettoRimborso)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SaldoCoinquilino(c.getId(), c.getNome(), daBollette.add(daRimborsi), daBollette, daRimborsi);
    }

    /** Il dettaglio di un coinquilino, dalla voce più recente alla più vecchia, con il saldo progressivo. */
    @Transactional(readOnly = true)
    public List<VoceSaldo> voci(Long coinquilinoId) {
        Long utenteId = utenteCorrente.id();
        Coinquilino persona = trovaCoinquilino(coinquilinoId, utenteId);
        LocalDate dal = configurazioni.findByUtenteId(utenteId).map(ConfigurazioneSaldi::getSaldiDal).orElse(null);

        List<Riga> righe = new ArrayList<>();

        for (Bolletta b : bollettePagate(utenteId, dal)) {
            BigDecimal effetto = effettoBolletta(b, persona.getId());
            if (effetto.signum() == 0) {
                continue;
            }
            String chiPaga = b.getPagante() == null ? "Pagata da te" : "Pagata da " + persona.getNome();
            righe.add(new Riga(
                    dataPagamento(b), "BOLLETTA", b.getId(),
                    b.getTipo().getEtichetta() + " · " + b.getFornitore(),
                    chiPaga + " · periodo " + b.getPeriodoDal().format(GG_MM) + " – " + b.getPeriodoAl().format(GG_MM_AAAA),
                    effetto, b.getTipo(),
                    b.getMovimento() != null ? b.getMovimento().getId() : null));
        }

        for (Rimborso r : rimborsiDi(utenteId, dal)) {
            if (!r.getCoinquilino().getId().equals(persona.getId())) {
                continue;
            }
            boolean ricevuto = r.getDirezione() == DirezioneRimborso.RICEVUTO;
            String dettaglio = r.getNota() != null ? r.getNota()
                    : ricevuto ? persona.getNome() + " ti ha restituito dei soldi"
                    : "Hai restituito dei soldi a " + persona.getNome();
            righe.add(new Riga(
                    r.getData(), "RIMBORSO", r.getId(),
                    ricevuto ? "Rimborso ricevuto" : "Rimborso dato",
                    dettaglio, effettoRimborso(r), null,
                    r.getMovimento() != null ? r.getMovimento().getId() : null));
        }

        righe.sort(Comparator.comparing(Riga::data).thenComparing(Riga::tipo).thenComparing(Riga::id));

        BigDecimal progressivo = BigDecimal.ZERO;
        List<VoceSaldo> risultato = new ArrayList<>();
        for (Riga r : righe) {
            progressivo = progressivo.add(r.effetto());
            risultato.add(new VoceSaldo(r.tipo(), r.id(), r.data(), r.titolo(), r.dettaglio(),
                    r.effetto(), progressivo, r.tipoBolletta(), r.movimentoId()));
        }
        Collections.reverse(risultato);
        return risultato;
    }

    // ---------- da quando contare ----------

    public ConfigurazioneSaldiResponse salvaConfigurazione(ConfigurazioneSaldiRequest richiesta) {
        Long utenteId = utenteCorrente.id();
        ConfigurazioneSaldi cfg = configurazioni.findByUtenteId(utenteId)
                .orElseGet(() -> ConfigurazioneSaldi.builder().utente(utenti.getReferenceById(utenteId)).build());

        cfg.setSaldiDal(richiesta.saldiDal());
        cfg.setConfigurato(true);
        configurazioni.save(cfg);

        log.info("Saldi conteggiati dal {}", richiesta.saldiDal() != null ? richiesta.saldiDal() : "primo giorno");
        return new ConfigurazioneSaldiResponse(true, cfg.getSaldiDal());
    }

    // ---------- rimborsi ----------

    public RimborsoResponse creaRimborso(RimborsoRequest richiesta) {
        Long utenteId = utenteCorrente.id();
        Coinquilino persona = trovaCoinquilino(richiesta.coinquilinoId(), utenteId);

        Rimborso rimborso = Rimborso.builder()
                .utente(utenti.getReferenceById(utenteId))
                .coinquilino(persona)
                .direzione(richiesta.direzione())
                .importo(richiesta.importo().setScale(2, RoundingMode.HALF_UP))
                .data(richiesta.data())
                .nota(richiesta.nota() == null || richiesta.nota().isBlank() ? null : richiesta.nota().trim())
                .build();

        // i soldi che ricevi non sono un'entrata: la spesa nasce solo se li hai dati tu
        if (richiesta.direzione() == DirezioneRimborso.DATO && Boolean.TRUE.equals(richiesta.registraMovimento())) {
            rimborso.setMovimento(movimentiBollette.creaPerRimborso(rimborso, richiesta.tipo()));
        }

        rimborsi.save(rimborso);
        log.info("Rimborso {} di {} con {}", rimborso.getDirezione(), rimborso.getImporto(), persona.getNome());
        return risposta(rimborso);
    }

    public void eliminaRimborso(Long id) {
        Rimborso rimborso = rimborsi.findByIdAndUtenteIdAndDeletedFalse(id, utenteCorrente.id())
                .orElseThrow(() -> new RimborsoNotFoundException(id));

        Movimento collegato = rimborso.getMovimento();
        rimborso.setMovimento(null);
        rimborso.setDeleted(true);
        rimborsi.save(rimborso);

        if (collegato != null) {
            movimentiBollette.elimina(collegato);
        }
        log.info("Rimborso eliminato id={}", id);
    }

    // ---------- appoggio ----------

    private Coinquilino trovaCoinquilino(Long id, Long utenteId) {
        return coinquilini.findByIdAndUtenteIdAndDeletedFalse(id, utenteId)
                .orElseThrow(() -> new DatiNonValidiException("Il coinquilino scelto non esiste"));
    }

    private List<Bolletta> bollettePagate(Long utenteId, LocalDate dal) {
        return bollette.findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(utenteId).stream()
                .filter(b -> b.getStato() == StatoBolletta.PAGATA)
                .filter(b -> dal == null || !dataPagamento(b).isBefore(dal))
                .toList();
    }

    private List<Rimborso> rimborsiDi(Long utenteId, LocalDate dal) {
        return rimborsi.findAllByUtenteIdAndDeletedFalseOrderByDataAscIdAsc(utenteId).stream()
                .filter(r -> dal == null || !r.getData().isBefore(dal))
                .toList();
    }

    private static LocalDate dataPagamento(Bolletta b) {
        return b.getDataPagamento() != null ? b.getDataPagamento() : b.getDataScadenza();
    }

    private static BigDecimal terzo(Bolletta b) {
        return b.getImporto().divide(BigDecimal.valueOf(3), 2, RoundingMode.DOWN);
    }

    /** + se l'hai pagata tu, − se l'ha pagata lui, 0 se l'ha pagata l'altro coinquilino. */
    private static BigDecimal effettoBolletta(Bolletta b, Long coinquilinoId) {
        Coinquilino pagante = b.getPagante();
        if (pagante == null) {
            return terzo(b);
        }
        return pagante.getId().equals(coinquilinoId) ? terzo(b).negate() : BigDecimal.ZERO;
    }

    private static BigDecimal effettoRimborso(Rimborso r) {
        return r.getDirezione() == DirezioneRimborso.RICEVUTO ? r.getImporto().negate() : r.getImporto();
    }

    private RimborsoResponse risposta(Rimborso r) {
        return new RimborsoResponse(
                r.getId(),
                r.getCoinquilino().getId(),
                r.getCoinquilino().getNome(),
                r.getDirezione(),
                r.getImporto(),
                r.getData(),
                r.getNota(),
                r.getMovimento() != null ? r.getMovimento().getId() : null);
    }
}