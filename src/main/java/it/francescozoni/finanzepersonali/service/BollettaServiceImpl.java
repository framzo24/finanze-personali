// service/BollettaServiceImpl.java
package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.BollettaNotFoundException;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.mapper.BollettaMapper;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Coinquilino;
import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.OrigineBolletta;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.repository.BollettaRepository;
import it.francescozoni.finanzepersonali.repository.CoinquilinoRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.specification.BollettaSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class BollettaServiceImpl implements BollettaService {

    private static final long NESSUNA_ESCLUSIONE = -1L;

    private final BollettaRepository repository;
    private final CoinquilinoRepository coinquiliniRepository;
    private final UtenteRepository utenteRepository;
    private final UtenteCorrente utenteCorrente;
    private final BollettaMapper mapper;

    public BollettaServiceImpl(BollettaRepository repository,
                               CoinquilinoRepository coinquiliniRepository,
                               UtenteRepository utenteRepository,
                               UtenteCorrente utenteCorrente,
                               BollettaMapper mapper) {
        this.repository = repository;
        this.coinquiliniRepository = coinquiliniRepository;
        this.utenteRepository = utenteRepository;
        this.utenteCorrente = utenteCorrente;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BollettePageResponse cerca(Integer anno, TipoBolletta tipo, StatoBolletta stato, int pagina, int dimensione) {
        Long utenteId = utenteCorrente.id();

        Specification<Bolletta> specification = (root, query, cb) -> cb.conjunction();
        specification = specification
                .and(BollettaSpecification.utente(utenteId))
                .and(BollettaSpecification.nonEliminate());

        if (anno != null) {
            specification = specification.and(BollettaSpecification.anno(anno));
        }
        if (tipo != null) {
            specification = specification.and(BollettaSpecification.tipo(tipo));
        }
        if (stato != null) {
            specification = specification.and(BollettaSpecification.stato(stato));
        }

        Page<Bolletta> page = repository.findAll(specification, PageRequest.of(
                Math.max(pagina, 0),
                Math.clamp(dimensione, 1, 100),
                Sort.by(Sort.Order.desc("dataScadenza"), Sort.Order.desc("id"))));

        return new BollettePageResponse(
                page.getContent().stream().map(mapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public BollettaResponse trova(Long id) {
        return mapper.toResponse(trovaMia(id));
    }

    @Override
    public BollettaResponse crea(BollettaRequest request) {
        Long utenteId = utenteCorrente.id();
        Coinquilino pagante = valida(request, utenteId);

        doppione(request, utenteId, NESSUNA_ESCLUSIONE).ifPresent(messaggio -> {
            throw new DatiNonValidiException(messaggio);
        });

        Bolletta bolletta = Bolletta.builder()
                .utente(utenteRepository.getReferenceById(utenteId))
                .origine(OrigineBolletta.MANUALE)
                .build();
        applica(bolletta, request, pagante);

        log.info("Nuova bolletta {} {}", request.tipo(), request.periodoDal());
        return mapper.toResponse(repository.save(bolletta));
    }

    @Override
    public BollettaResponse modifica(Long id, BollettaRequest request) {
        Long utenteId = utenteCorrente.id();
        Bolletta bolletta = trovaMia(id);
        Coinquilino pagante = valida(request, utenteId);

        doppione(request, utenteId, id).ifPresent(messaggio -> {
            throw new DatiNonValidiException(messaggio);
        });

        applica(bolletta, request, pagante);
        log.info("Bolletta modificata id={}", id);
        return mapper.toResponse(repository.save(bolletta));
    }

    @Override
    public void elimina(Long id) {
        Bolletta bolletta = trovaMia(id);
        bolletta.setDeleted(true);
        repository.save(bolletta);
        log.info("Bolletta eliminata id={}", id);
    }

    @Override
    public BollettaResponse paga(Long id, PagamentoRequest request) {
        Bolletta bolletta = trovaMia(id);

        if (bolletta.getStato() == StatoBolletta.PAGATA) {
            throw new DatiNonValidiException("La bolletta risulta già pagata");
        }

        bolletta.setPagante(cercaPagante(request.paganteId(), utenteCorrente.id()));
        bolletta.setStato(StatoBolletta.PAGATA);
        bolletta.setDataPagamento(request.dataPagamento() != null ? request.dataPagamento() : LocalDate.now());

        log.info("Bolletta pagata id={}", id);
        return mapper.toResponse(repository.save(bolletta));
    }

    @Override
    public BatchStoricoResponse caricaStorico(BatchStoricoRequest request) {
        Long utenteId = utenteCorrente.id();
        int creati = 0;
        int saltati = 0;
        int numero = 0;

        // un errore su una riga annulla tutto il caricamento: o entra tutto o niente
        for (RigaStorico riga : request.righe()) {
            numero++;
            BollettaRequest r = new BollettaRequest(
                    request.tipo(), request.fornitore(),
                    riga.periodoDal(), riga.periodoAl(), null, riga.dataScadenza(),
                    riga.importo(), riga.consumo(), null,
                    NaturaBolletta.EFFETTIVA, StatoBolletta.PAGATA, riga.dataScadenza(),
                    riga.paganteId(), null);

            try {
                Coinquilino pagante = valida(r, utenteId);

                if (doppione(r, utenteId, NESSUNA_ESCLUSIONE).isPresent()) {
                    saltati++;
                    continue;
                }

                Bolletta bolletta = Bolletta.builder()
                        .utente(utenteRepository.getReferenceById(utenteId))
                        .origine(OrigineBolletta.MANUALE)
                        .build();
                applica(bolletta, r, pagante);
                repository.save(bolletta);
                creati++;
            } catch (DatiNonValidiException e) {
                throw new DatiNonValidiException("Riga " + numero + ": " + e.getMessage());
            }
        }

        log.info("Storico {}: {} bollette caricate, {} già presenti", request.tipo(), creati, saltati);
        return new BatchStoricoResponse(creati, saltati);
    }

    @Override
    @Transactional(readOnly = true)
    public RiepilogoBollette riepilogo() {
        List<Bolletta> tutte = repository
                .findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(utenteCorrente.id());
        int anno = LocalDate.now().getYear();

        List<RiepilogoTipo> tipi = Arrays.stream(TipoBolletta.values())
                .map(tipo -> riepilogoTipo(tipo, tutte, anno))
                .toList();

        List<Bolletta> daPagare = tutte.stream().filter(b -> b.getStato() == StatoBolletta.DA_PAGARE).toList();

        return new RiepilogoBollette(
                tipi,
                daPagare.size(),
                somma(daPagare.stream().map(Bolletta::getImporto).toList()),
                somma(daPagare.stream().map(BollettaMapper::quotaTua).toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinquilinoResponse> coinquilini() {
        return coinquiliniRepository.findAllByUtenteIdAndDeletedFalseOrderByIdAsc(utenteCorrente.id())
                .stream()
                .map(c -> new CoinquilinoResponse(c.getId(), c.getNome()))
                .toList();
    }

    // ---------- appoggio ----------

    private Bolletta trovaMia(Long id) {
        return repository.findByIdAndUtenteIdAndDeletedFalse(id, utenteCorrente.id())
                .orElseThrow(() -> new BollettaNotFoundException(id));
    }

    private Coinquilino cercaPagante(Long paganteId, Long utenteId) {
        if (paganteId == null) {
            return null;
        }
        return coinquiliniRepository.findByIdAndUtenteIdAndDeletedFalse(paganteId, utenteId)
                .orElseThrow(() -> new DatiNonValidiException("Il pagante scelto non esiste"));
    }

    /** Controlla i dati e restituisce il pagante (null = tu). */
    private Coinquilino valida(BollettaRequest r, Long utenteId) {
        if (r.periodoAl().isBefore(r.periodoDal())) {
            throw new DatiNonValidiException("La fine del periodo non può precedere l'inizio");
        }

        NaturaBolletta natura = r.natura() != null ? r.natura() : NaturaBolletta.EFFETTIVA;
        if (natura == NaturaBolletta.CONGUAGLIO) {
            if (r.importo().signum() == 0) {
                throw new DatiNonValidiException("L'importo del conguaglio non può essere zero");
            }
        } else if (r.importo().signum() <= 0) {
            throw new DatiNonValidiException("L'importo deve essere maggiore di zero");
        }

        if (r.consumo() != null && r.consumo().signum() < 0) {
            throw new DatiNonValidiException("Il consumo non può essere negativo");
        }

        return cercaPagante(r.paganteId(), utenteId);
    }

    private void applica(Bolletta b, BollettaRequest r, Coinquilino pagante) {
        StatoBolletta stato = r.stato() != null ? r.stato() : StatoBolletta.DA_PAGARE;

        b.setTipo(r.tipo());
        b.setFornitore(r.fornitore().trim());
        b.setPeriodoDal(r.periodoDal());
        b.setPeriodoAl(r.periodoAl());
        b.setDataEmissione(r.dataEmissione());
        b.setDataScadenza(r.dataScadenza());
        b.setImporto(r.importo().setScale(2, RoundingMode.HALF_UP));
        b.setConsumo(r.tipo() == TipoBolletta.WIFI ? null : r.consumo());
        b.setNumeroDocumento(r.numeroDocumento() == null || r.numeroDocumento().isBlank()
                ? null : r.numeroDocumento().trim());
        b.setNatura(r.natura() != null ? r.natura() : NaturaBolletta.EFFETTIVA);
        b.setStato(stato);
        b.setDataPagamento(stato == StatoBolletta.PAGATA
                ? (r.dataPagamento() != null ? r.dataPagamento() : LocalDate.now())
                : null);
        b.setPagante(pagante);
        b.setNote(r.note() == null || r.note().isBlank() ? null : r.note().trim());
    }

    private Optional<String> doppione(BollettaRequest r, Long utenteId, Long escludiId) {
        NaturaBolletta natura = r.natura() != null ? r.natura() : NaturaBolletta.EFFETTIVA;

        if (repository.existsDoppionePeriodo(utenteId, r.tipo(), natura, r.periodoDal(), r.periodoAl(), escludiId)) {
            return Optional.of("Esiste già una bolletta di questo tipo per lo stesso periodo");
        }
        if (r.numeroDocumento() != null && !r.numeroDocumento().isBlank()
                && repository.existsDoppioneNumero(utenteId, r.tipo(), r.fornitore().trim(),
                r.numeroDocumento().trim(), escludiId)) {
            return Optional.of("Esiste già una bolletta con questo numero documento");
        }
        return Optional.empty();
    }

    private RiepilogoTipo riepilogoTipo(TipoBolletta tipo, List<Bolletta> tutte, int anno) {
        List<Bolletta> lista = tutte.stream().filter(b -> b.getTipo() == tipo).toList();

        Bolletta ultima = lista.isEmpty() ? null : lista.getLast();

        List<Bolletta> dellAnno = lista.stream().filter(b -> b.getDataScadenza().getYear() == anno).toList();
        List<Bolletta> recenti = lista.subList(Math.max(0, lista.size() - 6), lista.size());
        List<Bolletta> perGrafico = lista.subList(Math.max(0, lista.size() - 8), lista.size());

        return new RiepilogoTipo(
                tipo,
                ultima != null ? mapper.toResponse(ultima) : null,
                somma(dellAnno.stream().map(Bolletta::getImporto).toList()),
                somma(dellAnno.stream().map(BollettaMapper::quotaTua).toList()),
                media(recenti.stream().map(Bolletta::getImporto).toList()),
                media(recenti.stream().map(BollettaMapper::quotaTua).toList()),
                (int) lista.stream().filter(b -> b.getStato() == StatoBolletta.DA_PAGARE).count(),
                perGrafico.stream()
                        .map(b -> new PuntoAndamento(
                                YearMonth.from(b.getPeriodoAl()).toString(),
                                b.getImporto(),
                                BollettaMapper.quotaTua(b)))
                        .toList());
    }

    private BigDecimal somma(List<BigDecimal> valori) {
        return valori.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal media(List<BigDecimal> valori) {
        return valori.isEmpty()
                ? BigDecimal.ZERO
                : somma(valori).divide(BigDecimal.valueOf(valori.size()), 2, RoundingMode.HALF_UP);
    }
}