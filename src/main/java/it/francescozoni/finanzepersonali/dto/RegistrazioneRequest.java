package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrazioneRequest(
        @NotBlank(message = "Il nome è obbligatorio")
        @Size(max = 60, message = "Il nome può avere al massimo 60 caratteri")
        String nome,

        @NotBlank(message = "Il cognome è obbligatorio")
        @Size(max = 60, message = "Il cognome può avere al massimo 60 caratteri")
        String cognome,

        @NotBlank(message = "La password è obbligatoria")
        @Size(min = 8, max = 72, message = "La password deve avere da 8 a 72 caratteri")
        String password
) {
}