package it.francescozoni.finanzepersonali.mapper;

import it.francescozoni.finanzepersonali.dto.BollettaResponse;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Coinquilino;
import it.francescozoni.finanzepersonali.model.enums.ModalitaBollette;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.util.QuoteIrene;
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
                b.getOrigine(),
                b.getMovimento() != null ? b.getMovimento().getId() : null
        );
    }

    public static BigDecimal quotaTua(Bolletta b) {
        if (b.getUtente().getModalitaBollette() == ModalitaBollette.IRENE) {
            return QuoteIrene.quota(b.getImporto(), QuoteIrene.INDICE_TU);
        }
        BigDecimal terzo = b.getImporto().divide(BigDecimal.valueOf(3), 2, RoundingMode.DOWN);
        return b.getPagante() == null
                ? b.getImporto().subtract(terzo.multiply(BigDecimal.valueOf(2)))
                : terzo;
    }
}