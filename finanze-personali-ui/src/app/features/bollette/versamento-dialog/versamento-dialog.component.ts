import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { TipoBolletta } from '@app/core/models/bolletta';
import { BollettaQuote, QuotaPersona, VersamentoQuote } from '@app/core/models/quote';
import { QuoteService } from '@app/core/services/quote.service';
import { TIPO_META, isoData } from '@app/core/utils/bollette';
import { arrotonda } from '@app/core/utils/quote';

export interface VersamentoDialogData {
  /** Chi invia: undefined o null = tu. */
  coinquilinoId?: number | null;
  /** Mostra solo questa bolletta. */
  bollettaId?: number;
  /** Mostra solo le bollette di questa utenza. */
  tipo?: TipoBolletta | null;
}

interface Riga {
  bolletta: BollettaQuote;
  mancante: number;
  scelta: boolean;
  importo: number | null;
}

@Component({
  selector: 'app-versamento-dialog',
  imports: [
    CurrencyPipe, DatePipe, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatDatepickerModule, MatCheckboxModule, MatSlideToggleModule, MatButtonModule, MatIconModule
  ],
  templateUrl: './versamento-dialog.component.html',
  styleUrl: './versamento-dialog.component.scss'
})
export class VersamentoDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly quote = inject(QuoteService);
  private readonly dialogRef = inject(MatDialogRef<VersamentoDialogComponent, VersamentoQuote[]>);
  readonly data = inject<VersamentoDialogData>(MAT_DIALOG_DATA);

  readonly meta = TIPO_META;
  readonly oggi = new Date();
  readonly caricamento = signal(true);
  readonly invio = signal(false);

  readonly persone = signal<QuotaPersona[]>([]);
  private readonly aperte = signal<BollettaQuote[]>([]);
  readonly righe = signal<Riga[]>([]);

  readonly form = this.fb.group({
    coinquilinoId: [(this.data.coinquilinoId ?? null) as number | null],
    data: [new Date() as Date | null, Validators.required],
    nota: ['', Validators.maxLength(500)],
    registraMovimento: [true]
  });

  private readonly valori = toSignal(this.form.valueChanges, { initialValue: this.form.value });

  readonly sonoIo = computed(() => (this.valori().coinquilinoId ?? null) === null);
  readonly scelte = computed(() => this.righe().filter(r => r.scelta));
  readonly totale = computed(() => this.scelte().reduce((somma, r) => somma + (r.importo ?? 0), 0));
  readonly valido = computed(
    () => this.scelte().length > 0 && this.scelte().every(r => r.importo !== null && r.importo > 0)
  );

  constructor() {
    this.quote.aperte().subscribe({
      next: lista => {
        this.aperte.set(lista);
        this.persone.set(lista[0]?.persone ?? []);
        this.ricalcola();
        this.caricamento.set(false);
      },
      error: () => this.caricamento.set(false)
    });

    this.form.controls.coinquilinoId.valueChanges.subscribe(() => this.ricalcola());
  }

  imposta(indice: number, parziale: Partial<Riga>): void {
    this.righe.update(lista => lista.map((r, i) => (i === indice ? { ...r, ...parziale } : r)));
  }

  importoDigitato(indice: number, valore: number): void {
    this.imposta(indice, { importo: Number.isFinite(valore) ? valore : null });
  }

  salva(): void {
    if (this.form.invalid || !this.valido() || this.invio()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    this.invio.set(true);

    this.quote
      .registra({
        coinquilinoId: v.coinquilinoId,
        data: isoData(v.data as Date),
        nota: v.nota?.trim() || null,
        registraMovimento: v.coinquilinoId === null && v.registraMovimento,
        voci: this.scelte().map(r => ({ bollettaId: r.bolletta.bollettaId, importo: r.importo as number }))
      })
      .subscribe({
        next: registrati => this.dialogRef.close(registrati),
        error: () => this.invio.set(false)
      });
  }

  annulla(): void {
    this.dialogRef.close();
  }

  private ricalcola(): void {
    const chi = this.form.controls.coinquilinoId.value;

    this.righe.set(
      this.aperte()
        .filter(b => this.data.bollettaId === undefined || b.bollettaId === this.data.bollettaId)
        .filter(b => !this.data.tipo || b.tipo === this.data.tipo)
        .map(b => {
          const manca = Math.max(b.persone.find(p => p.coinquilinoId === chi)?.mancante ?? 0, 0);
          const daInviare = manca > 0.005;
          return { bolletta: b, mancante: manca, scelta: daInviare, importo: daInviare ? arrotonda(manca) : null };
        })
    );
  }
}
