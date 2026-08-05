import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardResponse } from '../../models/dashboard-response';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {


  dashboard?: DashboardResponse;


  constructor(
    private dashboardService: DashboardService
  ) {}


  ngOnInit(): void {

    this.dashboardService.getDashboard()
      .subscribe({

        next: response => {
          this.dashboard = response;
          console.log('Dashboard ricevuta', response);
        },

        error: error => {
          console.error('Errore caricamento dashboard', error);
        }

      });

  }

}
