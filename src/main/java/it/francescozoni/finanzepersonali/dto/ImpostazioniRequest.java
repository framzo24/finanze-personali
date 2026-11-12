// ImpostazioniRequest.java
package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ImpostazioniRequest(@NotEmpty @Size(max = 4) List<@Valid ImpostazioneRequest> impostazioni) {
}