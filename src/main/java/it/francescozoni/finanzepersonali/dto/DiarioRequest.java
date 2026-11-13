// DiarioRequest.java
package it.francescozoni.finanzepersonali.dto;

import jakarta.validation.constraints.Size;

public record DiarioRequest(
        @Size(max = 20000, message = "Il testo può avere al massimo 20.000 caratteri") String testo
) {
}