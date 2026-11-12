import { Component, inject, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { NotificheService } from '../../core/services/notifiche.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  readonly notifiche = inject(NotificheService);

  navigated = output<void>();

  links = [
    { path: '/dashboard', label: 'Dashboard', icon: 'space_dashboard' },
    { path: '/movimenti', label: 'Movimenti', icon: 'swap_horiz' },
    { path: '/categorie', label: 'Categorie', icon: 'sell' },
    { path: '/bollette', label: 'Bollette', icon: 'receipt_long' }
  ];
}
