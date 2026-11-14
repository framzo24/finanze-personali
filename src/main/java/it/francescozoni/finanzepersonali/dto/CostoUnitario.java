package it.francescozoni.finanzepersonali.dto;

import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.util.List;

public record CostoUnitario(TipoBolletta tipo, String unita, List<PuntoCosto> punti) {
}