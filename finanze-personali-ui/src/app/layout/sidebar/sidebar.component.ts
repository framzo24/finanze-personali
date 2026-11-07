import { Component, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatIconModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  navigated = output<void>();

  links = [
    { path: '/dashboard', label: 'Dashboard', icon: 'space_dashboard' },
    { path: '/movimenti', label: 'Movimenti', icon: 'swap_horiz' },
    { path: '/categorie', label: 'Categorie', icon: 'sell' }
    // In futuro: { path: '/aspetto', label: 'Aspetto', icon: 'palette' }
  ];
}
