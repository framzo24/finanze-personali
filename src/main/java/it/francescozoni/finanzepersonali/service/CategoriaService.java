package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;

import java.util.List;

public interface CategoriaService {


    List<CategoriaResponse> trovaTutte();


    CategoriaResponse trova(Long id);


    CategoriaResponse crea(
            CreateCategoriaRequest request
    );


    CategoriaResponse modifica(
            Long id,
            UpdateCategoriaRequest request
    );


    void elimina(Long id);

}