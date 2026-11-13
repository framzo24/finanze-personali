import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { DirezioneRimborso, Rimborso, SaldoCoinquilino, TipoBolletta } from '../../../core/models/bolletta';
import { BolletteService } from '@app/core/services/bollette.service';
import { TIPI, isoData } from '@app/core/utils/bollette';

export interface RimborsoDialogData {
  saldi: SaldoCoinquilino[];
  coinquilinoId?: number;
}

function arrotonda(n: number): number {
  return Math.round(n * 100) / 100;
}

@Component({
  selector: 'app-rimborso-dialog',
  imports: [
    CurrencyPipe, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatDatepickerModule, MatSlideToggleModule, MatButtonModule, MatIconModule
  ],
  templateUrl: './rimborso-dialog.component.html',
  styleUrl: './rimborso-dialog.component.scss'
})
export class RimborsoDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly service = inject(BolletteService);
  private readonly dialogRef = inject(MatDialogRef<RimborsoDialogComponent, Rimborso>);
  readonly data = inject<RimborsoDialogData>(MAT_DIALOG_DATA);

  readonly tipi = TIPI;
  readonly invio = signal(false);

  private readonly iniziale =
    this.data.saldi.find(p => p.coinquilinoId === this.data.coinquilinoId) ?? this.data.saldi[0];
  private readonly proposta = this.proponi(this.iniziale);

  readonly form = this.fb.group({
    coinquilinoId: [this.iniziale.coinquilinoId],
    direzione: [this.proposta.direzione as DirezioneRimborso],
    importo: [this.proposta.importo as number | null, [Validators.required, Validators.min(0.01)]],
    data: [new Date() as Date | null, Validators.required],
    nota: ['', Validators.maxLength(500)],
    registraMovimento: [true],
    tipo: [null as TipoBolletta | null]
  });

  readonly valori = toSignal(this.form.valueChanges, { initialValue: this.form.value });

  readonly persona = computed(
    () => this.data.saldi.find(p => p.coinquilinoId === this.valori().coinquilinoId) ?? this.iniziale
  );

  /** Come sarà il saldo dopo questo rimborso. */
  readonly esito = computed(() => {
    const importo = Number(this.valori().importo);
    if (!importo || importo <= 0) return null;

    const base = this.persona().saldo;
    const dopo = arrotonda(this.valori().direzione === 'RICEVUTO' ? base - importo : base + importo);

    if (Math.abs(dopo) < 0.005) return { stato: 'pari' as const, importo: 0 };
    return dopo > 0
      ? { stato: 'ti-deve' as const, importo: dopo }
      : { stato: 'gli-devi' as const, importo: -dopo };
  });

  constructor() {
    // cambiando coinquilino si ripropongono direzione e importo del suo saldo
    this.form.controls.coinquilinoId.valueChanges.subscribe(id => {
      const persona = this.data.saldi.find(p => p.coinquilinoId === id);
      if (persona) {
        const proposta = this.proponi(persona);
        this.form.patchValue({ direzione: proposta.direzione, importo: proposta.importo });
      }
    });
  }

  scegliDirezione(direzione: DirezioneRimborso): void {
    this.form.controls.direzione.setValue(direzione);
  }

  salva(): void {
    if (this.form.invalid || this.invio()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const registra = v.direzione === 'DATO' && v.registraMovimento;

    this.invio.set(true);
    this.service
      .creaRimborso({
        coinquilinoId: Number(v.coinquilinoId),
        direzione: v.direzione,
        importo: Number(v.importo),
        data: isoData(v.data as Date),
        nota: v.nota?.trim() || null,
        registraMovimento: registra,
        tipo: registra ? v.tipo : null
      })
      .subscribe({
        next: rimborso => this.dialogRef.close(rimborso),
        error: () => this.invio.set(false)
      });
  }

  annulla(): void {
    this.dialogRef.close();
  }

  /** Se ti deve dei soldi proponi "mi ha restituito", altrimenti "gli ho restituito". */
  private proponi(p: SaldoCoinquilino): { direzione: DirezioneRimborso; importo: number | null } {
    const assoluto = arrotonda(Math.abs(p.saldo));
    return { direzione: p.saldo > 0 ? 'RICEVUTO' : 'DATO', importo: assoluto > 0 ? assoluto : null };
  }
}
