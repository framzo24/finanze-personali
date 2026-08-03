package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.model.Movimento;

import java.math.BigDecimal;
import java.util.List;

public interface MovimentoService {

    Movimento aggiungi(Movimento movimento);

    Movimento modifica(Movimento movimento);

    void elimina(Long id);

    Movimento trova(Long id);

    List<Movimento> trovaTutti();

    BigDecimal saldo();

    BigDecimal totaleEntrate();

    BigDecimal totaleUscite();

}