import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Impostazione, TipoBolletta } from '../../../core/models/bolletta';
import { BolletteService } from '../../../core/services/bollette.service';
import { TIPO_META } from '../../../core/utils/bollette';

@Component({
  selector: 'app-impostazioni-dialog',
  imports: [MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule],
  templateUrl: './impostazioni-dialog.component.html',
  styleUrl: './impostazioni-dialog.component.scss'
})
export class ImpostazioniDialogComponent {
  private readonly service = inject(BolletteService);
  private readonly dialogRef = inject(MatDialogRef<ImpostazioniDialogComponent, boolean>);

  readonly meta = TIPO_META;
  readonly righe = signal<Impostazione[]>([]);
  readonly caricamento = signal(true);
  readonly invio = signal(false);

  readonly valido = computed(() =>
    this.righe().every(r => Number.isInteger(r.preavvisoGiorni) && r.preavvisoGiorni >= 0 && r.preavvisoGiorni <= 30)
  );

  constructor() {
    this.service.impostazioni().subscribe({
      next: righe => {
        this.righe.set(righe);
        this.caricamento.set(false);
      },
      error: () => this.caricamento.set(false)
    });
  }

  imposta(tipo: TipoBolletta, campo: 'periodicitaMesi' | 'preavvisoGiorni', valore: number): void {
    this.righe.update(lista => lista.map(r => (r.tipo === tipo ? { ...r, [campo]: valore } : r)));
  }

  salva(): void {
    if (!this.valido() || this.invio()) return;
    this.invio.set(true);

    this.service.salvaImpostazioni(this.righe()).subscribe({
      next: () => this.dialogRef.close(true),
      error: () => this.invio.set(false)
    });
  }

  annulla(): void {
    this.dialogRef.close(false);
  }
}
