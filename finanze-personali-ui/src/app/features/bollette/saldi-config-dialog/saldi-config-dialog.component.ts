import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { BolletteService } from '../../../core/services/bollette.service';
import { daIso, isoData } from '../../../core/utils/bollette';

export interface SaldiConfigData {
  saldiDal: string | null;
  configurato: boolean;
}

@Component({
  selector: 'app-saldi-config-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatRadioModule, MatFormFieldModule, MatInputModule,
    MatDatepickerModule, MatButtonModule
  ],
  templateUrl: './saldi-config-dialog.component.html',
  styleUrl: './saldi-config-dialog.component.scss'
})
export class SaldiConfigDialogComponent {
  private readonly service = inject(BolletteService);
  private readonly dialogRef = inject(MatDialogRef<SaldiConfigDialogComponent, boolean>);
  readonly data = inject<SaldiConfigData>(MAT_DIALOG_DATA);

  readonly modo = signal<'data' | 'tutto'>(this.data.configurato && !this.data.saldiDal ? 'tutto' : 'data');
  readonly dataCtrl = new FormControl<Date | null>(this.data.saldiDal ? daIso(this.data.saldiDal) : new Date());
  readonly invio = signal(false);

  private readonly valore = toSignal(this.dataCtrl.valueChanges, { initialValue: this.dataCtrl.value });
  readonly valido = computed(() => this.modo() === 'tutto' || !!this.valore());

  imposta(scelta: 'oggi' | 'mese'): void {
    const oggi = new Date();
    this.dataCtrl.setValue(scelta === 'oggi' ? oggi : new Date(oggi.getFullYear(), oggi.getMonth(), 1));
  }

  salva(): void {
    if (!this.valido() || this.invio()) return;

    const dal = this.modo() === 'tutto' ? null : isoData(this.dataCtrl.value as Date);
    this.invio.set(true);

    this.service.salvaConfigurazione(dal).subscribe({
      next: () => this.dialogRef.close(true),
      error: () => this.invio.set(false)
    });
  }

  annulla(): void {
    this.dialogRef.close(false);
  }
}
