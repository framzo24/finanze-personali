// service/BollettaService.java
package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;

import java.util.List;

public interface BollettaService {

    BollettePageResponse cerca(Integer anno, TipoBolletta tipo, StatoBolletta stato, int pagina, int dimensione);

    BollettaResponse trova(Long id);

    BollettaResponse crea(BollettaRequest request);

    BollettaResponse modifica(Long id, BollettaRequest request);

    void elimina(Long id);

    BollettaResponse paga(Long id, PagamentoRequest request);

    BatchStoricoResponse caricaStorico(BatchStoricoRequest request);

    RiepilogoBollette riepilogo();

    List<CoinquilinoResponse> coinquilini();
}