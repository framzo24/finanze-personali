import { CanDeactivateFn } from '@angular/router';
import { DiarioComponent } from './diario.component';

/** Prima di lasciare la pagina, controlla che non ci siano modifiche non salvate. */
export const diarioUscitaGuard: CanDeactivateFn<DiarioComponent> = component => component.confermaUscita();
