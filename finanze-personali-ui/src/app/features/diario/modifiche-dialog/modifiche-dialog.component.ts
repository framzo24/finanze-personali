import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export type EsitoModifiche = 'salva' | 'scarta';

@Component({
  selector: 'app-modifiche-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Modifiche non salvate</h2>

    <mat-dialog-content>
      <div class="avviso">
        <mat-icon>edit_note</mat-icon>
        <p>
          Hai scritto qualcosa che non hai ancora salvato.
          <strong>Salva prima di continuare</strong>, altrimenti le modifiche andranno perse.
        </p>
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Resta qui</button>
      <button mat-button class="scarta" [mat-dialog-close]="'scarta'">Scarta le modifiche</button>
      <button mat-flat-button cdkFocusInitial [mat-dialog-close]="'salva'">Salva e continua</button>
    </mat-dialog-actions>
  `,
  styles: `
    .avviso {
      display: flex;
      gap: 14px;
      padding: 14px 16px;
      border-radius: 16px;
      color: var(--app-text);
      background: color-mix(in srgb, var(--app-primary) 10%, transparent);

      mat-icon { flex-shrink: 0; color: var(--app-primary); }
      p { margin: 0; line-height: 1.5; }
      strong { font-weight: 500; }
    }

    .scarta {
      --mat-button-text-label-text-color: var(--app-expense);
    }
  `
})
export class ModificheDialogComponent {}
