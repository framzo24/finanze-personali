import {Component, computed, inject, input, output} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ThemeService } from '@app/core/services/theme.service';
import {MatMenuModule} from "@angular/material/menu";
import {SessioneService} from "@app/core/services/sessione.service";

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatMenuModule
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  readonly theme = inject(ThemeService);
  readonly sessione = inject(SessioneService);

  showMenuButton = input(false);
  menuToggle = output<void>();

  readonly iniziale = computed(() => (this.sessione.utente()?.nome ?? '?').charAt(0).toUpperCase());
}
