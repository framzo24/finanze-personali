// PagamentoRequest.java
package it.francescozoni.finanzepersonali.dto;

import java.time.LocalDate;

/** paganteId null = paghi tu. */
public record PagamentoRequest(LocalDate dataPagamento, Long paganteId, Boolean registraMovimento) {
}