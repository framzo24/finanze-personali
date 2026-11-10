// CoinquilinoRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Coinquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinquilinoRepository extends JpaRepository<Coinquilino, Long> {

    List<Coinquilino> findAllByUtenteIdAndDeletedFalseOrderByIdAsc(Long utenteId);

    Optional<Coinquilino> findByIdAndUtenteIdAndDeletedFalse(Long id, Long utenteId);
}