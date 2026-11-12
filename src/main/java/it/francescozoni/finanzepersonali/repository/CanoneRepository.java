// CanoneRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Canone;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CanoneRepository extends JpaRepository<Canone, Long> {

    List<Canone> findAllByUtenteIdAndAttivoTrueAndDeletedFalse(Long utenteId);

    boolean existsByUtenteIdAndTipoAndDeletedFalse(Long utenteId, TipoBolletta tipo);

    List<Canone> findAllByUtenteIdAndDeletedFalseOrderByIdAsc(Long utenteId);

    Optional<Canone> findByIdAndUtenteIdAndDeletedFalse(Long id, Long utenteId);
}