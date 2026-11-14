package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.CategoriaTotaleResponse;
import it.francescozoni.finanzepersonali.dto.DashboardResponse;
import it.francescozoni.finanzepersonali.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return dashboardService.riepilogo();
    }

    @GetMapping("/spese")
    public List<CategoriaTotaleResponse> spese(@RequestParam int anno, @RequestParam int mese) {
        return dashboardService.spesePerMese(anno, mese);
    }
}
