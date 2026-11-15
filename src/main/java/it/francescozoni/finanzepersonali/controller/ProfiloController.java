package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.ModalitaRequest;
import it.francescozoni.finanzepersonali.dto.UtenteResponse;
import it.francescozoni.finanzepersonali.service.ProfiloService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profilo")
public class ProfiloController {

    private final ProfiloService service;

    public ProfiloController(ProfiloService service) {
        this.service = service;
    }

    @GetMapping
    public UtenteResponse profilo() {
        return service.profilo();
    }

    @PutMapping("/modalita-bollette")
    public UtenteResponse scegli(@Valid @RequestBody ModalitaRequest richiesta) {
        return service.scegliModalita(richiesta.modalita());
    }
}