package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Movimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoRepository extends JpaRepository<Movimento, Long>, JpaSpecificationExecutor<Movimento> {
    List<Movimento> findAllByDeletedFalse();

    Page<Movimento> findAllByDeletedFalse(Pageable pageable);

    Page<Movimento> findAll(
            Specification<Movimento> specification,
            Pageable pageable
    );
}