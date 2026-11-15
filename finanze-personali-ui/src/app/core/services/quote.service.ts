import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { BollettaQuote, Prospetto, VersamentiRequest, VersamentoQuote } from '../models/quote';

@Injectable({ providedIn: 'root' })
export class QuoteService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/quote`;

  prospetto(soloAperte: boolean): Observable<Prospetto> {
    return this.http.get<Prospetto>(`${this.api}/prospetto`, { params: new HttpParams().set('soloAperte', soloAperte) });
  }

  aperte(): Observable<BollettaQuote[]> {
    return this.http.get<BollettaQuote[]>(`${this.api}/aperte`);
  }

  perBollette(ids: number[]): Observable<BollettaQuote[]> {
    return this.http.get<BollettaQuote[]>(`${this.api}/bollette`, { params: new HttpParams().set('ids', ids.join(',')) });
  }

  registra(richiesta: VersamentiRequest): Observable<VersamentoQuote[]> {
    return this.http.post<VersamentoQuote[]>(`${this.api}/versamenti`, richiesta);
  }

  eliminaVersamento(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/versamenti/${id}`);
  }
}
