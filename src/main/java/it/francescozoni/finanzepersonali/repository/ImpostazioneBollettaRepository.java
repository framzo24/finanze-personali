package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.ImpostazioneBolletta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImpostazioneBollettaRepository extends JpaRepository<ImpostazioneBolletta, Long> {

    List<ImpostazioneBolletta> findAllByUtenteId(Long utenteId);
}