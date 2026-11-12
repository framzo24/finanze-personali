import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Canone, Coinquilino, Tariffa, TariffaEsito } from '../../../core/models/bolletta';
import { BolletteService } from '../../../core/services/bollette.service';
import { TIPO_META } from '../../../core/utils/bollette';

export interface CanoneDialogData {
  canone: Canone;
  coinquilini: Coinquilino[];
}

@Component({
  selector: 'app-canone-dialog',
  imports: [
    CurrencyPipe, DatePipe, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatSlideToggleModule, MatButtonModule, MatIconModule, MatTooltipModule
  ],
  templateUrl: './canone-dialog.component.html',
  styleUrl: './canone-dialog.component.scss'
})
export class CanoneDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(BolletteService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly dialogRef = inject(MatDialogRef<CanoneDialogComponent, boolean>);
  readonly data = inject<CanoneDialogData>(MAT_DIALOG_DATA);

  readonly meta = TIPO_META;
  readonly canone = signal<Canone>(this.data.canone);
  readonly sezione = signal<'prezzo' | 'dettagli'>('prezzo');
  readonly modificato = signal(false);
  readonly esito = signal<string | null>(null);
  readonly invio = signal(false);

  /** Dalla più recente alla più vecchia. */
  readonly tariffe = computed(() => [...this.canone().tariffe].reverse());
  readonly primaId = computed(() => this.canone().tariffe[0]?.id ?? null);

  readonly mesi = Array.from({ length: 12 }, (_, i) => ({
    valore: i + 1,
    label: new Date(2000, i, 1).toLocaleDateString('it-IT', { month: 'long' })
  }));
  private readonly annoCorrente = new Date().getFullYear();
  readonly anni = [this.annoCorrente - 1, this.annoCorrente, this.annoCorrente + 1];

  readonly tariffaForm = this.fb.group({
    importo: [null as number | null, [Validators.required, Validators.min(0.01)]],
    mese: [new Date().getMonth() + 1],
    anno: [this.annoCorrente]
  });

  readonly dettagli = this.fb.group({
    fornitore: [this.data.canone.fornitore, [Validators.required, Validators.maxLength(120)]],
    giornoScadenza: [this.data.canone.giornoScadenza, [Validators.required, Validators.min(1), Validators.max(28)]],
    paganteId: [this.data.canone.paganteId as number | null],
    pagamentoAutomatico: [this.data.canone.pagamentoAutomatico],
    attivo: [this.data.canone.attivo]
  });

  aggiungi(): void {
    if (this.tariffaForm.invalid || this.invio()) {
      this.tariffaForm.markAllAsTouched();
      return;
    }

    const { importo, mese, anno } = this.tariffaForm.getRawValue();
    this.invio.set(true);

    this.service
      .aggiungiTariffa(this.canone().id, {
        importo: Number(importo),
        validaDal: `${anno}-${String(mese).padStart(2, '0')}-01`
      })
      .subscribe({
        next: esito => {
          this.canone.set(esito.canone);
          this.modificato.set(true);
          this.esito.set(this.testoEsito(esito));
          this.tariffaForm.controls.importo.reset(null);
          this.invio.set(false);
        },
        error: () => this.invio.set(false)
      });
  }

  elimina(tariffa: Tariffa): void {
    if (!confirm('Eliminare questo prezzo? Le bollette da pagare tornano al prezzo precedente.')) {
      return;
    }

    this.service.eliminaTariffa(this.canone().id, tariffa.id).subscribe(esito => {
      this.canone.set(esito.canone);
      this.modificato.set(true);
      this.esito.set(this.testoEsito(esito));
    });
  }

  salvaDettagli(): void {
    if (this.dettagli.invalid || this.invio()) {
      this.dettagli.markAllAsTouched();
      return;
    }

    const v = this.dettagli.getRawValue();
    this.invio.set(true);

    this.service
      .modificaCanone(this.canone().id, {
        fornitore: v.fornitore.trim(),
        giornoScadenza: Number(v.giornoScadenza),
        paganteId: v.paganteId ?? null,
        pagamentoAutomatico: v.pagamentoAutomatico,
        attivo: v.attivo
      })
      .subscribe({
        next: canone => {
          this.canone.set(canone);
          this.modificato.set(true);
          this.invio.set(false);
          this.snackBar.open('Dettagli salvati', 'OK', { duration: 3000 });
        },
        error: () => this.invio.set(false)
      });
  }

  chiudi(): void {
    this.dialogRef.close(this.modificato());
  }

  private testoEsito(e: TariffaEsito): string {
    const parti = [
      e.aggiornate === 0
        ? 'Nessuna bolletta da pagare da aggiornare'
        : e.aggiornate === 1
          ? 'Aggiornata 1 bolletta da pagare'
          : `Aggiornate ${e.aggiornate} bollette da pagare`
    ];
    if (e.nonModificate > 0) {
      parti.push(e.nonModificate === 1 ? '1 già pagata, non modificata' : `${e.nonModificate} già pagate, non modificate`);
    }
    return parti.join(' · ');
  }
}
