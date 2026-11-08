import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, Disponibilita, RegistrazioneRequest, Utente } from '@app/models/auth';
import { SessioneService } from './sessione.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly sessione = inject(SessioneService);
  private readonly api = `${environment.apiUrl}/auth`;

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.api}/login`, { username, password })
      .pipe(tap(risposta => this.sessione.salva(risposta)));
  }

  registra(dati: RegistrazioneRequest): Observable<Utente> {
    return this.http.post<Utente>(`${this.api}/registrazione`, dati);
  }

  disponibilita(nome: string, cognome: string): Observable<Disponibilita> {
    const params = new HttpParams().set('nome', nome).set('cognome', cognome);
    return this.http.get<Disponibilita>(`${this.api}/disponibilita`, { params });
  }
}
