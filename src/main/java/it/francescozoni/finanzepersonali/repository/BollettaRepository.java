// BollettaRepository.java
package it.francescozoni.finanzepersonali.repository;

import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BollettaRepository extends JpaRepository<Bolletta, Long>, JpaSpecificationExecutor<Bolletta> {

    @EntityGraph(attributePaths = {"pagante"})
    Optional<Bolletta> findByIdAndUtenteIdAndDeletedFalse(Long id, Long utenteId);

    @EntityGraph(attributePaths = {"pagante"})
    List<Bolletta> findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(Long utenteId);

    @EntityGraph(attributePaths = {"pagante"})
    Page<Bolletta> findAll(Specification<Bolletta> specification, Pageable pageable);

    /** Periodi già generati per un canone, comprese le bollette eliminate (così non si rigenerano). */
    @Query("select b.periodoDal from Bolletta b where b.canone.id = :canoneId")
    List<LocalDate> findPeriodiDalByCanoneId(@Param("canoneId") Long canoneId);

    @Query("""
            select count(b) > 0 from Bolletta b
            where b.utente.id = :utenteId and b.deleted = false and b.id <> :escludiId
              and b.tipo = :tipo and b.natura = :natura
              and b.periodoDal = :dal and b.periodoAl = :al
            """)
    boolean existsDoppionePeriodo(@Param("utenteId") Long utenteId,
                                  @Param("tipo") TipoBolletta tipo,
                                  @Param("natura") NaturaBolletta natura,
                                  @Param("dal") LocalDate dal,
                                  @Param("al") LocalDate al,
                                  @Param("escludiId") Long escludiId);

    @Query("""
            select count(b) > 0 from Bolletta b
            where b.utente.id = :utenteId and b.deleted = false and b.id <> :escludiId
              and b.tipo = :tipo and lower(b.fornitore) = lower(:fornitore)
              and b.numeroDocumento = :numero
            """)
    boolean existsDoppioneNumero(@Param("utenteId") Long utenteId,
                                 @Param("tipo") TipoBolletta tipo,
                                 @Param("fornitore") String fornitore,
                                 @Param("numero") String numero,
                                 @Param("escludiId") Long escludiId);
}