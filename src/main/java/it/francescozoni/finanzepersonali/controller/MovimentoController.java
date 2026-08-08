package it.francescozoni.finanzepersonali.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.mapper.MovimentoMapper;
import it.francescozoni.finanzepersonali.service.MovimentoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimenti")
@Slf4j
public class MovimentoController {

    private final MovimentoService movimentoService;
    private final MovimentoMapper movimentoMapper;

    public MovimentoController(MovimentoService movimentoService, MovimentoMapper movimentoMapper) {
        this.movimentoService = movimentoService;
        this.movimentoMapper = movimentoMapper;
    }

    @Operation(
            summary = "Ricerca movimenti",
            description = "Restituisce i movimenti filtrabili per data, categoria, tipo, importo e descrizione"
    )
    @GetMapping("/search")
    public MovimentiPageResponse cerca(
            MovimentoFilter filter,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int dimensione
            ) {
        log.info("Richiesta elenco movimenti");
        return movimentoService.cerca(
                filter,
                pagina,
                dimensione
        );
    }

    @GetMapping("/pagina")
    public MovimentiPageResponse trovaPagina(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int dimensione
    ) {

        log.info(
                "Richiesta movimenti paginati pagina={}, dimensione={}",
                pagina,
                dimensione
        );

        return movimentoService.trovaPaginati(
                pagina,
                dimensione
        );
    }

    @GetMapping("/{id}")
    public MovimentoResponse trovaUno(@PathVariable Long id) {
        log.info("Ricerca movimento id={}", id);
        return movimentoMapper.toResponse(movimentoService.trova(id));
    }

    @Operation(
            summary = "Crea un nuovo movimento",
            description = "Inserisce una nuova entrata o uscita"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentoResponse crea(@Valid @RequestBody CreateMovimentoRequest input) {
        log.info("Creazione movimento");
        return movimentoMapper.toResponse(movimentoService.aggiungi(input));
    }

    @Operation(
            summary = "Modifica un movimento"
    )
    @PutMapping("/{id}")
    public MovimentoResponse modifica(@PathVariable Long id, @Valid @RequestBody UpdateMovimentoRequest input) {
        log.info("Aggiornamento movimento id={}", id);
        return movimentoMapper.toResponse(movimentoService.modifica(id,input));
    }

    @Operation(summary = "Elimina un movimento")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        log.info("Delete movimento id={}", id);
        movimentoService.elimina(id);
    }
}