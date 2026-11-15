package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.QuoteDto.*;
import it.francescozoni.finanzepersonali.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    private final QuoteService service;

    public QuoteController(QuoteService service) {
        this.service = service;
    }

    @GetMapping("/prospetto")
    public ProspettoResponse prospetto(@RequestParam(defaultValue = "true") boolean soloAperte) {
        return service.prospetto(soloAperte);
    }

    @GetMapping("/aperte")
    public List<BollettaQuote> aperte() {
        return service.aperte();
    }

    @GetMapping("/bollette")
    public List<BollettaQuote> bollette(@RequestParam List<Long> ids) {
        return service.perBollette(ids);
    }

    @PostMapping("/versamenti")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VersamentoResponse> registra(@Valid @RequestBody VersamentiRequest richiesta) {
        return service.registra(richiesta);
    }

    @DeleteMapping("/versamenti/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        service.elimina(id);
    }
}