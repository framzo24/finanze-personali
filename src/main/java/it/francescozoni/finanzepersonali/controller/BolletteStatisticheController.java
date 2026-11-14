package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.StatisticheBollette;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.service.BolletteStatisticheService;
import it.francescozoni.finanzepersonali.service.CanoneService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bollette/statistiche")
public class BolletteStatisticheController {

    private final BolletteStatisticheService service;
    private final CanoneService canoneService;
    private final UtenteCorrente utenteCorrente;

    public BolletteStatisticheController(BolletteStatisticheService service,
                                         CanoneService canoneService,
                                         UtenteCorrente utenteCorrente) {
        this.service = service;
        this.canoneService = canoneService;
        this.utenteCorrente = utenteCorrente;
    }

    @GetMapping
    public StatisticheBollette statistiche() {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.statistiche();
    }
}