import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { ModalitaBollette } from '../models/auth';
import { ProfiloService } from '../services/profilo.service';
import { SessioneService } from '../services/sessione.service';

/** La modalità scelta: dalla sessione se c'è già, altrimenti la chiede al server (sessioni più vecchie). */
function modalitaCorrente(): Observable<ModalitaBollette | null> {
  const sessione = inject(SessioneService);
  const profilo = inject(ProfiloService);

  const nota = sessione.utente()?.modalitaBollette;
  if (nota !== undefined) return of(nota);

  return profilo.carica().pipe(
    tap(utente => sessione.aggiornaUtente(utente)),
    map(utente => utente.modalitaBollette ?? null)
  );
}

/** Le pagine dell'app richiedono di aver scelto la modalità di gestione delle bollette. */
export const modalitaGuard: CanActivateFn = () => {
  const router = inject(Router);
  return modalitaCorrente().pipe(
    map(modalita => (modalita ? true : router.createUrlTree(['/scelta-modalita']))),
    catchError(() => of(true))
  );
};

/** La pagina di scelta ha senso solo finché non si è scelto. */
export const sceltaGuard: CanActivateFn = () => {
  const router = inject(Router);
  return modalitaCorrente().pipe(
    map(modalita => (modalita ? router.createUrlTree(['/dashboard']) : true)),
    catchError(() => of(true))
  );
};
