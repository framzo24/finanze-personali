import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import {
  Bolletta,
  Canone,
  Coinquilino,
  RiepilogoBollette,
  RiepilogoTipo, Saldi, SaldoCoinquilino,
  Scadenza,
  Tariffa,
  TipoBolletta
} from '../../core/models/bolletta';
import { BolletteService } from '../../core/services/bollette.service';
import { NotificheService } from '../../core/services/notifiche.service';
import { TIPI, TIPO_META, quotaDi } from '../../core/utils/bollette';
import { BollettaDialogComponent } from './bolletta-dialog/bolletta-dialog.component';
import { CanoneDialogComponent } from './canone-dialog/canone-dialog.component';
import { ImpostazioniDialogComponent } from './impostazioni-dialog/impostazioni-dialog.component';
import { PagaDialogComponent } from './paga-dialog/paga-dialog.component';
import { StoricoDialogComponent } from './storico-dialog/storico-dialog.component';
import {
  SaldoDettaglioDialogComponent
} from "@app/features/bollette/saldo-dettaglio-dialog/saldo-dettaglio-dialog.component";
import {SaldiConfigDialogComponent} from "@app/features/bollette/saldi-config-dialog/saldi-config-dialog.component";
import {RimborsoDialogComponent} from "@app/features/bollette/rimborso-dialog/rimborso-dialog.component";

@Component({
  selector: 'app-bollette',
  imports: [
    CurrencyPipe, DatePipe, FormsModule,
    MatButtonModule, MatFormFieldModule, MatIconModule, MatPaginatorModule,
    MatSelectModule, MatTableModule, MatTooltipModule
  ],
  templateUrl: './bollette.component.html',
  styleUrl: './bollette.component.scss'
})
export class BolletteComponent implements OnInit {
  private readonly service = inject(BolletteService);
  private readonly notifiche = inject(NotificheService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly tipi = TIPI;
  readonly meta = TIPO_META;
  readonly annoCorrente = new Date().getFullYear();
  readonly anni = [0, 1, 2, 3].map(i => this.annoCorrente - i);
  readonly colonne = ['tipo', 'periodo', 'scadenza', 'importo', 'pagante', 'stato', 'azioni'];

  vista = signal<'casa' | 'mia'>('casa');
  riepilogo = signal<RiepilogoBollette | null>(null);
  scadenze = signal<Scadenza[]>([]);
  canoni = signal<Canone[]>([]);
  saldi = signal<Saldi | null>(null);
  coinquilini = signal<Coinquilino[]>([]);
  bollette = signal<Bolletta[]>([]);
  totale = signal(0);
  pagina = signal(0);
  dimensione = signal(20);
  caricamento = signal(true);

  filtroAnno: number | null = null;
  filtroTipo = '';
  filtroStato = '';

  /** Fornitore dell'ultima bolletta di ogni tipo, per precompilare il modulo. */
  private readonly fornitori = computed(() => {
    const mappa: Partial<Record<TipoBolletta, string>> = {};
    for (const t of this.riepilogo()?.tipi ?? []) {
      if (t.ultima) mappa[t.tipo] = t.ultima.fornitore;
    }
    return mappa;
  });

  ngOnInit(): void {
    this.service.coinquilini().subscribe(c => this.coinquilini.set(c));
    this.ricarica();
  }

  ricarica(): void {
    this.service.riepilogo().subscribe(r => this.riepilogo.set(r));
    this.service.scadenze().subscribe(s => this.scadenze.set(s));
    this.service.canoni().subscribe(c => this.canoni.set(c));
    this.service.saldi().subscribe(s => this.saldi.set(s));
    this.caricaLista();
    this.notifiche.aggiorna();
  }

  caricaLista(): void {
    this.service
      .cerca({ anno: this.filtroAnno, tipo: this.filtroTipo, stato: this.filtroStato }, this.pagina(), this.dimensione())
      .subscribe({
        next: p => {
          this.bollette.set(p.contenuto);
          this.totale.set(p.totaleElementi);
          this.caricamento.set(false);
        },
        error: () => this.caricamento.set(false)
      });
  }

  applicaFiltri(): void {
    this.pagina.set(0);
    this.caricaLista();
  }

  resetFiltri(): void {
    this.filtroAnno = null;
    this.filtroTipo = '';
    this.filtroStato = '';
    this.applicaFiltri();
  }

  cambiaPagina(evento: PageEvent): void {
    this.pagina.set(evento.pageIndex);
    this.dimensione.set(evento.pageSize);
    this.caricaLista();
  }

  // ---------- numeri ----------
  valore(importo: number, quota: number): number {
    return this.vista() === 'mia' ? quota : importo;
  }

  totaleAnno(t: RiepilogoTipo): number {
    return this.vista() === 'mia' ? t.totaleAnnoQuota : t.totaleAnno;
  }

  /** Punti della linea dell'andamento (viewBox 100×32). */
  spark(t: RiepilogoTipo): string | null {
    const v = t.andamento.map(p => (this.vista() === 'mia' ? p.quota : p.importo));
    if (v.length < 2) return null;
    const min = Math.min(...v);
    const spazio = Math.max(...v) - min || 1;
    return v.map((y, i) => `${(i / (v.length - 1)) * 100},${28 - ((y - min) / spazio) * 24}`).join(' ');
  }

  statoLabel(b: Bolletta): string {
    return b.stato === 'PAGATA' ? 'Pagata' : b.scaduta ? 'Scaduta' : 'Da pagare';
  }

  statoClasse(b: Bolletta): string {
    return b.stato === 'PAGATA' ? 'ok' : b.scaduta ? 'ko' : 'attesa';
  }

  // ---------- scadenze ----------
  importoScadenza(s: Scadenza): number | null {
    return s.importo === null ? null : this.valore(s.importo, s.quotaTua ?? 0);
  }

  quando(s: Scadenza): string {
    const g = s.giorni;
    if (s.prevista) {
      if (g < 0) return g === -1 ? 'Attesa da ieri' : `Attesa da ${-g} giorni`;
      if (g === 0) return 'Prevista oggi';
      return g === 1 ? 'Prevista domani' : `Prevista tra ${g} giorni`;
    }
    if (g < -1) return `Scaduta da ${-g} giorni`;
    if (g === -1) return 'Scaduta ieri';
    if (g === 0) return 'Scade oggi';
    return g === 1 ? 'Scade domani' : `Scade tra ${g} giorni`;
  }

  pagaScadenza(s: Scadenza): void {
    if (s.bollettaId === null) return;
    this.service.trova(s.bollettaId).subscribe(b => this.paga(b));
  }

  // ---------- canoni ----------
  quotaCanone(c: Canone): number {
    return quotaDi(c.importoAttuale, c.paganteId);
  }

  ultimeTariffe(c: Canone): Tariffa[] {
    return [...c.tariffe].reverse().slice(0, 3);
  }

  apriCanone(canone: Canone): void {
    this.dialog
      .open(CanoneDialogComponent, {
        width: '580px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { canone, coinquilini: this.coinquilini() }
      })
      .afterClosed()
      .subscribe(modificato => {
        if (modificato) this.ricarica();
      });
  }

  impostazioni(): void {
    this.dialog
      .open(ImpostazioniDialogComponent, { width: '600px', maxWidth: '95vw', autoFocus: false })
      .afterClosed()
      .subscribe(salvato => {
        if (salvato) {
          this.snackBar.open('Impostazioni salvate', 'OK', { duration: 3000 });
          this.ricarica();
        }
      });
  }

  // ---------- saldi con i coinquilini ----------
  statoSaldo(p: SaldoCoinquilino): 'ti-deve' | 'gli-devi' | 'pari' {
    if (Math.abs(p.saldo) < 0.005) return 'pari';
    return p.saldo > 0 ? 'ti-deve' : 'gli-devi';
  }

  fraseSaldo(p: SaldoCoinquilino): string {
    const stato = this.statoSaldo(p);
    return stato === 'pari' ? 'Siete in pari' : stato === 'ti-deve' ? 'Ti deve' : 'Gli devi';
  }

  assoluto(n: number): number {
    return Math.abs(n);
  }

  rimborso(persona?: SaldoCoinquilino): void {
    const lista = this.saldi()?.coinquilini ?? [];
    if (lista.length === 0) return;

    this.dialog
      .open(RimborsoDialogComponent, {
        width: '540px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { saldi: lista, coinquilinoId: persona?.coinquilinoId }
      })
      .afterClosed()
      .subscribe(registrato => {
        if (registrato) {
          this.snackBar.open('Rimborso registrato', 'OK', { duration: 3000 });
          this.ricarica();
        }
      });
  }

  dettaglio(persona: SaldoCoinquilino): void {
    this.dialog
      .open(SaldoDettaglioDialogComponent, {
        width: '620px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { persona }
      })
      .afterClosed()
      .subscribe(modificato => {
        if (modificato) this.ricarica();
      });
  }

  configuraSaldi(): void {
    const s = this.saldi();
    this.dialog
      .open(SaldiConfigDialogComponent, {
        width: '520px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { saldiDal: s?.saldiDal ?? null, configurato: s?.configurato ?? false }
      })
      .afterClosed()
      .subscribe(salvato => {
        if (salvato) {
          this.snackBar.open('Conteggio dei saldi aggiornato', 'OK', { duration: 3000 });
          this.ricarica();
        }
      });
  }

  // ---------- azioni ----------
  nuova(tipo?: TipoBolletta): void {
    this.apri({ tipo });
  }

  modifica(bolletta: Bolletta): void {
    this.apri({ bolletta });
  }

  private apri(extra: { bolletta?: Bolletta; tipo?: TipoBolletta }): void {
    this.dialog
      .open(BollettaDialogComponent, {
        width: '600px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { ...extra, coinquilini: this.coinquilini(), fornitori: this.fornitori() }
      })
      .afterClosed()
      .subscribe(salvata => {
        if (salvata) this.ricarica();
      });
  }

  paga(bolletta: Bolletta): void {
    this.dialog
      .open(PagaDialogComponent, {
        width: '440px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { bolletta, coinquilini: this.coinquilini() }
      })
      .afterClosed()
      .subscribe(pagata => {
        if (pagata) {
          this.snackBar.open('Bolletta segnata come pagata', 'OK', { duration: 3000 });
          this.ricarica();
        }
      });
  }

  elimina(bolletta: Bolletta): void {
    if (!confirm(`Eliminare la bolletta ${this.meta[bolletta.tipo].label.toLowerCase()} di ${bolletta.fornitore}?`)) {
      return;
    }
    this.service.elimina(bolletta.id).subscribe(() => {
      this.snackBar.open('Bolletta eliminata', 'OK', { duration: 3000 });
      this.ricarica();
    });
  }

  storico(): void {
    this.dialog
      .open(StoricoDialogComponent, {
        width: '680px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { coinquilini: this.coinquilini(), fornitori: this.fornitori() }
      })
      .afterClosed()
      .subscribe(esito => {
        if (esito) {
          this.snackBar.open(
            esito.saltati > 0
              ? `Caricate ${esito.creati} bollette (${esito.saltati} già presenti, saltate)`
              : `Caricate ${esito.creati} bollette`,
            'OK',
            { duration: 5000 }
          );
          this.ricarica();
        }
      });
  }

  getMeta(tipo: TipoBolletta) {
    return this.meta[tipo];
  }
}
