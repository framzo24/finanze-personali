package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.CreateMovimentoRequest;
import it.francescozoni.finanzepersonali.dto.MovimentiPageResponse;
import it.francescozoni.finanzepersonali.dto.MovimentoFilter;
import it.francescozoni.finanzepersonali.dto.UpdateMovimentoRequest;
import it.francescozoni.finanzepersonali.model.Movimento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimentoService {

    Movimento aggiungi(CreateMovimentoRequest input);

    Movimento modifica(Long id, UpdateMovimentoRequest input);

    void elimina(Long id);

    Movimento trova(Long id);

    List<Movimento> trovaTutti();

    BigDecimal saldo();

    BigDecimal totaleEntrate();

    BigDecimal totaleUscite();

    MovimentiPageResponse cerca(
            MovimentoFilter filter,
            int pagina,
            int dimensione
    );
    List<Movimento> trovaPerPeriodo(LocalDateTime da, LocalDateTime a);

    MovimentiPageResponse trovaPaginati(int pagina, int dimensione);

}