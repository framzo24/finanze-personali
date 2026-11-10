import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Coinquilino, TipoBolletta } from '../../../core/models/bolletta';
import { BolletteService } from '../../../core/services/bollette.service';
import { TIPI } from '../../../core/utils/bollette';
import { parseStorico } from '../../../core/utils/storico';

export interface StoricoDialogData {
  coinquilini: Coinquilino[];
  fornitori: Partial<Record<TipoBolletta, string>>;
}

@Component({
  selector: 'app-storico-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule
  ],
  templateUrl: './storico-dialog.component.html',
  styleUrl: './storico-dialog.component.scss'
})
export class StoricoDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(BolletteService);
  private readonly dialogRef = inject(MatDialogRef<StoricoDialogComponent>);
  readonly data = inject<StoricoDialogData>(MAT_DIALOG_DATA);

  readonly tipi = TIPI.filter(t => t.tipo !== 'WIFI');
  readonly invio = signal(false);

  readonly form = this.fb.group({
    tipo: ['LUCE' as TipoBolletta, Validators.required],
    fornitore: [this.data.fornitori['LUCE'] ?? '', [Validators.required, Validators.maxLength(120)]],
    paganteId: [null as number | null],
    testo: ['']
  });

  private readonly valori = toSignal(this.form.valueChanges, { initialValue: this.form.value });

  readonly risultato = computed(() =>
    parseStorico(this.valori().testo ?? '', this.data.coinquilini, this.valori().paganteId ?? null)
  );

  readonly errori = computed(() => this.risultato().errori.slice(0, 5));
  readonly altriErrori = computed(() => Math.max(0, this.risultato().errori.length - 5));

  readonly pronto = computed(
    () => this.risultato().righe.length > 0 && this.risultato().errori.length === 0 && this.form.valid
  );

  constructor() {
    this.form.controls.tipo.valueChanges.subscribe(tipo => {
      const proposto = this.data.fornitori[tipo as TipoBolletta];
      if (proposto) this.form.controls.fornitore.setValue(proposto);
    });
  }

  carica(): void {
    if (!this.pronto() || this.invio()) return;

    const v = this.form.getRawValue();
    this.invio.set(true);

    this.service
      .caricaStorico({
        tipo: v.tipo as TipoBolletta,
        fornitore: (v.fornitore ?? '').trim(),
        righe: this.risultato().righe
      })
      .subscribe({
        next: esito => this.dialogRef.close(esito),
        error: () => this.invio.set(false)
      });
  }

  annulla(): void {
    this.dialogRef.close();
  }
}
