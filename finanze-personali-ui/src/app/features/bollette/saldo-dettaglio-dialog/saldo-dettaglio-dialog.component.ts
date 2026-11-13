import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SaldoCoinquilino, VoceSaldo } from '../../../core/models/bolletta';
import { BolletteService } from '../../../core/services/bollette.service';
import { TIPO_META } from '../../../core/utils/bollette';

export interface SaldoDettaglioData {
  persona: SaldoCoinquilino;
}

@Component({
  selector: 'app-saldo-dettaglio-dialog',
  imports: [CurrencyPipe, DatePipe, MatDialogModule, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './saldo-dettaglio-dialog.component.html',
  styleUrl: './saldo-dettaglio-dialog.component.scss'
})
export class SaldoDettaglioDialogComponent {
  private readonly service = inject(BolletteService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<SaldoDettaglioDialogComponent, boolean>);
  readonly data = inject<SaldoDettaglioData>(MAT_DIALOG_DATA);

  readonly meta = TIPO_META;
  readonly voci = signal<VoceSaldo[]>([]);
  readonly saldo = signal(this.data.persona.saldo);
  readonly caricamento = signal(true);
  readonly modificato = signal(false);

  readonly stato = computed<'ti-deve' | 'gli-devi' | 'pari'>(() =>
    Math.abs(this.saldo()) < 0.005 ? 'pari' : this.saldo() > 0 ? 'ti-deve' : 'gli-devi'
  );

  readonly frase = computed(() => {
    const nome = this.data.persona.nome;
    return this.stato() === 'pari' ? `Tu e ${nome} siete in pari` : this.stato() === 'ti-deve' ? `${nome} ti deve` : `Devi a ${nome}`;
  });

  constructor() {
    this.carica();
  }

  assoluto(n: number): number {
    return Math.abs(n);
  }

  eliminaRimborso(voce: VoceSaldo): void {
    const testo = voce.movimentoId !== null
      ? 'Eliminare il rimborso? Verrà eliminata anche la spesa collegata nei movimenti.'
      : 'Eliminare il rimborso?';
    if (!confirm(testo)) return;

    this.service.eliminaRimborso(voce.id).subscribe(() => {
      this.modificato.set(true);
      this.snackBar.open('Rimborso eliminato', 'OK', { duration: 3000 });
      this.carica();
    });
  }

  chiudi(): void {
    this.dialogRef.close(this.modificato());
  }

  private carica(): void {
    this.service.vociSaldo(this.data.persona.coinquilinoId).subscribe({
      next: voci => {
        this.voci.set(voci);
        // la prima voce è la più recente: il suo progressivo è il saldo attuale
        this.saldo.set(voci.length > 0 ? voci[0].progressivo : 0);
        this.caricamento.set(false);
      },
      error: () => this.caricamento.set(false)
    });
  }
}
