package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.DashboardResponse;
import it.francescozoni.finanzepersonali.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse dashboard() {
        return dashboardService.riepilogo();
    }
}
