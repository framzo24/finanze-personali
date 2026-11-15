package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.QuoteDto.*;
import it.francescozoni.finanzepersonali.exception.BollettaNotFoundException;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.exception.VersamentoNotFoundException;
import it.francescozoni.finanzepersonali.model.*;
import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.repository.BollettaRepository;
import it.francescozoni.finanzepersonali.repository.CoinquilinoRepository;
import it.francescozoni.finanzepersonali.repository.ConfigurazioneSaldiRepository;
import it.francescozoni.finanzepersonali.repository.VersamentoRepository;
import it.francescozoni.finanzepersonali.security.ModalitaCorrente;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.util.QuoteIrene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Modalità Irene: le bollette le paga il proprietario e ognuno gli invia la sua quota (un terzo). */
@Slf4j
@Service
@Transactional
public class QuoteService {

    public static final String PROPRIETARIO_PREDEFINITO = "Proprietario";
    private static final String NOME_TU = "Irene";
    /** Le altre due persone, nell'ordine in cui compaiono prima e dopo di te. */
    private static final List<String> ALTRI = List.of("Victoria", "Anita");

    private record Persona(Long coinquilinoId, String nome, int indice, boolean tu) {
    }

    private final BollettaRepository bollette;
    private final VersamentoRepository versamenti;
    private final CoinquilinoRepository coinquilini;
    private final ConfigurazioneSaldiRepository configurazioni;
    private final UtenteCorrente utenteCorrente;
    private final ModalitaCorrente modalita;
    private final BolletteMovimentiService movimentiBollette;

    public QuoteService(BollettaRepository bollette,
                        VersamentoRepository versamenti,
                        CoinquilinoRepository coinquilini,
                        ConfigurazioneSaldiRepository configurazioni,
                        UtenteCorrente utenteCorrente,
                        ModalitaCorrente modalita,
                        BolletteMovimentiService movimentiBollette) {
        this.bollette = bollette;
        this.versamenti = versamenti;
        this.coinquilini = coinquilini;
        this.configurazioni = configurazioni;
        this.utenteCorrente = utenteCorrente;
        this.modalita = modalita;
        this.movimentiBollette = movimentiBollette;
    }

    // ---------- persone ----------

    /** Crea Victoria e Anita per un account che ha scelto la modalità Irene. */
    public void creaPersone(Utente utente) {
        List<Coinquilino> esistenti = coinquilini.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utente.getId());
        for (String nome : ALTRI) {
            if (cerca(esistenti, nome) == null) {
                coinquilini.save(Coinquilino.builder().utente(utente).nome(nome).build());
            }
        }
    }

    private List<Persona> persone(Long utenteId) {
        List<Coinquilino> tutti = coinquilini.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utenteId);
        Coinquilino victoria = cerca(tutti, ALTRI.get(0));
        Coinquilino anita = cerca(tutti, ALTRI.get(1));
        if (victoria == null || anita == null) {
            throw new DatiNonValidiException("Le persone della casa non sono configurate");
        }
        return List.of(
                new Persona(victoria.getId(), victoria.getNome(), 0, false),
                new Persona(null, NOME_TU, QuoteIrene.INDICE_TU, true),
                new Persona(anita.getId(), anita.getNome(), 2, false));
    }

    private Coinquilino cerca(List<Coinquilino> lista, String nome) {
        return lista.stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst().orElse(null);
    }

    // ---------- consultazione ----------

    @Transactional(readOnly = true)
    public ProspettoResponse prospetto(boolean soloAperte) {
        modalita.richiedi(ModalitaBollette.IRENE);
        Long utenteId = utenteCorrente.id();

        Optional<ConfigurazioneSaldi> cfg = configurazioni.findByUtenteId(utenteId);
        LocalDate dal = cfg.map(ConfigurazioneSaldi::getSaldiDal).orElse(null);
        List<Persona> persone = persone(utenteId);

        List<Bolletta> lista = bollette.findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(utenteId).stream()
                .filter(b -> inConteggio(b, dal))
                .filter(b -> !soloAperte || b.getStato() == StatoBolletta.DA_PAGARE)
                .toList();
        List<BollettaQuote> quote = costruisci(lista, persone);

        List<RigaProspetto> utenze = new ArrayList<>();
        for (TipoBolletta tipo : TipoBolletta.values()) {
            List<BollettaQuote> dellTipo = quote.stream().filter(q -> q.tipo() == tipo).toList();
            if (!dellTipo.isEmpty()) {
                utenze.add(riga(tipo, dellTipo, persone.size()));
            }
        }

        return new ProspettoResponse(
                cfg.map(ConfigurazioneSaldi::isConfigurato).orElse(false),
                dal,
                proprietario(),
                persone.stream().map(p -> new PersonaProspetto(p.coinquilinoId(), p.nome(), p.tu())).toList(),
                utenze,
                riga(null, quote, persone.size()));
    }

    /** Le bollette non ancora saldate, dalla più vicina alla scadenza. */
    @Transactional(readOnly = true)
    public List<BollettaQuote> aperte() {
        modalita.richiedi(ModalitaBollette.IRENE);
        Long utenteId = utenteCorrente.id();
        LocalDate dal = contaDal(utenteId);

        List<Bolletta> lista = bollette.findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(utenteId).stream()
                .filter(b -> b.getStato() == StatoBolletta.DA_PAGARE && inConteggio(b, dal))
                .toList();
        return costruisci(lista, persone(utenteId));
    }

    @Transactional(readOnly = true)
    public List<BollettaQuote> perBollette(List<Long> ids) {
        modalita.richiedi(ModalitaBollette.IRENE);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        if (ids.size() > 100) {
            throw new DatiNonValidiException("Troppe bollette richieste insieme");
        }
        Long utenteId = utenteCorrente.id();
        return costruisci(bollette.findAllByUtenteIdAndDeletedFalseAndIdIn(utenteId, ids), persone(utenteId));
    }

    @Transactional(readOnly = true)
    public String proprietario() {
        return configurazioni.findByUtenteId(utenteCorrente.id())
                .map(ConfigurazioneSaldi::getProprietario)
                .filter(nome -> !nome.isBlank())
                .orElse(PROPRIETARIO_PREDEFINITO);
    }

    // ---------- invii ----------

    public List<VersamentoResponse> registra(VersamentiRequest richiesta) {
        modalita.richiedi(ModalitaBollette.IRENE);
        Long utenteId = utenteCorrente.id();

        if (richiesta.data().isAfter(LocalDate.now())) {
            throw new DatiNonValidiException("La data non può essere nel futuro");
        }

        Persona persona = persone(utenteId).stream()
                .filter(p -> Objects.equals(p.coinquilinoId(), richiesta.coinquilinoId()))
                .findFirst()
                .orElseThrow(() -> new DatiNonValidiException("La persona scelta non esiste"));

        Coinquilino coinquilino = persona.tu() ? null : coinquilini.getReferenceById(persona.coinquilinoId());
        boolean registra = persona.tu() && Boolean.TRUE.equals(richiesta.registraMovimento());
        String nota = richiesta.nota() == null || richiesta.nota().isBlank() ? null : richiesta.nota().trim();

        Set<Long> viste = new HashSet<>();
        List<VersamentoResponse> risultato = new ArrayList<>();

        for (VoceVersamento voce : richiesta.voci()) {
            if (!viste.add(voce.bollettaId())) {
                throw new DatiNonValidiException("Una bolletta compare più di una volta");
            }

            Bolletta b = bollette.findByIdAndUtenteIdAndDeletedFalse(voce.bollettaId(), utenteId)
                    .orElseThrow(() -> new BollettaNotFoundException(voce.bollettaId()));
            if (Boolean.TRUE.equals(b.getStorica())) {
                throw new DatiNonValidiException("Una bolletta dello storico è già saldata");
            }

            Versamento v = versamenti.save(Versamento.builder()
                    .utente(b.getUtente())
                    .bolletta(b)
                    .coinquilino(coinquilino)
                    .importo(voce.importo().setScale(2, RoundingMode.HALF_UP))
                    .data(richiesta.data())
                    .nota(nota)
                    .build());

            if (registra) {
                v.setMovimento(movimentiBollette.creaPerVersamento(v));
            }

            riallinea(b);
            bollette.save(b);
            risultato.add(risposta(v));
        }

        log.info("Registrati {} invii di {}", risultato.size(), persona.nome());
        return risultato;
    }

    public void elimina(Long id) {
        modalita.richiedi(ModalitaBollette.IRENE);

        Versamento v = versamenti.findByIdAndUtenteIdAndDeletedFalse(id, utenteCorrente.id())
                .orElseThrow(() -> new VersamentoNotFoundException(id));
        Bolletta b = v.getBolletta();
        Movimento collegato = v.getMovimento();

        v.setMovimento(null);
        v.setDeleted(true);
        versamenti.saveAndFlush(v);

        if (collegato != null) {
            movimentiBollette.elimina(collegato);
        }
        riallinea(b);
        bollette.save(b);
        log.info("Eliminato l'invio id={}", id);
    }

    // ---------- usati dalle bollette ----------

    /** Una bolletta è saldata quando nessuno ha più nulla da inviare. Non si imposta a mano. */
    public void riallinea(Bolletta b) {
        List<Versamento> vs = b.getId() == null ? List.of() : versamenti.findAllByBollettaIdAndDeletedFalse(b.getId());
        boolean saldata = quote(b, persone(utenteCorrente.id()), vs).stream()
                .allMatch(q -> q.mancante().signum() <= 0);

        LocalDate ultimo = vs.stream().map(Versamento::getData).max(LocalDate::compareTo).orElse(null);

        b.setStato(saldata ? StatoBolletta.PAGATA : StatoBolletta.DA_PAGARE);
        b.setDataPagamento(!saldata ? null
                : Boolean.TRUE.equals(b.getStorica()) || ultimo == null ? b.getDataScadenza() : ultimo);
    }

    /** Elimina gli invii di una bolletta che sta per essere eliminata, con le spese collegate. */
    public void eliminaVersamenti(Bolletta b) {
        for (Versamento v : versamenti.findAllByBollettaIdAndDeletedFalse(b.getId())) {
            Movimento collegato = v.getMovimento();
            v.setMovimento(null);
            v.setDeleted(true);
            versamenti.save(v);
            if (collegato != null) {
                movimentiBollette.elimina(collegato);
            }
        }
    }

    // ---------- calcolo ----------

    private List<BollettaQuote> costruisci(List<Bolletta> lista, List<Persona> persone) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Versamento>> perBolletta = versamenti
                .findAllByBollettaIdInAndDeletedFalse(lista.stream().map(Bolletta::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(v -> v.getBolletta().getId()));

        return lista.stream()
                .map(b -> conQuote(b, persone, perBolletta.getOrDefault(b.getId(), List.of())))
                .toList();
    }

    private BollettaQuote conQuote(Bolletta b, List<Persona> persone, List<Versamento> vs) {
        boolean scaduta = b.getStato() == StatoBolletta.DA_PAGARE && b.getDataScadenza().isBefore(LocalDate.now());
        List<VersamentoResponse> elenco = vs.stream()
                .sorted(Comparator.comparing(Versamento::getData).thenComparing(Versamento::getId).reversed())
                .map(this::risposta)
                .toList();

        return new BollettaQuote(b.getId(), b.getTipo(), b.getFornitore(), b.getPeriodoDal(), b.getPeriodoAl(),
                b.getDataScadenza(), b.getImporto(), b.getStato(), scaduta, Boolean.TRUE.equals(b.getStorica()),
                quote(b, persone, vs), elenco);
    }

    private List<QuotaPersona> quote(Bolletta b, List<Persona> persone, List<Versamento> vs) {
        List<BigDecimal> parti = QuoteIrene.dividi(b.getImporto());
        boolean storica = Boolean.TRUE.equals(b.getStorica());
        List<QuotaPersona> risultato = new ArrayList<>();

        for (Persona p : persone) {
            BigDecimal quota = parti.get(p.indice());
            // una bolletta dello storico è già stata sistemata: nessuno deve più inviare niente
            BigDecimal inviato = storica
                    ? quota
                    : vs.stream().filter(v -> appartiene(v, p)).map(Versamento::getImporto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal mancante = quota.subtract(inviato);

            risultato.add(new QuotaPersona(p.coinquilinoId(), p.nome(), p.tu(), quota, inviato, mancante,
                    stato(quota, inviato, mancante, storica, p.tu())));
        }
        return risultato;
    }

    private StatoQuota stato(BigDecimal quota, BigDecimal inviato, BigDecimal mancante, boolean storica, boolean tu) {
        if (storica) {
            return StatoQuota.A_POSTO;
        }
        if (inviato.signum() == 0) {
            return quota.signum() > 0 ? StatoQuota.DA_INVIARE : StatoQuota.A_POSTO;
        }
        if (mancante.signum() > 0) {
            return StatoQuota.PARZIALE;
        }
        return tu ? StatoQuota.INVIATA : StatoQuota.DICHIARATA;
    }

    private boolean appartiene(Versamento v, Persona p) {
        return p.tu()
                ? v.getCoinquilino() == null
                : v.getCoinquilino() != null && v.getCoinquilino().getId().equals(p.coinquilinoId());
    }

    private RigaProspetto riga(TipoBolletta tipo, List<BollettaQuote> lista, int persone) {
        BigDecimal totale = lista.stream().map(BollettaQuote::importo).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CellaProspetto> celle = new ArrayList<>();

        for (int i = 0; i < persone; i++) {
            final int indice = i;
            BigDecimal quota = lista.stream().map(q -> q.persone().get(indice).quota())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal inviato = lista.stream().map(q -> q.persone().get(indice).inviato())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            celle.add(new CellaProspetto(quota, inviato, quota.subtract(inviato)));
        }
        return new RigaProspetto(tipo, lista.size(), totale, celle);
    }

    private VersamentoResponse risposta(Versamento v) {
        Coinquilino c = v.getCoinquilino();
        return new VersamentoResponse(
                v.getId(),
                v.getBolletta().getId(),
                c != null ? c.getId() : null,
                c != null ? c.getNome() : NOME_TU,
                c == null,
                v.getImporto(),
                v.getData(),
                v.getNota(),
                c != null,
                v.getMovimento() != null ? v.getMovimento().getId() : null);
    }

    private LocalDate contaDal(Long utenteId) {
        return configurazioni.findByUtenteId(utenteId).map(ConfigurazioneSaldi::getSaldiDal).orElse(null);
    }

    private boolean inConteggio(Bolletta b, LocalDate dal) {
        return dal == null || !b.getDataScadenza().isBefore(dal);
    }
}