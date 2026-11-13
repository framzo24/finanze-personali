// ConfigurazioneSaldiResponse.java
package it.francescozoni.finanzepersonali.dto;

import java.time.LocalDate;

public record ConfigurazioneSaldiResponse(boolean configurato, LocalDate saldiDal) {
}