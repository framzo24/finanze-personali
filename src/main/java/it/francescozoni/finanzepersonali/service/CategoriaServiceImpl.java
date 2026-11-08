package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.CategoriaNotFoundException;
import it.francescozoni.finanzepersonali.mapper.CategoriaMapper;
import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.model.enums.AzioneEliminazioneCategoria;
import it.francescozoni.finanzepersonali.repository.CategoriaRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repository;
    private final CategoriaMapper mapper;
    private final UtenteRepository utenteRepository;
    private final UtenteCorrente utenteCorrente;

    public CategoriaServiceImpl(
            CategoriaRepository repository,
            CategoriaMapper mapper,
            UtenteRepository utenteRepository,
            UtenteCorrente utenteCorrente
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.utenteRepository = utenteRepository;
        this.utenteCorrente = utenteCorrente;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> trovaTutte() {
        log.debug("Recupero tutte le categorie");

        return repository
                .findAllByDeletedFalseAndCategoriaPadreIsNullAndUtenteId(utenteCorrente.id())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse trova(Long id) {
        log.debug("Recupero categoria id={}", id);
        return mapper.toResponse(trovaMia(id));
    }

    @Override
    public CategoriaResponse crea(CreateCategoriaRequest request) {
        log.info("Creazione categoria nome={}", request.nome());

        Long utenteId = utenteCorrente.id();

        if (repository.existsByNomeIgnoreCaseAndDeletedFalseAndUtenteId(request.nome(), utenteId)) {
            throw new IllegalArgumentException("Esiste già una categoria con questo nome");
        }

        Categoria categoriaPadre = null;

        if (request.categoriaPadreId() != null) {
            categoriaPadre = trovaMia(request.categoriaPadreId());

            if (calcolaLivello(categoriaPadre) >= 3) {
                throw new IllegalArgumentException("Non è possibile creare più di 3 livelli di categorie");
            }
        }

        Categoria categoria = Categoria.builder()
                .nome(request.nome())
                .icona(request.icona())
                .colore(request.colore())
                .categoriaPadre(categoriaPadre)
                .utente(utenteRepository.getReferenceById(utenteId))
                .build();

        return mapper.toResponse(repository.save(categoria));
    }

    @Override
    public CategoriaResponse modifica(Long id, UpdateCategoriaRequest request) {
        log.info("Modifica categoria id={}", id);

        Long utenteId = utenteCorrente.id();
        Categoria categoria = trovaMia(id);

        Categoria categoriaPadre = null;

        if (request.categoriaPadreId() != null) {
            categoriaPadre = trovaMia(request.categoriaPadreId());

            // niente cicli: il padre non può essere la categoria stessa né una sua discendente
            if (eStessaODiscendente(categoriaPadre, categoria)) {
                throw new IllegalArgumentException(
                        "Una categoria non può essere inserita dentro se stessa o dentro una sua sottocategoria");
            }
        }

        if (!categoria.getNome().equalsIgnoreCase(request.nome())
                && repository.existsByNomeIgnoreCaseAndDeletedFalseAndUtenteId(request.nome(), utenteId)) {
            throw new IllegalArgumentException("Esiste già una categoria con questo nome");
        }

        categoria.setNome(request.nome());
        categoria.setIcona(request.icona());
        categoria.setColore(request.colore());
        categoria.setCategoriaPadre(categoriaPadre);

        return mapper.toResponse(repository.save(categoria));
    }

    @Override
    public void elimina(Long id, AzioneEliminazioneCategoria azione, Long nuovaCategoriaPadreId) {
        log.info("Eliminazione categoria id={}", id);

        Categoria categoria = trovaMia(id);

        switch (azione) {
            case ELIMINA_ALBERO -> eliminaRicorsiva(categoria);

            case SPOSTA_FIGLI -> {
                if (nuovaCategoriaPadreId == null) {
                    throw new IllegalArgumentException("Specificare la nuova categoria padre");
                }

                Categoria nuovoPadre = trovaMia(nuovaCategoriaPadreId);

                if (eStessaODiscendente(nuovoPadre, categoria)) {
                    throw new IllegalArgumentException(
                            "La nuova categoria padre non può essere quella che stai eliminando o una sua sottocategoria");
                }

                spostaFigli(categoria, nuovoPadre);

                categoria.setDeleted(true);
                repository.save(categoria);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> trovaTuttePiatte() {
        return repository
                .findAllByDeletedFalseAndUtenteId(utenteCorrente.id())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------- metodi di appoggio ----------

    /** Una categoria dell'utente corrente: se è di un altro utente risulta "non trovata". */
    private Categoria trovaMia(Long id) {
        return repository.findByIdAndDeletedFalseAndUtenteId(id, utenteCorrente.id())
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    private void eliminaRicorsiva(Categoria categoria) {
        List<Categoria> figli = List.copyOf(categoria.getSottoCategorie());
        for (Categoria figlio : figli) {
            eliminaRicorsiva(figlio);
        }
        categoria.setDeleted(true);
        repository.save(categoria);
    }

    private void spostaFigli(Categoria categoria, Categoria nuovoPadre) {
        for (Categoria figlio : List.copyOf(categoria.getSottoCategorie())) {
            figlio.setCategoriaPadre(nuovoPadre);
            repository.save(figlio);
        }
    }

    private int calcolaLivello(Categoria categoria) {
        int livello = 1;
        while (categoria.getCategoriaPadre() != null) {
            livello++;
            categoria = categoria.getCategoriaPadre();
        }
        return livello;
    }

    /** Vero se "candidata" è "antenata" oppure si trova sotto di lei nell'albero. */
    private boolean eStessaODiscendente(Categoria candidata, Categoria antenata) {
        for (Categoria c = candidata; c != null; c = c.getCategoriaPadre()) {
            if (c.getId().equals(antenata.getId())) {
                return true;
            }
        }
        return false;
    }
}