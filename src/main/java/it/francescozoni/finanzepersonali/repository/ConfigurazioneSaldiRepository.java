// repository/ConfigurazioneSaldiRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.ConfigurazioneSaldi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurazioneSaldiRepository extends JpaRepository<ConfigurazioneSaldi, Long> {

    Optional<ConfigurazioneSaldi> findByUtenteId(Long utenteId);
}