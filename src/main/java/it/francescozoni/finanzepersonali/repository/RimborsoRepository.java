// repository/RimborsoRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Rimborso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RimborsoRepository extends JpaRepository<Rimborso, Long> {

    @EntityGraph(attributePaths = {"coinquilino"})
    List<Rimborso> findAllByUtenteIdAndDeletedFalseOrderByDataAscIdAsc(Long utenteId);

    @EntityGraph(attributePaths = {"coinquilino"})
    Optional<Rimborso> findByIdAndUtenteIdAndDeletedFalse(Long id, Long utenteId);
}