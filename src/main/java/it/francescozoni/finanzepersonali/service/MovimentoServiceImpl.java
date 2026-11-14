package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.CreateMovimentoRequest;
import it.francescozoni.finanzepersonali.dto.MovimentiPageResponse;
import it.francescozoni.finanzepersonali.dto.MovimentoFilter;
import it.francescozoni.finanzepersonali.dto.UpdateMovimentoRequest;
import it.francescozoni.finanzepersonali.exception.CategoriaNotFoundException;
import it.francescozoni.finanzepersonali.mapper.MovimentoMapper;
import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.exception.MovimentoNotFoundException;
import it.francescozoni.finanzepersonali.model.enums.StatoMovimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import it.francescozoni.finanzepersonali.repository.CategoriaRepository;
import it.francescozoni.finanzepersonali.repository.MovimentoRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.specification.MovimentoSpecification;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class MovimentoServiceImpl implements MovimentoService {

    private final MovimentoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final MovimentoMapper movimentoMapper;
    private final UtenteRepository utenteRepository;
    private final UtenteCorrente utenteCorrente;

    public MovimentoServiceImpl(MovimentoRepository repository, CategoriaRepository categoriaRepository, MovimentoMapper movimentoMapper, UtenteRepository utenteRepository, UtenteCorrente utenteCorrente) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.movimentoMapper = movimentoMapper;
        this.utenteRepository = utenteRepository;
        this.utenteCorrente = utenteCorrente;
    }

    @Override
    public Movimento aggiungi(CreateMovimentoRequest input) {
        log.info(
                "Inserimento nuovo movimento: descrizione={}, importo={}, tipo={}, categoriaId={}",
                input.getDescrizione(),
                input.getImporto(),
                input.getTipo(),
                input.getCategoriaId()
        );
        LocalDateTime data = input.getDataMovimento() != null
                ? input.getDataMovimento()
                : LocalDateTime.now();
        StatoMovimento stato = input.getStato() != null
                ? input.getStato()
                : StatoMovimento.COMPLETATO;
        Categoria categoria = recuperaCategoria(input);
        Movimento movimento = Movimento.builder()
                .descrizione(input.getDescrizione())
                .importo(input.getImporto())
                .tipo(input.getTipo())
                .categoria(categoria)
                .dataMovimento(data)
                .stato(stato)
                .utente(utenteRepository.getReferenceById(utenteCorrente.id()))
                .build();

        Movimento salvato = repository.save(movimento);

        log.debug(
                "Movimento salvato con id={}",
                salvato.getId()
        );

        return salvato;
    }

    @Override
    public Movimento modifica(Long id, UpdateMovimentoRequest input) {
        log.info("Modifica movimento id={}", id);

        Movimento movimento = repository.findByIdAndUtenteId(id, utenteCorrente.id())
                .orElseThrow(() -> {
                    log.warn("Tentata modifica movimento inesistente id={}", id);
                    return new MovimentoNotFoundException(id);
                });

        Categoria categoria = categoriaRepository
                .findByIdAndDeletedFalseAndUtenteId(input.getCategoriaId(), utenteCorrente.id())
                .orElseThrow(() -> new CategoriaNotFoundException(input.getCategoriaId()));

        movimento.setDescrizione(input.getDescrizione());
        movimento.setImporto(input.getImporto());
        movimento.setTipo(input.getTipo());
        movimento.setCategoria(categoria);
        movimento.setDataMovimento(input.getDataMovimento());

        Movimento aggiornato = repository.save(movimento);

        log.debug("Movimento aggiornato id={}", aggiornato.getId());

        return aggiornato;
    }

    @Override
    public void elimina(Long id) {
        log.info("Eliminazione movimento id={}", id);
        if (!repository.existsByIdAndUtenteId(id, utenteCorrente.id())) {
            log.warn("Tentata eliminazione movimento inesistente id={}", id);
            throw new MovimentoNotFoundException(id);
        }
        repository.deleteById(id);
        log.debug(
                "Movimento eliminato id={}",
                id
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Movimento trova(Long id) {
        return repository.findByIdAndUtenteId(id, utenteCorrente.id()).orElseThrow(() -> new MovimentoNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movimento> trovaTutti() {
        return repository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id())
                .stream()
                .sorted(Comparator.comparing(Movimento::getDataMovimento).reversed())
                .toList();
    }

    @Override
    public BigDecimal saldo() {
        return totaleEntrate().subtract(totaleUscite());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totaleEntrate() {
        return repository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id())
                .stream()
                .filter(m -> m.getTipo() == TipoMovimento.ENTRATA)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal totaleUscite() {
        return repository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id())
                .stream()
                .filter(m -> m.getTipo() == TipoMovimento.USCITA)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimentiPageResponse cerca(
            MovimentoFilter filter,
            int pagina,
            int dimensione
    ) {

        Specification<Movimento> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();


        specification = specification.and(
                MovimentoSpecification.nonEliminati()
        );

        specification = specification.and(MovimentoSpecification.utente(utenteCorrente.id()));


        if (filter.getDataDa() != null) {
            specification = specification.and(
                    MovimentoSpecification.dataDa(
                            filter.getDataDa()
                    )
            );
        }


        if (filter.getDataA() != null) {
            specification = specification.and(
                    MovimentoSpecification.dataA(
                            filter.getDataA()
                    )
            );
        }

        Optional<Set<Long>> categorieFiltro = categorieDaFiltrare(filter);
        if (categorieFiltro.isPresent()) {
            specification = specification.and(MovimentoSpecification.categorie(categorieFiltro.get()));
        }

        if (filter.getTipo() != null) {
            specification = specification.and(
                    MovimentoSpecification.tipo(
                            filter.getTipo()
                    )
            );
        }


        if (filter.getImportoMin() != null) {
            specification = specification.and(
                    MovimentoSpecification.importoMin(
                            filter.getImportoMin()
                    )
            );
        }


        if (filter.getImportoMax() != null) {
            specification = specification.and(
                    MovimentoSpecification.importoMax(
                            filter.getImportoMax()
                    )
            );
        }


        if (filter.getDescrizione() != null
                && !filter.getDescrizione().isBlank()) {

            specification = specification.and(
                    MovimentoSpecification.descrizione(
                            filter.getDescrizione()
                    )
            );
        }


        Page<Movimento> page =
                repository.findAll(
                        specification,
                        PageRequest.of(
                                pagina,
                                dimensione,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "dataMovimento"
                                )
                        )
                );


        return MovimentiPageResponse.builder()
                .contenuto(
                        page.getContent()
                                .stream()
                                .map(movimentoMapper::toResponse)
                                .toList()
                )
                .pagina(page.getNumber())
                .dimensione(page.getSize())
                .totaleElementi(page.getTotalElements())
                .totalePagine(page.getTotalPages())
                .build();
    }

    @Override
    public List<Movimento> trovaPerPeriodo(LocalDateTime da, LocalDateTime a) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimentiPageResponse trovaPaginati(
            int pagina,
            int dimensione
    ) {

        Page<Movimento> page =
                repository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id(),
                        PageRequest.of(
                                pagina,
                                dimensione,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "dataMovimento"
                                )
                        )
                );


        return MovimentiPageResponse.builder()
                .contenuto(
                        page.getContent()
                                .stream()
                                .map(movimentoMapper::toResponse)
                                .toList()
                )
                .pagina(page.getNumber())
                .dimensione(page.getSize())
                .totaleElementi(page.getTotalElements())
                .totalePagine(page.getTotalPages())
                .build();
    }

    private @NonNull Categoria recuperaCategoria(CreateMovimentoRequest input) {
        return categoriaRepository
                .findByIdAndDeletedFalseAndUtenteId(input.getCategoriaId(), utenteCorrente.id())
                .orElseThrow(() -> new CategoriaNotFoundException(input.getCategoriaId()));
    }

    /** Le categorie da cercare, oppure vuoto se non c'è nessun filtro sulle categorie. */
    private Optional<Set<Long>> categorieDaFiltrare(MovimentoFilter filter) {
        Long categoria = filter.getCategoriaId();
        Long sotto = filter.getSottocategoriaId();
        if (categoria == null && sotto == null) {
            return Optional.empty();
        }

        Map<Long, List<Long>> figli = new HashMap<>();
        for (Categoria c : categoriaRepository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id())) {
            if (c.getCategoriaPadre() != null) {
                figli.computeIfAbsent(c.getCategoriaPadre().getId(), k -> new ArrayList<>()).add(c.getId());
            }
        }

        Set<Long> dallaCategoria = categoria != null ? conDiscendenti(categoria, figli) : null;
        Set<Long> dallaSotto = sotto != null ? conDiscendenti(sotto, figli) : null;

        if (dallaCategoria == null) {
            return Optional.of(dallaSotto);
        }
        if (dallaSotto == null) {
            return Optional.of(dallaCategoria);
        }
        // entrambi: conta la sottocategoria, ma solo se sta dentro la categoria scelta
        return Optional.of(dallaCategoria.contains(sotto) ? dallaSotto : Set.of());
    }

    private Set<Long> conDiscendenti(Long id, Map<Long, List<Long>> figli) {
        Set<Long> risultato = new HashSet<>();
        Deque<Long> coda = new ArrayDeque<>();
        coda.add(id);
        while (!coda.isEmpty()) {
            Long corrente = coda.poll();
            if (risultato.add(corrente)) {
                coda.addAll(figli.getOrDefault(corrente, List.of()));
            }
        }
        return risultato;
    }
}