// BatchStoricoRequest.java
package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchStoricoRequest(
        @NotNull TipoBolletta tipo,
        @NotBlank @Size(max = 120) String fornitore,
        @NotEmpty @Size(max = 200) List<@Valid RigaStorico> righe
) {
}