import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BatchStoricoRequest,
  BatchStoricoResponse,
  Bolletta,
  BollettaRequest,
  BollettePage,
  Coinquilino,
  RiepilogoBollette
} from '../models/bolletta';

export interface FiltriBollette {
  anno: number | null;
  tipo: string;
  stato: string;
}

@Injectable({ providedIn: 'root' })
export class BolletteService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/bollette`;

  cerca(filtri: FiltriBollette, pagina: number, dimensione: number): Observable<BollettePage> {
    let params = new HttpParams().set('pagina', pagina).set('dimensione', dimensione);
    if (filtri.anno) params = params.set('anno', filtri.anno);
    if (filtri.tipo) params = params.set('tipo', filtri.tipo);
    if (filtri.stato) params = params.set('stato', filtri.stato);
    return this.http.get<BollettePage>(this.api, { params });
  }

  riepilogo(): Observable<RiepilogoBollette> {
    return this.http.get<RiepilogoBollette>(`${this.api}/riepilogo`);
  }

  coinquilini(): Observable<Coinquilino[]> {
    return this.http.get<Coinquilino[]>(`${environment.apiUrl}/coinquilini`);
  }

  crea(richiesta: BollettaRequest): Observable<Bolletta> {
    return this.http.post<Bolletta>(this.api, richiesta);
  }

  modifica(id: number, richiesta: BollettaRequest): Observable<Bolletta> {
    return this.http.put<Bolletta>(`${this.api}/${id}`, richiesta);
  }

  elimina(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  paga(id: number, dataPagamento: string, paganteId: number | null): Observable<Bolletta> {
    return this.http.post<Bolletta>(`${this.api}/${id}/paga`, { dataPagamento, paganteId });
  }

  caricaStorico(richiesta: BatchStoricoRequest): Observable<BatchStoricoResponse> {
    return this.http.post<BatchStoricoResponse>(`${this.api}/storico`, richiesta);
  }
}
