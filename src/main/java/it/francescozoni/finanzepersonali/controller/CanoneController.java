// controller/CanoneController.java
package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.CanoneRequest;
import it.francescozoni.finanzepersonali.dto.CanoneResponse;
import it.francescozoni.finanzepersonali.dto.TariffaEsito;
import it.francescozoni.finanzepersonali.dto.TariffaRequest;
import it.francescozoni.finanzepersonali.service.CanoneGestioneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canoni")
public class CanoneController {

    private final CanoneGestioneService service;

    public CanoneController(CanoneGestioneService service) {
        this.service = service;
    }

    @GetMapping
    public List<CanoneResponse> elenco() {
        return service.elenco();
    }

    @PutMapping("/{id}")
    public CanoneResponse modifica(@PathVariable Long id, @Valid @RequestBody CanoneRequest richiesta) {
        return service.modifica(id, richiesta);
    }

    @PostMapping("/{id}/tariffe")
    public TariffaEsito aggiungiTariffa(@PathVariable Long id, @Valid @RequestBody TariffaRequest richiesta) {
        return service.aggiungiTariffa(id, richiesta);
    }

    @DeleteMapping("/{id}/tariffe/{tariffaId}")
    public TariffaEsito eliminaTariffa(@PathVariable Long id, @PathVariable Long tariffaId) {
        return service.eliminaTariffa(id, tariffaId);
    }
}