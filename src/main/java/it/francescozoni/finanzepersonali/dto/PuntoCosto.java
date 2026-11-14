package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;

import java.math.BigDecimal;

public record PuntoCosto(String periodo, BigDecimal costo, BigDecimal importo, BigDecimal consumo, NaturaBolletta natura) {
}