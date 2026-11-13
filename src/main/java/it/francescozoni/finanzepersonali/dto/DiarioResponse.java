// DiarioResponse.java
package it.francescozoni.finanzepersonali.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** testo vuoto = quel giorno non ha un pensiero. */
public record DiarioResponse(LocalDate giorno, String testo, LocalDateTime aggiornatoIl) {
}