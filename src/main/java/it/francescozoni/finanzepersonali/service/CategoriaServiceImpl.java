package it.francescozoni.finanzepersonali.service;


import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.CategoriaNotFoundException;
import it.francescozoni.finanzepersonali.mapper.CategoriaMapper;
import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.model.enums.AzioneEliminazioneCategoria;
import it.francescozoni.finanzepersonali.repository.CategoriaRepository;
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


    public CategoriaServiceImpl(
            CategoriaRepository repository,
            CategoriaMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> trovaTutte() {

        log.debug("Recupero tutte le categorie");

        return repository
                .findAllByDeletedFalseAndCategoriaPadreIsNull()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse trova(Long id) {

        log.debug("Recupero categoria id={}", id);

        Categoria categoria =
                repository.findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () -> new CategoriaNotFoundException(id)
                        );

        return mapper.toResponse(categoria);
    }


    @Override
    public CategoriaResponse crea(
            CreateCategoriaRequest request
    ) {

        log.info("Creazione categoria nome={}", request.nome());

        if(repository.existsByNomeIgnoreCaseAndDeletedFalse(
                request.nome()
        )) {

            throw new IllegalArgumentException(
                    "Esiste già una categoria con questo nome"
            );

        }


        Categoria categoriaPadre = null;


        if (request.categoriaPadreId() != null) {

            categoriaPadre =
                    repository.findByIdAndDeletedFalse(
                                    request.categoriaPadreId()
                            )
                            .orElseThrow(
                                    () -> new CategoriaNotFoundException(
                                            request.categoriaPadreId()
                                    )
                            );


            if (calcolaLivello(categoriaPadre) >= 3) {

                throw new IllegalArgumentException(
                        "Non è possibile creare più di 3 livelli di categorie"
                );

            }

        }


        Categoria categoria =
                Categoria.builder()
                        .nome(request.nome())
                        .icona(request.icona())
                        .colore(request.colore())
                        .categoriaPadre(categoriaPadre)
                        .build();


        return mapper.toResponse(
                repository.save(categoria)
        );
    }



    @Override
    public CategoriaResponse modifica(
            Long id,
            UpdateCategoriaRequest request
    ) {

        log.info("Modifica categoria id={}", id);


        Categoria categoria =
                repository.findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () -> new CategoriaNotFoundException(id)
                        );


        Categoria categoriaPadre = null;


        if (request.categoriaPadreId() != null) {

            if (id.equals(request.categoriaPadreId())) {

                throw new IllegalArgumentException(
                        "Una categoria non può essere padre di se stessa"
                );
            }


            categoriaPadre =
                    repository.findByIdAndDeletedFalse(
                                    request.categoriaPadreId()
                            )
                            .orElseThrow(
                                    () -> new CategoriaNotFoundException(
                                            request.categoriaPadreId()
                                    )
                            );
        }

        if(!categoria.getNome()
                .equalsIgnoreCase(request.nome())
                &&
                repository.existsByNomeIgnoreCaseAndDeletedFalse(
                        request.nome()
                )) {


            throw new IllegalArgumentException(
                    "Esiste già una categoria con questo nome"
            );

        }

        categoria.setNome(request.nome());

        categoria.setIcona(request.icona());

        categoria.setColore(request.colore());

        categoria.setCategoriaPadre(categoriaPadre);


        return mapper.toResponse(
                repository.save(categoria)
        );
    }



    @Override
    public void elimina(
            Long id,
            AzioneEliminazioneCategoria azione,
            Long nuovaCategoriaPadreId
    ) {

        log.info("Eliminazione categoria id={}", id);
        Categoria categoria =
                repository.findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () -> new CategoriaNotFoundException(id)
                        );


        switch (azione) {

            case ELIMINA_ALBERO -> {

                eliminaRicorsiva(categoria);

            }

            case SPOSTA_FIGLI -> {

                if (nuovaCategoriaPadreId == null) {

                    throw new IllegalArgumentException(

                            "Specificare la nuova categoria padre"

                    );

                }

                Categoria nuovoPadre =

                        repository.findByIdAndDeletedFalse(

                                        nuovaCategoriaPadreId

                                )

                                .orElseThrow(

                                        () -> new CategoriaNotFoundException(

                                                nuovaCategoriaPadreId

                                        )

                                );

                spostaFigli(

                        categoria,

                        nuovoPadre

                );

                categoria.setDeleted(true);

                repository.save(categoria);

            }

        }

    }



    private void eliminaRicorsiva(Categoria categoria) {
        List<Categoria> figli =
                List.copyOf(categoria.getSottoCategorie());
        for (Categoria figlio : figli) {
            eliminaRicorsiva(figlio);
        }
        categoria.setDeleted(true);
        repository.save(categoria);

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> trovaTuttePiatte() {

        return repository
                .findAllByDeletedFalse()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void spostaFigli(
            Categoria categoria,
            Categoria nuovoPadre
    ) {


        for (Categoria figlio :
                categoria.getSottoCategorie()) {


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



}