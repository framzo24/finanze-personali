import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
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
  dashboard?: DashboardResponse;
  errore = false;
  readonly today = new Date();

  constructor(
    private dashboardService: DashboardService
  ) {}

  get saldoMese(): number {
    return (this.dashboard?.entrateMeseCorrente ?? 0) - (this.dashboard?.usciteMeseCorrente ?? 0);
  }

  ngOnInit(): void {
    this.dashboardService.getDashboard()
      .subscribe({
        next: response => {
          this.dashboard = response;
          this.errore = false;
        },
        error: error => {
          this.errore = true;
          console.error('Errore caricamento dashboard', error);
        }
      });
  }
}
