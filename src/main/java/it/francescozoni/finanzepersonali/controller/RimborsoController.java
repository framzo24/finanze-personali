// controller/RimborsoController.java
package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.RimborsoRequest;
import it.francescozoni.finanzepersonali.dto.RimborsoResponse;
import it.francescozoni.finanzepersonali.service.SaldiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rimborsi")
public class RimborsoController {

    private final SaldiService service;

    public RimborsoController(SaldiService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RimborsoResponse crea(@Valid @RequestBody RimborsoRequest richiesta) {
        return service.creaRimborso(richiesta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        service.eliminaRimborso(id);
    }
}