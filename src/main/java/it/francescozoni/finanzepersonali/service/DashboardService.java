package it.francescozoni.finanzepersonali.service;

import it.francescozoni.finanzepersonali.dto.CategoriaTotaleResponse;
import it.francescozoni.finanzepersonali.dto.DashboardResponse;

import java.util.List;

public interface DashboardService {
    DashboardResponse riepilogo();

    List<CategoriaTotaleResponse> spesePerMese(int anno, int mese);
}
