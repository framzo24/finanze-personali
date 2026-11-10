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
import { Bolletta, Coinquilino, RiepilogoBollette, RiepilogoTipo, TipoBolletta } from '@app/core/models/bolletta';
import { BolletteService } from '@app/core/services/bollette.service';
import { TIPI, TIPO_META } from '@app/core/utils/bollette';
import { BollettaDialogComponent } from './bolletta-dialog/bolletta-dialog.component';
import { PagaDialogComponent } from './paga-dialog/paga-dialog.component';
import { StoricoDialogComponent } from './storico-dialog/storico-dialog.component';

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
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly tipi = TIPI;
  readonly meta = TIPO_META;
  readonly annoCorrente = new Date().getFullYear();
  readonly anni = [0, 1, 2, 3].map(i => this.annoCorrente - i);
  readonly colonne = ['tipo', 'periodo', 'scadenza', 'importo', 'pagante', 'stato', 'azioni'];

  vista = signal<'casa' | 'mia'>('casa');
  riepilogo = signal<RiepilogoBollette | null>(null);
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
    this.caricaLista();
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
