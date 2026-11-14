import { Component, DestroyRef, NgZone, computed, inject, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { NotificheService } from '../../core/services/notifiche.service';
import { SessioneService } from '../../core/services/sessione.service';
import { ThemeService } from '../../core/services/theme.service';
import { TIPO_META } from '../../core/utils/bollette';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, MatIconModule, MatButtonModule, MatMenuModule, MatBadgeModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  readonly theme = inject(ThemeService);
  readonly sessione = inject(SessioneService);
  readonly notifiche = inject(NotificheService);
  private readonly zone = inject(NgZone);
  readonly meta = TIPO_META;

  showMenuButton = input(false);
  menuToggle = output<void>();

  readonly iniziale = computed(() => (this.sessione.utente()?.nome ?? '?').charAt(0).toUpperCase());

  // ---------- orologio ----------
  readonly adesso = signal(new Date());

  /** "Sabato 14 Novembre" */
  readonly dataEstesa = computed(() =>
    this.maiuscole(this.adesso().toLocaleDateString('it-IT', { weekday: 'long', day: 'numeric', month: 'long' }))
  );

  /** "11:53:14" */
  readonly ora = computed(() =>
    this.adesso().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23' })
  );

  private timer: ReturnType<typeof setTimeout> | undefined;

  constructor() {
    this.notifiche.avvia();
    this.avviaOrologio();

    inject(DestroyRef).onDestroy(() => {
      this.notifiche.ferma();
      clearTimeout(this.timer);
    });
  }

  private avviaOrologio(): void {
    this.zone.runOutsideAngular(() => {
      const tick = () => {
        this.adesso.set(new Date());
        // il prossimo aggiornamento cade subito dopo l'inizio del secondo successivo
        this.timer = setTimeout(tick, 1000 - (Date.now() % 1000) + 5);
      };
      tick();
    });
  }

  private maiuscole(testo: string): string {
    return testo.replace(/\p{L}+/gu, parola => parola.charAt(0).toUpperCase() + parola.slice(1));
  }
}
