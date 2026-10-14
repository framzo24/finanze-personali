package it.francescozoni.finanzepersonali.controller;

import it.francescozoni.finanzepersonali.dto.DashboardResponse;
import it.francescozoni.finanzepersonali.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService service;
    @InjectMocks
    private DashboardController controller;
    @Mock
    private DashboardResponse dashboardResponse;

    @Test
    void getDashboard() {
        when(service.riepilogo()).thenReturn(dashboardResponse);

        DashboardResponse response = controller.dashboard();
        assertEquals(dashboardResponse, response);
    }

}