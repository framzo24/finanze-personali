// TariffaCanoneRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.TariffaCanone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffaCanoneRepository extends JpaRepository<TariffaCanone, Long> {

    List<TariffaCanone> findAllByCanoneIdOrderByValidaDalAsc(Long canoneId);
}