import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { ThemeService } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'finanze-personali-ui';
  private readonly theme = inject(ThemeService);
}
