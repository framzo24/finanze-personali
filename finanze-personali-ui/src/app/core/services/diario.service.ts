import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VoceDiario } from '../models/diario';

@Injectable({ providedIn: 'root' })
export class DiarioService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/diario`;

  giorno(giorno: string): Observable<VoceDiario> {
    return this.http.get<VoceDiario>(`${this.api}/${giorno}`);
  }

  salva(giorno: string, testo: string): Observable<VoceDiario> {
    return this.http.put<VoceDiario>(`${this.api}/${giorno}`, { testo });
  }

  /** I giorni del mese (mese da 1 a 12) che hanno un pensiero. */
  giorni(anno: number, mese: number): Observable<string[]> {
    const params = new HttpParams().set('anno', anno).set('mese', mese);
    return this.http.get<string[]>(`${this.api}/giorni`, { params });
  }

  recenti(limite: number): Observable<VoceDiario[]> {
    return this.http.get<VoceDiario[]>(`${this.api}/recenti`, { params: new HttpParams().set('limite', limite) });
  }

  periodo(da: string, a: string): Observable<VoceDiario[]> {
    return this.http.get<VoceDiario[]>(`${this.api}/periodo`, { params: new HttpParams().set('da', da).set('a', a) });
  }
}
