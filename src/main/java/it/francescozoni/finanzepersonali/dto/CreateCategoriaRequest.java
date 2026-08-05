package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoriaRequest(

        @NotBlank(message = "Il nome è obbligatorio")
        String nome,

        String icona,

        String colore

) {
}