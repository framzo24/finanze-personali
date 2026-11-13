import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Bolletta, Coinquilino } from '../../../core/models/bolletta';
import { BolletteService } from '../../../core/services/bollette.service';
import { isoData, quotaDi } from '../../../core/utils/bollette';

export interface PagaDialogData {
  bolletta: Bolletta;
  coinquilini: Coinquilino[];
}

@Component({
  selector: 'app-paga-dialog',
  imports: [
    CurrencyPipe, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatDatepickerModule, MatSlideToggleModule, MatButtonModule
  ],
  templateUrl: './paga-dialog.component.html',
  styleUrl: './paga-dialog.component.scss'
})
export class PagaDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(BolletteService);
  private readonly dialogRef = inject(MatDialogRef<PagaDialogComponent>);
  readonly data = inject<PagaDialogData>(MAT_DIALOG_DATA);

  readonly invio = signal(false);

  readonly form = this.fb.group({
    dataPagamento: [new Date() as Date | null, Validators.required],
    paganteId: [this.data.bolletta.paganteId as number | null],
    registraMovimento: [true]
  });

  private readonly valori = toSignal(this.form.valueChanges, { initialValue: this.form.value });

  /** La spesa nei movimenti esiste solo se paghi tu. */
  readonly pagoIo = computed(() => (this.valori().paganteId ?? null) === null);
  readonly quota = computed(() => quotaDi(this.data.bolletta.importo, null));

  conferma(): void {
    if (this.form.invalid || this.invio()) return;

    const { dataPagamento, paganteId, registraMovimento } = this.form.getRawValue();
    this.invio.set(true);

    this.service
      .paga(
        this.data.bolletta.id,
        isoData(dataPagamento as Date),
        paganteId ?? null,
        this.pagoIo() && !!registraMovimento
      )
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: () => this.invio.set(false)
      });
  }

  annulla(): void {
    this.dialogRef.close();
  }
}
