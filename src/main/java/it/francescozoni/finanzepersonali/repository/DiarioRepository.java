package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.VoceDiario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiarioRepository extends JpaRepository<VoceDiario, Long> {

    Optional<VoceDiario> findByUtenteIdAndGiorno(Long utenteId, LocalDate giorno);

    List<VoceDiario> findByUtenteIdOrderByGiornoDesc(Long utenteId, Pageable pageable);

    List<VoceDiario> findAllByUtenteIdAndGiornoBetweenOrderByGiornoDesc(Long utenteId, LocalDate da, LocalDate a);

    @Query("""
            select v.giorno from VoceDiario v
            where v.utente.id = :utenteId and v.giorno between :da and :a
            order by v.giorno
            """)
    List<LocalDate> findGiorni(@Param("utenteId") Long utenteId,
                               @Param("da") LocalDate da,
                               @Param("a") LocalDate a);
}