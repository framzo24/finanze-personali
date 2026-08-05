package it.francescozoni.finanzepersonali.service;


import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.exception.CategoriaNotFoundException;
import it.francescozoni.finanzepersonali.mapper.CategoriaMapper;
import it.francescozoni.finanzepersonali.model.Categoria;
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

        return repository.findAllByDeletedFalse()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse trova(Long id) {

        return mapper.toResponse(
                repository.findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () -> new CategoriaNotFoundException(id)
                        )
        );
    }


    @Override
    public CategoriaResponse crea(
            CreateCategoriaRequest request
    ) {

        Categoria categoria =
                Categoria.builder()
                        .nome(request.nome())
                        .icona(request.icona())
                        .colore(request.colore())
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

        Categoria categoria =
                repository.findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () -> new CategoriaNotFoundException(id)
                        );


        categoria.setNome(request.nome());
        categoria.setIcona(request.icona());
        categoria.setColore(request.colore());


        return mapper.toResponse(
                repository.save(categoria)
        );
    }


    @Override
    public void elimina(Long id) {

        Categoria categoria =
                repository.findByIdAndDeletedFalse(id)
                        .orElseThrow(
                                () -> new CategoriaNotFoundException(id)
                        );


        categoria.setDeleted(true);

        repository.save(categoria);
    }
}