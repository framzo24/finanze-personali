import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessioneService } from '../services/sessione.service';

/** Le pagine dell'app richiedono di aver fatto l'accesso. */
export const authGuard: CanActivateFn = () => {
  const sessione = inject(SessioneService);
  const router = inject(Router);
  return sessione.autenticato() ? true : router.createUrlTree(['/login']);
};

/** Login e registrazione hanno senso solo se non si è già dentro. */
export const guestGuard: CanActivateFn = () => {
  const sessione = inject(SessioneService);
  const router = inject(Router);
  return sessione.autenticato() ? router.createUrlTree(['/dashboard']) : true;
};
