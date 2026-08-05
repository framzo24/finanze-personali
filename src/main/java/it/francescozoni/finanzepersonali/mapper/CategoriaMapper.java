package it.francescozoni.finanzepersonali.mapper;

import it.francescozoni.finanzepersonali.dto.CategoriaResponse;
import it.francescozoni.finanzepersonali.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {


    public CategoriaResponse toResponse(
            Categoria categoria
    ) {

        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getIcona(),
                categoria.getColore()
        );
    }
}