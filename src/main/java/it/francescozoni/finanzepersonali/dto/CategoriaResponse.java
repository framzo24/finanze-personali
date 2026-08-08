package it.francescozoni.finanzepersonali.dto;

import java.util.List;

public record CategoriaResponse(

        Long id,

        String nome,

        String icona,

        String colore,

        Long categoriaPadreId,

        String categoriaPadreNome,

        List<CategoriaResponse> sottoCategorie

) {
}