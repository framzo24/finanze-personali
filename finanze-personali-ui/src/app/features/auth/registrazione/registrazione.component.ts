import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, catchError, combineLatest, debounceTime, map, of, startWith, switchMap, tap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { usernameDa } from '../../../core/utils/username';

type StatoUsername = 'vuoto' | 'verifica' | 'libero' | 'occupato' | 'errore';

function uguale(controllo: AbstractControl): ValidationErrors | null {
  const password = controllo.parent?.get('password')?.value;
  return controllo.value === password ? null : { diverse: true };
}

@Component({
  selector: 'app-registrazione',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule],
  templateUrl: './registrazione.component.html',
  styleUrl: './registrazione.component.scss'
})
export class RegistrazioneComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);
  readonly theme = inject(ThemeService);

  readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(60)]],
    cognome: ['', [Validators.required, Validators.maxLength(60)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    conferma: ['', [Validators.required, uguale]]
  });

  readonly mostra = signal(false);
  readonly invio = signal(false);
  readonly anteprima = signal('');
  readonly stato = signal<StatoUsername>('vuoto');

  constructor() {
    const { nome, cognome, password, conferma } = this.form.controls;

    // se cambia la password, la conferma va ricontrollata
    password.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => conferma.updateValueAndValidity());

    // anteprima del nome utente e verifica dal vivo della disponibilità
    combineLatest([
      nome.valueChanges.pipe(startWith(nome.value)),
      cognome.valueChanges.pipe(startWith(cognome.value))
    ])
      .pipe(
        tap(([n, c]) => {
          const utente = usernameDa(n, c);
          this.anteprima.set(utente ?? '');
          this.stato.set(utente ? 'verifica' : 'vuoto');
        }),
        debounceTime(350),
        switchMap(([n, c]): Observable<StatoUsername> => {
          if (!usernameDa(n, c)) return of<StatoUsername>('vuoto');
          return this.auth.disponibilita(n, c).pipe(
            map((r): StatoUsername => (r.disponibile ? 'libero' : 'occupato')),
            catchError(() => of<StatoUsername>('errore'))
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(s => this.stato.set(s));
  }

  get bloccato(): boolean {
    return this.form.invalid || this.invio() || this.stato() === 'occupato' || this.stato() === 'verifica';
  }

  registra(): void {
    if (this.bloccato) {
      this.form.markAllAsTouched();
      return;
    }
    this.invio.set(true);

    const { nome, cognome, password } = this.form.getRawValue();
    this.auth.registra({ nome, cognome, password }).subscribe({
      next: utente => {
        this.snackBar.open('Account creato. Ora puoi accedere.', 'OK', { duration: 4000 });
        this.router.navigate(['/login'], { queryParams: { nuovo: utente.username } });
      },
      error: (errore: HttpErrorResponse) => {
        this.invio.set(false);
        if (errore.status === 409) {
          this.stato.set('occupato');
        }
      }
    });
  }
}
