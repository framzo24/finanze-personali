package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.DiarioRequest;
import it.francescozoni.finanzepersonali.dto.DiarioResponse;
import it.francescozoni.finanzepersonali.service.DiarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diario")
public class DiarioController {

    private final DiarioService service;

    public DiarioController(DiarioService service) {
        this.service = service;
    }

    @GetMapping("/giorni")
    public List<LocalDate> giorni(@RequestParam int anno, @RequestParam int mese) {
        return service.giorniConTesto(anno, mese);
    }

    @GetMapping("/recenti")
    public List<DiarioResponse> recenti(@RequestParam(defaultValue = "10") int limite) {
        return service.recenti(limite);
    }

    @GetMapping("/periodo")
    public List<DiarioResponse> periodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate da,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate a) {
        return service.periodo(da, a);
    }

    @GetMapping("/{giorno}")
    public DiarioResponse giorno(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno) {
        return service.giorno(giorno);
    }

    @PutMapping("/{giorno}")
    public DiarioResponse salva(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giorno,
            @Valid @RequestBody DiarioRequest richiesta) {
        return service.salva(giorno, richiesta);
    }
}