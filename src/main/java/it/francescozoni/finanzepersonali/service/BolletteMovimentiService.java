package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.mapper.BollettaMapper;
import it.francescozoni.finanzepersonali.model.Bolletta;
import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.model.Rimborso;
import it.francescozoni.finanzepersonali.model.enums.StatoMovimento;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoMovimento;
import it.francescozoni.finanzepersonali.repository.CategoriaRepository;
import it.francescozoni.finanzepersonali.repository.MovimentoRepository;
import it.francescozoni.finanzepersonali.repository.UtenteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Le spese che le bollette e i rimborsi generano nei tuoi movimenti. */
@Slf4j
@Service
@Transactional
public class BolletteMovimentiService {

    private static final String NOME_PADRE = "Bollette";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CategoriaRepository categorie;
    private final MovimentoRepository movimenti;
    private final UtenteRepository utenti;

    public BolletteMovimentiService(CategoriaRepository categorie,
                                    MovimentoRepository movimenti,
                                    UtenteRepository utenti) {
        this.categorie = categorie;
        this.movimenti = movimenti;
        this.utenti = utenti;
    }

    /** Il movimento con la tua quota di una bolletta che hai pagato tu. Null se la quota è zero. */
    public Movimento creaPerBolletta(Bolletta b) {
        BigDecimal quota = BollettaMapper.quotaTua(b);
        if (quota.signum() == 0) {
            return null;
        }
        Movimento m = Movimento.builder().utente(b.getUtente()).build();
        compila(m, b, quota);
        log.info("Registrata la spesa della bolletta {} {}", b.getTipo(), b.getPeriodoDal());
        return movimenti.save(m);
    }

    public void aggiorna(Movimento m, Bolletta b) {
        compila(m, b, BollettaMapper.quotaTua(b));
        movimenti.save(m);
    }

    /** L'uscita per un rimborso che hai dato tu. Il tipo serve a scegliere la categoria (null = Bollette). */
    public Movimento creaPerRimborso(Rimborso r, TipoBolletta tipo) {
        Movimento m = Movimento.builder()
                .utente(r.getUtente())
                .descrizione("Rimborso a " + r.getCoinquilino().getNome())
                .importo(r.getImporto())
                .tipo(TipoMovimento.USCITA)
                .stato(StatoMovimento.COMPLETATO)
                .dataMovimento(r.getData().atStartOfDay())
                .note(r.getNota())
                .categoria(categoriaPer(r.getUtente().getId(), tipo))
                .build();
        log.info("Registrata l'uscita per il rimborso a {}", r.getCoinquilino().getNome());
        return movimenti.save(m);
    }

    public void elimina(Movimento m) {
        m.setDeleted(true);
        movimenti.save(m);
    }

    // ---------- appoggio ----------

    private void compila(Movimento m, Bolletta b, BigDecimal quota) {
        LocalDate data = b.getDataPagamento() != null ? b.getDataPagamento() : LocalDate.now();

        m.setDescrizione("Bolletta " + b.getTipo().getEtichetta().toLowerCase(Locale.ITALY) + " · " + b.getFornitore());
        m.setImporto(quota.abs());
        // un conguaglio a credito pagato da te è un rientro, non una spesa
        m.setTipo(quota.signum() < 0 ? TipoMovimento.ENTRATA : TipoMovimento.USCITA);
        m.setStato(StatoMovimento.COMPLETATO);
        m.setDataMovimento(data.atStartOfDay());
        m.setNote("La tua quota (un terzo) di " + euro(b.getImporto())
                + " · periodo dal " + b.getPeriodoDal().format(DATA) + " al " + b.getPeriodoAl().format(DATA));
        m.setCategoria(categoriaPer(b.getUtente().getId(), b.getTipo()));
    }

    /** Bollette > Luce / Gas / Acqua / Wi-Fi. Usa le categorie che esistono già, altrimenti le crea. */
    private Categoria categoriaPer(Long utenteId, TipoBolletta tipo) {
        List<Categoria> tutte = categorie.findAllByDeletedFalseAndUtenteId(utenteId);

        Categoria padre = trovaPerNome(tutte, NOME_PADRE);
        if (padre == null) {
            padre = categorie.save(Categoria.builder()
                    .nome(NOME_PADRE)
                    .icona("🧾")
                    .colore("#7C3AED")
                    .utente(utenti.getReferenceById(utenteId))
                    .build());
        }
        if (tipo == null) {
            return padre;
        }

        Categoria figlia = trovaPerNome(tutte, tipo.getEtichetta());
        if (figlia == null) {
            figlia = categorie.save(Categoria.builder()
                    .nome(tipo.getEtichetta())
                    .icona(icona(tipo))
                    .colore(colore(tipo))
                    .categoriaPadre(padre)
                    .utente(utenti.getReferenceById(utenteId))
                    .build());
        }
        return figlia;
    }

    private Categoria trovaPerNome(List<Categoria> lista, String nome) {
        return lista.stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst().orElse(null);
    }

    private String icona(TipoBolletta tipo) {
        return switch (tipo) {
            case LUCE -> "💡";
            case GAS -> "🔥";
            case ACQUA -> "💧";
            case WIFI -> "📶";
        };
    }

    private String colore(TipoBolletta tipo) {
        return switch (tipo) {
            case LUCE -> "#CA8A04";
            case GAS -> "#EA580C";
            case ACQUA -> "#2563EB";
            case WIFI -> "#7C3AED";
        };
    }

    private String euro(BigDecimal valore) {
        return String.format(Locale.ITALY, "%,.2f €", valore);
    }
}