import { Component, DestroyRef, computed, inject, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { NotificheService } from '@app/core/services/notifiche.service';
import { SessioneService } from '@app/core/services/sessione.service';
import { ThemeService } from '@app/core/services/theme.service';
import { TIPO_META } from '@app/core/utils/bollette';

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
  readonly meta = TIPO_META;

  showMenuButton = input(false);
  menuToggle = output<void>();

  readonly iniziale = computed(() => (this.sessione.utente()?.nome ?? '?').charAt(0).toUpperCase());

  constructor() {
    this.notifiche.avvia();
    inject(DestroyRef).onDestroy(() => this.notifiche.ferma());
  }
}
