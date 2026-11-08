package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Movimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimentoRepository extends JpaRepository<Movimento, Long>, JpaSpecificationExecutor<Movimento> {

    @EntityGraph(attributePaths = {"categoria"})
    List<Movimento> findAllByDeletedFalseAndUtenteId(Long utenteId);

    @EntityGraph(attributePaths = {"categoria"})
    Page<Movimento> findAllByDeletedFalseAndUtenteId(Long utenteId, Pageable pageable);

    Optional<Movimento> findByIdAndUtenteId(Long id, Long utenteId);

    boolean existsByIdAndUtenteId(Long id, Long utenteId);

    @EntityGraph(attributePaths = {"categoria"})
    Page<Movimento> findAll(Specification<Movimento> specification, Pageable pageable);
}