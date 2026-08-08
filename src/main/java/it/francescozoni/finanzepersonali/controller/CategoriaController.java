package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.*;
import it.francescozoni.finanzepersonali.model.enums.AzioneEliminazioneCategoria;
import it.francescozoni.finanzepersonali.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorie")
@Slf4j
public class CategoriaController {


    private final CategoriaService service;


    public CategoriaController(
            CategoriaService service
    ) {
        this.service = service;
    }


    @GetMapping
    public List<CategoriaResponse> tutte() {
        log.info("Dashboad principale");
        return service.trovaTutte();
    }

    @GetMapping("/all")
    public List<CategoriaResponse> tuttePiatte() {
        return service.trovaTuttePiatte();
    }

    @GetMapping("/{id}")
    public CategoriaResponse una(
            @PathVariable Long id
    ) {
        return service.trova(id);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse crea(
            @Valid @RequestBody CreateCategoriaRequest request
    ) {
        return service.crea(request);
    }


    @PutMapping("/{id}")
    public CategoriaResponse modifica(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoriaRequest request
    ) {
        return service.modifica(id, request);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(
            @PathVariable Long id,
            @RequestParam AzioneEliminazioneCategoria azione,
            @RequestParam(required = false) Long nuovaCategoriaPadreId
    ) {
        service.elimina(
                id,
                azione,
                nuovaCategoriaPadreId
                );
    }
}