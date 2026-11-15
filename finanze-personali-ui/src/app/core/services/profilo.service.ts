import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ModalitaBollette, Utente } from '../models/auth';

@Injectable({ providedIn: 'root' })
export class ProfiloService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/profilo`;

  carica(): Observable<Utente> {
    return this.http.get<Utente>(this.api);
  }

  scegliModalita(modalita: ModalitaBollette): Observable<Utente> {
    return this.http.put<Utente>(`${this.api}/modalita-bollette`, { modalita });
  }
}
