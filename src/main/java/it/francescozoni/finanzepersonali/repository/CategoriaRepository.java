package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByDeletedFalse();

    Optional<Categoria> findByIdAndDeletedFalse(Long id);

    List<Categoria> findAllByDeletedFalseAndCategoriaPadreIsNull();

    boolean existsByNomeIgnoreCaseAndDeletedFalse(String nome);
}
