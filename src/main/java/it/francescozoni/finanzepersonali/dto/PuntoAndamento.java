// PuntoAndamento.java
package it.francescozoni.finanzepersonali.dto;

import java.math.BigDecimal;

public record PuntoAndamento(String periodo, BigDecimal importo, BigDecimal quota) {
}