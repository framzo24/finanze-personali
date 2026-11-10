// BollettaController.java
package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.model.enums.StatoBolletta;
import it.francescozoni.finanzepersonali.model.enums.TipoBolletta;
import it.francescozoni.finanzepersonali.security.UtenteCorrente;
import it.francescozoni.finanzepersonali.service.BollettaService;
import it.francescozoni.finanzepersonali.service.CanoneService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/bollette")
public class BollettaController {

    private final BollettaService service;
    private final CanoneService canoneService;
    private final UtenteCorrente utenteCorrente;

    public BollettaController(BollettaService service, CanoneService canoneService, UtenteCorrente utenteCorrente) {
        this.service = service;
        this.canoneService = canoneService;
        this.utenteCorrente = utenteCorrente;
    }

    @GetMapping
    public BollettePageResponse cerca(
            @RequestParam(required = false) Integer anno,
            @RequestParam(required = false) TipoBolletta tipo,
            @RequestParam(required = false) StatoBolletta stato,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int dimensione
    ) {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.cerca(anno, tipo, stato, pagina, dimensione);
    }

    @GetMapping("/riepilogo")
    public RiepilogoBollette riepilogo() {
        canoneService.generaScadenze(utenteCorrente.id());
        return service.riepilogo();
    }

    @GetMapping("/{id}")
    public BollettaResponse trova(@PathVariable Long id) {
        return service.trova(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BollettaResponse crea(@Valid @RequestBody BollettaRequest request) {
        return service.crea(request);
    }

    @PutMapping("/{id}")
    public BollettaResponse modifica(@PathVariable Long id, @Valid @RequestBody BollettaRequest request) {
        return service.modifica(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        service.elimina(id);
    }

    @PostMapping("/{id}/paga")
    public BollettaResponse paga(@PathVariable Long id, @RequestBody PagamentoRequest request) {
        return service.paga(id, request);
    }

    @PostMapping("/storico")
    @ResponseStatus(HttpStatus.CREATED)
    public BatchStoricoResponse caricaStorico(@Valid @RequestBody BatchStoricoRequest request) {
        return service.caricaStorico(request);
    }
}