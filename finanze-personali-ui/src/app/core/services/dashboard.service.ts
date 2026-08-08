import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardResponse } from '../../models/dashboard-response';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(
    private http: HttpClient
  ) {}

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(this.apiUrl);
  }
}
