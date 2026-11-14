import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategoriaTotaleResponse } from '@app/models/dashboard-response';
import { environment } from '../../../environments/environment';
import { StatisticheBollette } from '../models/statistiche';

@Injectable({ providedIn: 'root' })
export class StatisticheService {
  private readonly http = inject(HttpClient);

  /** Le spese per categoria di un mese (mese da 1 a 12). */
  spesePerMese(anno: number, mese: number): Observable<CategoriaTotaleResponse[]> {
    const params = new HttpParams().set('anno', anno).set('mese', mese);
    return this.http.get<CategoriaTotaleResponse[]>(`${environment.apiUrl}/dashboard/spese`, { params });
  }

  bollette(): Observable<StatisticheBollette> {
    return this.http.get<StatisticheBollette>(`${environment.apiUrl}/bollette/statistiche`);
  }
}
