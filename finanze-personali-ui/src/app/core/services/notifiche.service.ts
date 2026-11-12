import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable, Subscription, catchError, of, switchMap, timer } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notifica } from '../models/bolletta';
import { SILENZIOSO } from '../utils/http-context';

const INTERVALLO_MS = 5 * 60 * 1000;

@Injectable({ providedIn: 'root' })
export class NotificheService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/bollette/notifiche`;

  readonly elenco = signal<Notifica[]>([]);
  readonly numero = computed(() => this.elenco().length);

  private sottoscrizione: Subscription | null = null;

  /** Controlla subito e poi ogni 5 minuti. */
  avvia(): void {
    if (this.sottoscrizione) return;
    this.sottoscrizione = timer(0, INTERVALLO_MS)
      .pipe(switchMap(() => this.richiedi()))
      .subscribe(lista => {
        if (lista) this.elenco.set(lista);
      });
  }

  /** Controllo immediato, per esempio dopo aver modificato una bolletta. */
  aggiorna(): void {
    this.richiedi().subscribe(lista => {
      if (lista) this.elenco.set(lista);
    });
  }

  ferma(): void {
    this.sottoscrizione?.unsubscribe();
    this.sottoscrizione = null;
    this.elenco.set([]);
  }

  private richiedi(): Observable<Notifica[] | null> {
    return this.http
      .get<Notifica[]>(this.url, { context: new HttpContext().set(SILENZIOSO, true) })
      .pipe(catchError(() => of(null)));
  }
}
