import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BollettaQuote, VersamentoQuote } from '@app/core/models/quote';
import { QuoteService } from '@app/core/services/quote.service';
import { TIPO_META } from '@app/core/utils/bollette';
import { STATO_QUOTA } from '@app/core/utils/quote';
import { VersamentoDialogComponent } from '../versamento-dialog/versamento-dialog.component';

@Component({
  selector: 'app-quote-dettaglio-dialog',
  imports: [CurrencyPipe, DatePipe, MatDialogModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './quote-dettaglio-dialog.component.html',
  styleUrl: './quote-dettaglio-dialog.component.scss'
})
export class QuoteDettaglioDialogComponent {
  private readonly quote = inject(QuoteService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<QuoteDettaglioDialogComponent, boolean>);
  readonly data = inject<{ bollettaId: number }>(MAT_DIALOG_DATA);

  readonly meta = TIPO_META;
  readonly stati = STATO_QUOTA;
  readonly bolletta = signal<BollettaQuote | null>(null);
  readonly modificato = signal(false);

  constructor() {
    this.carica();
  }

  registra(): void {
    this.dialog
      .open(VersamentoDialogComponent, {
        width: '600px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { bollettaId: this.data.bollettaId }
      })
      .afterClosed()
      .subscribe(registrati => {
        if (registrati) {
          this.modificato.set(true);
          this.carica();
        }
      });
  }

  elimina(v: VersamentoQuote): void {
    const testo = v.movimentoId !== null
      ? 'Eliminare questo invio? Verrà eliminata anche la spesa collegata nei movimenti.'
      : 'Eliminare questo invio?';
    if (!confirm(testo)) return;

    this.quote.eliminaVersamento(v.id).subscribe(() => {
      this.modificato.set(true);
      this.snackBar.open('Invio eliminato', 'OK', { duration: 3000 });
      this.carica();
    });
  }

  chiudi(): void {
    this.dialogRef.close(this.modificato());
  }

  private carica(): void {
    this.quote.perBollette([this.data.bollettaId]).subscribe(lista => this.bolletta.set(lista[0] ?? null));
  }
}
