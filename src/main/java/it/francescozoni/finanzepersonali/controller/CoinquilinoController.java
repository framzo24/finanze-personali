// CoinquilinoController.java
package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.CoinquilinoResponse;
import it.francescozoni.finanzepersonali.service.BollettaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coinquilini")
public class CoinquilinoController {

    private final BollettaService service;

    public CoinquilinoController(BollettaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CoinquilinoResponse> elenco() {
        return service.coinquilini();
    }
}