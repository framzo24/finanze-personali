// controller/SaldiController.java
package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.ConfigurazioneSaldiRequest;
import it.francescozoni.finanzepersonali.dto.ConfigurazioneSaldiResponse;
import it.francescozoni.finanzepersonali.dto.SaldiResponse;
import it.francescozoni.finanzepersonali.dto.VoceSaldo;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.service.CanoneService;
import it.francescozoni.finanzepersonali.service.SaldiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saldi")
public class SaldiController {

    private final SaldiService service;
    private final CanoneService canoneService;
    private final UtenteCorrente utenteCorrente;

    public SaldiController(SaldiService service, CanoneService canoneService, UtenteCorrente utenteCorrente) {
        this.service = service;
        this.canoneService = canoneService;
        this.utenteCorrente = utenteCorrente;
    }

    @GetMapping
    public SaldiResponse saldi() {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.saldi();
    }

    @GetMapping("/{coinquilinoId}/voci")
    public List<VoceSaldo> voci(@PathVariable Long coinquilinoId) {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.voci(coinquilinoId);
    }

    @PutMapping("/configurazione")
    public ConfigurazioneSaldiResponse configurazione(@RequestBody ConfigurazioneSaldiRequest richiesta) {
        return service.salvaConfigurazione(richiesta);
    }
}