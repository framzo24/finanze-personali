import { Component, ElementRef, afterNextRender, inject, signal, viewChild } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly theme = inject(ThemeService);

  readonly form = this.fb.group({ username: [''], password: [''] });
  readonly mostra = signal(false);
  readonly invio = signal(false);
  readonly scossa = signal(false);

  private readonly campoUsername = viewChild<ElementRef<HTMLInputElement>>('username');
  private readonly campoPassword = viewChild<ElementRef<HTMLInputElement>>('password');

  constructor() {
    // arrivo dalla registrazione: nome utente già compilato, si parte dalla password
    const nuovo = this.route.snapshot.queryParamMap.get('nuovo');
    if (nuovo) {
      this.form.patchValue({ username: nuovo });
    }
    afterNextRender(() => (nuovo ? this.campoPassword() : this.campoUsername())?.nativeElement.focus());
  }

  accedi(): void {
    if (this.invio()) return;
    this.invio.set(true);

    const { username, password } = this.form.getRawValue();
    this.auth.login(username, password).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: () => {
        this.invio.set(false);
        this.scossa.set(true);
        setTimeout(() => this.scossa.set(false), 500);
      }
    });
  }
}
