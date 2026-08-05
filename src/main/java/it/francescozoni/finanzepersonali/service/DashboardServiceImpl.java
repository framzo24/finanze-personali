package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.DashboardResponse;
import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import it.francescozoni.finanzepersonali.repository.MovimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.francescozoni.finanzepersonali.dto.AndamentoMensileResponse;
import it.francescozoni.finanzepersonali.dto.CategoriaTotaleResponse;
import it.francescozoni.finanzepersonali.dto.DashboardResponse;
import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final MovimentoRepository movimentoRepository;

    public DashboardServiceImpl(
            MovimentoRepository movimentoRepository
    ) {
        this.movimentoRepository = movimentoRepository;
    }

    @Override
    public DashboardResponse riepilogo() {
        List<Movimento> movimenti =
                movimentoRepository.findAllByDeletedFalse();
        BigDecimal entrate =
                totale(movimenti, TipoMovimento.ENTRATA);
        BigDecimal uscite =
                totale(movimenti, TipoMovimento.USCITA);

        YearMonth meseCorrente =
                YearMonth.now();

        List<Movimento> movimentiMese =
                movimenti.stream()
                        .filter(m -> YearMonth.from(m.getDataMovimento()).equals(meseCorrente))
                        .toList();

        BigDecimal entrateMese =
                totale(movimentiMese, TipoMovimento.ENTRATA);
        BigDecimal usciteMese =
                totale(movimentiMese, TipoMovimento.USCITA);

        return DashboardResponse.builder()
                .saldo(entrate.subtract(uscite))
                .totaleEntrate(entrate)
                .totaleUscite(uscite)
                .entrateMeseCorrente(entrateMese)
                .usciteMeseCorrente(usciteMese)
                .spesePerCategoria(
                        calcolaCategorie(movimenti)
                )
                .andamentoMensile(
                        calcolaAndamento(movimenti)
                )
                .build();
    }

    private BigDecimal totale(
            List<Movimento> movimenti,
            TipoMovimento tipo
    ) {

        return movimenti.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(Movimento::getImporto)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private List<CategoriaTotaleResponse> calcolaCategorie(
            List<Movimento> movimenti
    ) {

        return movimenti.stream()
                .filter(m -> m.getTipo() == TipoMovimento.USCITA)
                .collect(Collectors.groupingBy(
                        m -> m.getCategoria().getId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Movimento::getImporto,
                                BigDecimal::add
                        )
                ))
                .entrySet()
                .stream()
                .map(entry ->
                        CategoriaTotaleResponse.builder()
                                .categoriaId(entry.getKey())
                                .categoria(
                                        movimenti.stream()
                                                .filter(m ->
                                                        m.getCategoria().getId()
                                                                .equals(entry.getKey())
                                                )
                                                .findFirst()
                                                .get()
                                                .getCategoria()
                                                .getNome()
                                )
                                .totale(entry.getValue())
                                .build()
                )
                .toList();
    }

    private List<AndamentoMensileResponse> calcolaAndamento(
            List<Movimento> movimenti
    ) {

        return movimenti.stream()
                .collect(Collectors.groupingBy(
                        m -> YearMonth.from(m.getDataMovimento())
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {

                    List<Movimento> mese =
                            entry.getValue();

                    return AndamentoMensileResponse.builder()
                            .mese(entry.getKey().toString())
                            .entrate(
                                    totale(mese, TipoMovimento.ENTRATA)
                            )
                            .uscite(
                                    totale(mese, TipoMovimento.USCITA)
                            )
                            .build();
                })
                .toList();
    }
}
