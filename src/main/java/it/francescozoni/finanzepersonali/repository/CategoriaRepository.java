package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByDeletedFalseAndUtenteId(Long utenteId);

    List<Categoria> findAllByDeletedFalseAndCategoriaPadreIsNullAndUtenteId(Long utenteId);

    Optional<Categoria> findByIdAndDeletedFalseAndUtenteId(Long id, Long utenteId);

    boolean existsByNomeIgnoreCaseAndDeletedFalseAndUtenteId(String nome, Long utenteId);
}