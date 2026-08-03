package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Movimento;

import java.util.List;
import java.util.Optional;

public interface MovimentoRepository {

    Movimento save(Movimento movimento);

    Movimento update(Movimento movimento);

    boolean delete(Long id);

    Optional<Movimento> findById(Long id);

    List<Movimento> findAll();

    boolean existsById(Long id);

}