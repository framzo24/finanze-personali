package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;
import jakarta.validation.constraints.NotNull;

public record ModalitaRequest(@NotNull(message = "Scegli una modalità") ModalitaBollette modalita) {
}