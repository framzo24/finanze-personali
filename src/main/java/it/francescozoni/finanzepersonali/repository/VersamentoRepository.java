package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Versamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VersamentoRepository extends JpaRepository<Versamento, Long> {

    @EntityGraph(attributePaths = {"coinquilino"})
    List<Versamento> findAllByBollettaIdAndDeletedFalse(Long bollettaId);

    @EntityGraph(attributePaths = {"coinquilino"})
    List<Versamento> findAllByBollettaIdInAndDeletedFalse(Collection<Long> bollettaIds);

    @EntityGraph(attributePaths = {"coinquilino", "bolletta"})
    Optional<Versamento> findByIdAndUtenteIdAndDeletedFalse(Long id, Long utenteId);
}