package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.exception.MovimentoNotFoundException;
import it.francescozoni.finanzepersonali.model.Categoria;
import it.francescozoni.finanzepersonali.model.Movimento;
import it.francescozoni.finanzepersonali.model.TipoMovimento;
import it.francescozoni.finanzepersonali.service.MovimentoService;
import it.francescozoni.finanzepersonali.view.ConsoleView;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class MainController {

    private final ConsoleView view;
    private final MovimentoService service;
    private boolean running = true;

    public MainController(
            ConsoleView view,
            MovimentoService service
    ) {
        this.view = view;
        this.service = service;
    }

    public void start() {
        while (running) {
            view.stampaMenu();
            String scelta = view.leggiInput();
            switch (scelta) {
                case "1":
                    inserisciMovimento();
                    break;
                case "2":
                    view.stampaMovimenti(service.trovaTutti());
                    break;
                case "3":
                    view.stampaMessaggio(
                            "Saldo attuale: "
                                    + service.saldo()
                    );
                    break;
                case "4":
                    view.stampaMessaggio(
                            "Entrate: "
                                    + service.totaleEntrate()
                    );
                    break;
                case "5":
                    view.stampaMessaggio(
                            "Uscite: "
                                    + service.totaleUscite()
                    );
                    break;
                case "6":
                    eliminaMovimento();
                    break;
                case "0":
                    running = false;
                    view.stampaMessaggio("Chiusura programma...");
                    break;
                default:
                    view.stampaMessaggio("Scelta non valida.");
            }
        }
    }

    private void inserisciMovimento() {
        String descrizione = view.leggiStringa("Descrizione: ");
        String importoInput = view.leggiStringa("Importo: ");
        BigDecimal importo = new BigDecimal(importoInput);

        String tipoInput = view.leggiStringa("Tipo (ENTRATA/USCITA): ");
        TipoMovimento tipo = TipoMovimento.valueOf(tipoInput.toUpperCase());

        String categoriaInput = view.leggiStringa("Categoria: ");
        Categoria categoria = Categoria.valueOf(categoriaInput.toUpperCase());

        Movimento movimento = Movimento.builder()
                        .descrizione(descrizione)
                        .importo(importo)
                        .tipo(tipo)
                        .categoria(categoria)
                        .data(LocalDate.now())
                        .build();

        service.aggiungi(movimento);
        view.stampaMovimentoSalvato();
    }

    private void eliminaMovimento() {
        try {
            Long id = view.leggiId();
            service.elimina(id);
            view.stampaMessaggio(
                    "Movimento eliminato correttamente."
            );
        } catch (MovimentoNotFoundException e) {
            view.stampaMessaggio(
                    e.getMessage()
            );
        }
    }
}