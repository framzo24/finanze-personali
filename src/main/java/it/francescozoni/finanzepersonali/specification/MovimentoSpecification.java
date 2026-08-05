package it.francescozoni.finanzepersonali.specification;

import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MovimentoSpecification {

    private MovimentoSpecification() {
    }

    public static Specification<Movimento> dataDa(LocalDate dataDa) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("dataMovimento"), dataDa.atStartOfDay());
    }

    public static Specification<Movimento> dataA(LocalDate dataA) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("dataMovimento"), dataA.plusDays(1).atStartOfDay());
    }

    public static Specification<Movimento> categoria(Long categoriaId) {
        return (root, query, cb) ->
                cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Movimento> tipo(TipoMovimento tipo) {
        return (root, query, cb) ->
                cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Movimento> importoMin(BigDecimal importoMin) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("importo"), importoMin);
    }

    public static Specification<Movimento> importoMax(BigDecimal importoMax) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("importo"), importoMax);
    }

    public static Specification<Movimento> descrizione(String descrizione) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("descrizione")),
                        "%" + descrizione.toLowerCase() + "%"
                );
    }

    public static Specification<Movimento> nonEliminati() {

        return (root, query, cb) ->
                cb.isFalse(
                        root.get("deleted")
                );
    }
}
