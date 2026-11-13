import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Bolletta, Coinquilino, NaturaBolletta, StatoBolletta, TipoBolletta } from '@app/core/models/bolletta';
import { BolletteService } from '@app/core/services/bollette.service';
import { NATURE, TIPI, TIPO_META, daIso, isoData, quotaDi } from '@app/core/utils/bollette';
import {CurrencyPipe} from "@angular/common";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";

export interface BollettaDialogData {
  bolletta?: Bolletta;
  tipo?: TipoBolletta;
  coinquilini: Coinquilino[];
  fornitori: Partial<Record<TipoBolletta, string>>;
}

function importoValido(controllo: AbstractControl): ValidationErrors | null {
  const valore = controllo.value;
  if (valore === null || valore === '' || valore === undefined) return null;
  const natura = controllo.parent?.get('natura')?.value;
  if (natura === 'CONGUAGLIO') return Number(valore) === 0 ? { zero: true } : null;
  return Number(valore) > 0 ? null : { positivo: true };
}

function fineDopoInizio(controllo: AbstractControl): ValidationErrors | null {
  const dal = controllo.parent?.get('periodoDal')?.value as Date | null;
  const al = controllo.value as Date | null;
  return dal && al && al < dal ? { ordine: true } : null;
}

@Component({
  selector: 'app-bolletta-dialog',
  imports: [
    ReactiveFormsModule, CurrencyPipe, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatDatepickerModule, MatButtonModule, MatIconModule, MatSlideToggleModule
  ],
  templateUrl: './bolletta-dialog.component.html',
  styleUrl: './bolletta-dialog.component.scss'
})
export class BollettaDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(BolletteService);
  private readonly dialogRef = inject(MatDialogRef<BollettaDialogComponent>);
  readonly data = inject<BollettaDialogData>(MAT_DIALOG_DATA);

  readonly bolletta = this.data.bolletta ?? null;
  readonly tipi = TIPI;
  readonly nature = NATURE;
  readonly invio = signal(false);

  private fornitoreAutomatico = '';

  readonly form = this.fb.group({
    tipo: [(this.bolletta?.tipo ?? this.data.tipo ?? 'LUCE') as TipoBolletta, Validators.required],
    fornitore: [this.bolletta?.fornitore ?? '', [Validators.required, Validators.maxLength(120)]],
    periodoDal: [this.bolletta ? daIso(this.bolletta.periodoDal) : (null as Date | null), Validators.required],
    periodoAl: [this.bolletta ? daIso(this.bolletta.periodoAl) : (null as Date | null), [Validators.required, fineDopoInizio]],
    dataScadenza: [this.bolletta ? daIso(this.bolletta.dataScadenza) : (null as Date | null), Validators.required],
    dataEmissione: [this.bolletta?.dataEmissione ? daIso(this.bolletta.dataEmissione) : (null as Date | null)],
    importo: [this.bolletta?.importo ?? (null as number | null), [Validators.required, importoValido]],
    consumo: [this.bolletta?.consumo ?? (null as number | null), Validators.min(0)],
    natura: [(this.bolletta?.natura ?? 'EFFETTIVA') as NaturaBolletta],
    numeroDocumento: [this.bolletta?.numeroDocumento ?? '', Validators.maxLength(60)],
    paganteId: [this.bolletta?.paganteId ?? (null as number | null)],
    stato: [(this.bolletta?.stato ?? 'DA_PAGARE') as StatoBolletta],
    dataPagamento: [this.bolletta?.dataPagamento ? daIso(this.bolletta.dataPagamento) : (null as Date | null)],
    note: [this.bolletta?.note ?? '', Validators.maxLength(1000)],
    registraMovimento: [!this.bolletta || this.bolletta.stato === 'DA_PAGARE'],
  });

  private readonly valori = toSignal(this.form.valueChanges, { initialValue: this.form.value });

  readonly unita = computed(() => TIPO_META[this.valori().tipo ?? 'LUCE'].unita);

  /** La quota che ti spetta, calcolata mentre scrivi. */
  readonly quota = computed(() => {
    const importo = this.valori().importo;
    if (importo === null || importo === undefined || Number.isNaN(Number(importo))) return null;
    return quotaDi(Number(importo), this.valori().paganteId ?? null);
  });

  readonly paganteNome = computed(() => {
    const id = this.valori().paganteId ?? null;
    return id === null ? 'Tu' : this.data.coinquilini.find(c => c.id === id)?.nome ?? '';
  });

  /** Cosa succede alla spesa nei movimenti: solo se la bolletta è pagata da te. */
  readonly movimento = computed<'crea' | 'aggiorna' | 'elimina' | null>(() => {
    const v = this.valori();
    const dovuta = v.stato === 'PAGATA' && (v.paganteId ?? null) === null;
    const esiste = !!this.bolletta?.movimentoId;
    if (esiste) return dovuta ? 'aggiorna' : 'elimina';
    return dovuta ? 'crea' : null;
  });

  constructor() {
    const { tipo, natura, periodoDal, stato, dataPagamento } = this.form.controls;

    if (!this.bolletta) {
      this.riempiFornitore();
    }

    tipo.valueChanges.subscribe(() => this.riempiFornitore());
    natura.valueChanges.subscribe(() => this.form.controls.importo.updateValueAndValidity());
    periodoDal.valueChanges.subscribe(() => this.form.controls.periodoAl.updateValueAndValidity());
    stato.valueChanges.subscribe(s => {
      if (s === 'PAGATA' && !dataPagamento.value) {
        dataPagamento.setValue(new Date());
      }
    });
  }

  get tipoScelto(): TipoBolletta {
    return this.form.controls.tipo.value ?? 'LUCE';
  }

  scegliTipo(tipo: TipoBolletta): void {
    this.form.controls.tipo.setValue(tipo);
  }

  scegliStato(stato: StatoBolletta): void {
    this.form.controls.stato.setValue(stato);
  }

  /** Propone il fornitore dell'ultima bolletta dello stesso tipo, se non l'hai già scritto tu. */
  private riempiFornitore(): void {
    const campo = this.form.controls.fornitore;
    if (campo.value && campo.value !== this.fornitoreAutomatico) return;
    this.fornitoreAutomatico = this.data.fornitori[this.tipoScelto] ?? '';
    campo.setValue(this.fornitoreAutomatico);
  }

  salva(): void {
    if (this.form.invalid || this.invio()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const pagata = v.stato === 'PAGATA';

    const richiesta = {
      tipo: v.tipo as TipoBolletta,
      fornitore: (v.fornitore ?? '').trim(),
      periodoDal: isoData(v.periodoDal as Date),
      periodoAl: isoData(v.periodoAl as Date),
      dataEmissione: v.dataEmissione ? isoData(v.dataEmissione) : null,
      dataScadenza: isoData(v.dataScadenza as Date),
      importo: Number(v.importo),
      consumo: v.tipo === 'WIFI' || v.consumo === null || v.consumo === undefined ? null : Number(v.consumo),
      numeroDocumento: v.numeroDocumento?.trim() || null,
      natura: v.natura as NaturaBolletta,
      stato: v.stato as StatoBolletta,
      dataPagamento: pagata ? isoData((v.dataPagamento as Date | null) ?? new Date()) : null,
      paganteId: v.paganteId ?? null,
      note: v.note?.trim() || null,
      registraMovimento: this.movimento() === 'crea' ? !!v.registraMovimento : null
    };

    this.invio.set(true);
    const chiamata = this.bolletta
      ? this.service.modifica(this.bolletta.id, richiesta)
      : this.service.crea(richiesta);

    chiamata.subscribe({
      next: salvata => this.dialogRef.close(salvata),
      error: () => this.invio.set(false)
    });
  }

  annulla(): void {
    this.dialogRef.close();
  }
}
