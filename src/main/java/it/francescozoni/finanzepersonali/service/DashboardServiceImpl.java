package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.AndamentoMensileResponse;
import it.francescozoni.finanzepersonali.dto.CategoriaTotaleResponse;
import it.francescozoni.finanzepersonali.dto.DashboardResponse;
import it.francescozoni.finanzepersonali.exception.DatiNonValidiException;
import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import it.francescozoni.finanzepersonali.repository.MovimentoRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int MESI_ANDAMENTO = 6;

    private final MovimentoRepository movimentoRepository;
    private final UtenteCorrente utenteCorrente;

    public DashboardServiceImpl(MovimentoRepository movimentoRepository, UtenteCorrente utenteCorrente) {
        this.movimentoRepository = movimentoRepository;
        this.utenteCorrente = utenteCorrente;
    }

    @Override
    public DashboardResponse riepilogo() {
        List<Movimento> movimenti = movimentoRepository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id());
        BigDecimal entrate = totale(movimenti, TipoMovimento.ENTRATA);
        BigDecimal uscite = totale(movimenti, TipoMovimento.USCITA);

        YearMonth meseCorrente = YearMonth.now();
        List<Movimento> movimentiMese = movimenti.stream()
                .filter(m -> YearMonth.from(m.getDataMovimento()).equals(meseCorrente))
                .toList();

        return DashboardResponse.builder()
                .saldo(entrate.subtract(uscite))
                .totaleEntrate(entrate)
                .totaleUscite(uscite)
                .entrateMeseCorrente(totale(movimentiMese, TipoMovimento.ENTRATA))
                .usciteMeseCorrente(totale(movimentiMese, TipoMovimento.USCITA))
                .spesePerCategoria(calcolaCategorie(movimenti))
                .andamentoMensile(calcolaAndamento(movimenti))
                .build();
    }

    @Override
    public List<CategoriaTotaleResponse> spesePerMese(int anno, int mese) {
        YearMonth periodo;
        try {
            periodo = YearMonth.of(anno, mese);
        } catch (DateTimeException e) {
            throw new DatiNonValidiException("Mese non valido");
        }

        List<Movimento> delMese = movimentoRepository.findAllByDeletedFalseAndUtenteId(utenteCorrente.id()).stream()
                .filter(m -> YearMonth.from(m.getDataMovimento()).equals(periodo))
                .toList();

        return calcolaCategorie(delMese);
    }

    private BigDecimal totale(List<Movimento> movimenti, TipoMovimento tipo) {
        return movimenti.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(Movimento::getImporto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoriaTotaleResponse> calcolaCategorie(List<Movimento> movimenti) {
        return movimenti.stream()
                .filter(m -> m.getTipo() == TipoMovimento.USCITA && m.getCategoria() != null)
                .collect(Collectors.groupingBy(m -> m.getCategoria().getId()))
                .values()
                .stream()
                .map(lista -> {
                    Categoria categoria = lista.getFirst().getCategoria();
                    return CategoriaTotaleResponse.builder()
                            .categoriaId(categoria.getId())
                            .categoria(categoria.getNome())
                            .colore(categoria.getColore())
                            .icona(categoria.getIcona())
                            .totale(lista.stream()
                                    .map(Movimento::getImporto)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .build();
                })
                .sorted(Comparator.comparing(CategoriaTotaleResponse::totale).reversed())
                .toList();
    }

    /** Ultimi N mesi in ordine cronologico, compresi quelli senza movimenti. */
    private List<AndamentoMensileResponse> calcolaAndamento(List<Movimento> movimenti) {
        Map<YearMonth, List<Movimento>> perMese = movimenti.stream()
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getDataMovimento())));

        YearMonth ultimo = YearMonth.now();

        return IntStream.range(0, MESI_ANDAMENTO)
                .mapToObj(i -> ultimo.minusMonths(MESI_ANDAMENTO - 1L - i))
                .map(mese -> {
                    List<Movimento> lista = perMese.getOrDefault(mese, List.of());
                    return AndamentoMensileResponse.builder()
                            .mese(mese.toString())
                            .entrate(totale(lista, TipoMovimento.ENTRATA))
                            .uscite(totale(lista, TipoMovimento.USCITA))
                            .build();
                })
                .toList();
    }
}