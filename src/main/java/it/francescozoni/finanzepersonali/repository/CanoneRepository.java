// CanoneRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Canone;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanoneRepository extends JpaRepository<Canone, Long> {

    List<Canone> findAllByUtenteIdAndAttivoTrueAndDeletedFalse(Long utenteId);

    boolean existsByUtenteIdAndTipoAndDeletedFalse(Long utenteId, TipoBolletta tipo);
}