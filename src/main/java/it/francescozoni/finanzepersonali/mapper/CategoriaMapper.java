package it.francescozoni.finanzepersonali.mapper;

import it.francescozoni.finanzepersonali.dto.CategoriaResponse;
import it.francescozoni.finanzepersonali.model.Categoria;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CategoriaMapper {


    public CategoriaResponse toResponse(
            Categoria categoria
    ) {

        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getIcona(),
                categoria.getColore(),
                categoria.getCategoriaPadre() != null
                        ? categoria.getCategoriaPadre().getId()
                        : null,
                categoria.getCategoriaPadre() != null
                        ? categoria.getCategoriaPadre().getNome()
                        : null,

                categoria.getSottoCategorie() == null
                        ?
                        Collections.emptyList()
                        : categoria.getSottoCategorie()
                                .stream()
                                .map(this::toResponse)
                                .toList()
        );
    }
}