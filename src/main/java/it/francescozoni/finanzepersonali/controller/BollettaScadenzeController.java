// controller/BollettaScadenzeController.java
package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.ImpostazioneResponse;
import it.francescozoni.finanzepersonali.dto.ImpostazioniRequest;
import it.francescozoni.finanzepersonali.dto.NotificaResponse;
import it.francescozoni.finanzepersonali.dto.ScadenzaResponse;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.service.BolletteScadenzeService;
import it.francescozoni.finanzepersonali.service.CanoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bollette")
public class BollettaScadenzeController {

    private final BolletteScadenzeService service;
    private final CanoneService canoneService;
    private final UtenteCorrente utenteCorrente;

    public BollettaScadenzeController(BolletteScadenzeService service,
                                      CanoneService canoneService,
                                      UtenteCorrente utenteCorrente) {
        this.service = service;
        this.canoneService = canoneService;
        this.utenteCorrente = utenteCorrente;
    }

    @GetMapping("/scadenze")
    public List<ScadenzaResponse> scadenze() {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.scadenze();
    }

    @GetMapping("/notifiche")
    public List<NotificaResponse> notifiche() {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.notifiche();
    }

    @GetMapping("/impostazioni")
    public List<ImpostazioneResponse> impostazioni() {
        return service.impostazioni();
    }

    @PutMapping("/impostazioni")
    public List<ImpostazioneResponse> salvaImpostazioni(@Valid @RequestBody ImpostazioniRequest richiesta) {
        return service.salvaImpostazioni(richiesta);
    }
}