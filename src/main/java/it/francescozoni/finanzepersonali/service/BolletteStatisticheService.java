package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.CostoUnitario;
import it.francescozoni.finanzepersonali.dto.PuntoCosto;
import it.francescozoni.finanzepersonali.dto.StatisticaTipo;
import it.francescozoni.finanzepersonali.dto.StatisticheBollette;
import it.francescozoni.finanzepersonali.mapper.BollettaMapper;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.enums.NaturaBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.repository.BollettaRepository;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BolletteStatisticheService {

    private static final int PUNTI_COSTO = 12;

    private final BollettaRepository repository;
    private final UtenteCorrente utenteCorrente;

    public BolletteStatisticheService(BollettaRepository repository, UtenteCorrente utenteCorrente) {
        this.repository = repository;
        this.utenteCorrente = utenteCorrente;
    }

    public StatisticheBollette statistiche() {
        LocalDate oggi = LocalDate.now();
        LocalDate finoScorso = oggi.minusYears(1);
        List<Bolletta> tutte = repository
                .findAllByUtenteIdAndDeletedFalseOrderByDataScadenzaAscIdAsc(utenteCorrente.id());

        List<StatisticaTipo> tipi = Arrays.stream(TipoBolletta.values())
                .map(tipo -> perTipo(tipo, tutte, oggi, finoScorso))
                .toList();

        List<CostoUnitario> costi = Arrays.stream(TipoBolletta.values())
                .filter(tipo -> tipo.getUnita() != null)
                .map(tipo -> costo(tipo, tutte))
                .toList();

        return new StatisticheBollette(oggi.getYear(), oggi, finoScorso, tipi, costi);
    }

    /** Dal 1° gennaio a oggi contro lo stesso periodo dell'anno scorso. */
    private StatisticaTipo perTipo(TipoBolletta tipo, List<Bolletta> tutte, LocalDate oggi, LocalDate finoScorso) {
        LocalDate inizioAnno = LocalDate.of(oggi.getYear(), 1, 1);
        LocalDate inizioScorso = inizioAnno.minusYears(1);

        List<Bolletta> correnti = tutte.stream()
                .filter(b -> b.getTipo() == tipo && inPeriodo(b, inizioAnno, oggi))
                .toList();
        List<Bolletta> scorse = tutte.stream()
                .filter(b -> b.getTipo() == tipo && inPeriodo(b, inizioScorso, finoScorso))
                .toList();

        return new StatisticaTipo(
                tipo,
                somma(correnti, false), somma(correnti, true),
                somma(scorse, false), somma(scorse, true),
                correnti.size(), scorse.size());
    }

    /** Costo per unità delle ultime bollette con il consumo indicato. I conguagli sono esclusi. */
    private CostoUnitario costo(TipoBolletta tipo, List<Bolletta> tutte) {
        List<Bolletta> valide = tutte.stream()
                .filter(b -> b.getTipo() == tipo)
                .filter(b -> b.getConsumo() != null && b.getConsumo().signum() > 0)
                .filter(b -> b.getImporto().signum() > 0)
                .filter(b -> b.getNatura() != NaturaBolletta.CONGUAGLIO)
                .sorted(Comparator.comparing(Bolletta::getPeriodoAl).thenComparing(Bolletta::getId))
                .toList();

        List<PuntoCosto> punti = valide.subList(Math.max(0, valide.size() - PUNTI_COSTO), valide.size()).stream()
                .map(b -> new PuntoCosto(
                        YearMonth.from(b.getPeriodoAl()).toString(),
                        b.getImporto().divide(b.getConsumo(), 4, RoundingMode.HALF_UP),
                        b.getImporto(),
                        b.getConsumo(),
                        b.getNatura()))
                .toList();

        return new CostoUnitario(tipo, tipo.getUnita(), punti);
    }

    private boolean inPeriodo(Bolletta b, LocalDate da, LocalDate a) {
        return !b.getPeriodoAl().isBefore(da) && !b.getPeriodoAl().isAfter(a);
    }

    private BigDecimal somma(List<Bolletta> lista, boolean quota) {
        return lista.stream()
                .map(b -> quota ? BollettaMapper.quotaTua(b) : b.getImporto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}