package it.francescozoni.finanzepersonali.mapper;

import it.francescozoni.finanzepersonali.dto.BollettaResponse;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Coinquilino;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class BollettaMapper {

    public BollettaResponse toResponse(Bolletta b) {
        Coinquilino pagante = b.getPagante();
        boolean scaduta = b.getStato() == StatoBolletta.DA_PAGARE && b.getDataScadenza().isBefore(LocalDate.now());
        BigDecimal costoUnitario = b.getConsumo() != null && b.getConsumo().signum() > 0
                ? b.getImporto().divide(b.getConsumo(), 4, RoundingMode.HALF_UP)
                : null;

        return new BollettaResponse(
                b.getId(),
                b.getTipo(),
                b.getFornitore(),
                b.getPeriodoDal(),
                b.getPeriodoAl(),
                b.getDataEmissione(),
                b.getDataScadenza(),
                b.getImporto(),
                b.getConsumo(),
                b.getTipo().getUnita(),
                costoUnitario,
                b.getNumeroDocumento(),
                b.getNatura(),
                b.getStato(),
                scaduta,
                b.getDataPagamento(),
                pagante != null ? pagante.getId() : null,
                pagante != null ? pagante.getNome() : "Tu",
                quotaTua(b),
                b.getNote(),
                b.getOrigine()
        );
    }

    /**
     * La quota è un terzo dell'importo. Il centesimo che avanza resta al pagante:
     * se hai pagato tu la tua quota è "importo meno due terzi", altrimenti è un terzo.
     */
    public static BigDecimal quotaTua(Bolletta b) {
        BigDecimal terzo = b.getImporto().divide(BigDecimal.valueOf(3), 2, RoundingMode.DOWN);
        return b.getPagante() == null
                ? b.getImporto().subtract(terzo.multiply(BigDecimal.valueOf(2)))
                : terzo;
    }
}