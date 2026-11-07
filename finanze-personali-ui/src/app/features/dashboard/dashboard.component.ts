import {Component, OnInit, ChangeDetectionStrategy, computed, signal} from '@angular/core';
import { DashboardService } from '@app/core/services/dashboard.service';
import { DashboardResponse } from '@app/models/dashboard-response';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  dashboard = signal<DashboardResponse | undefined>(undefined);
  errore = signal(false);
  readonly today = new Date();

  constructor(
    private dashboardService: DashboardService
  ) {}

  saldoMese = computed(() =>
    (this.dashboard()?.entrateMeseCorrente ?? 0) - (this.dashboard()?.usciteMeseCorrente ?? 0)
  );

  ngOnInit(): void {
    this.dashboardService.getDashboard().subscribe({
      next: response => {
        this.dashboard.set(response);
        this.errore.set(false);
      },
      error: error => {
        this.errore.set(true);
        console.error('Errore caricamento dashboard', error);
      }
    });
  }
}
