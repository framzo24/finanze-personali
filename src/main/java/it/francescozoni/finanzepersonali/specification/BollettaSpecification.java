// BollettaSpecification.java (pacchetto specification/)
package it.francescozoni.finanzepersonali.specification;

import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class BollettaSpecification {

    private BollettaSpecification() {
    }

    public static Specification<Bolletta> utente(Long utenteId) {
        return (root, query, cb) -> cb.equal(root.get("utente").get("id"), utenteId);
    }

    public static Specification<Bolletta> nonEliminate() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    /** Anno della data di scadenza. */
    public static Specification<Bolletta> anno(int anno) {
        return (root, query, cb) -> cb.between(root.get("dataScadenza"),
                LocalDate.of(anno, 1, 1), LocalDate.of(anno, 12, 31));
    }

    public static Specification<Bolletta> tipo(TipoBolletta tipo) {
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Bolletta> stato(StatoBolletta stato) {
        return (root, query, cb) -> cb.equal(root.get("stato"), stato);
    }
}