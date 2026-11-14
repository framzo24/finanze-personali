import { Component, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { MovimentoService } from '@app/core/services/movimento.service';
import { MovimentoResponse } from '@app/core/models/movimento-response';
import { MatDialog } from '@angular/material/dialog';
import { MovimentoDialogComponent } from './movimento-dialog/movimento-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MovimentoFilter } from '@app/core/models/movimento-filter';
import { CategoriaService } from '@app/core/services/categoria.service';
import { CategoriaResponse } from '@app/core/models/categoria-response';
import { PageEvent } from '@angular/material/paginator';

interface VoceSotto {
  id: number;
  nome: string;
  icona: string;
  livello: number;
  percorso: string;
  radiceId: number;
}

@Component({
  selector: 'app-movimenti',
  standalone: false,
  templateUrl: './movimenti.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./movimenti.component.scss']
})
export class MovimentiComponent implements OnInit {

  // stato della vista: signal
  pagina = signal(0);
  dimensione = signal(20);
  totaleElementi = signal(0);
  movimenti = signal<MovimentoResponse[]>([]);
  categorie = signal<CategoriaResponse[]>([]);

  // filtri
  filtro: MovimentoFilter = {};
  categoriaId = signal<number | null>(null);
  sottocategoriaId = signal<number | null>(null);
  dataDa?: Date;
  dataA?: Date;

  displayedColumns: string[] = ['descrizione', 'categoria', 'tipo', 'importo', 'data', 'azioni'];

  /** Tutte le sottocategorie, a ogni livello, con il loro percorso. */
  private readonly tutteSotto = computed<VoceSotto[]>(() => {
    const risultato: VoceSotto[] = [];
    const visita = (lista: CategoriaResponse[], livello: number, percorso: string[], radiceId: number) => {
      for (const c of lista) {
        const p = [...percorso, c.nome];
        risultato.push({ id: c.id, nome: c.nome, icona: c.icona ?? '', livello, percorso: p.join(' › '), radiceId });
        visita(c.sottoCategorie ?? [], livello + 1, p, radiceId);
      }
    };
    for (const radice of this.categorie()) {
      visita(radice.sottoCategorie ?? [], 1, [radice.nome], radice.id);
    }
    return risultato;
  });

  /** Le sottocategorie della categoria scelta, oppure tutte se non ne hai scelta una. */
  readonly sottocategorie = computed(() => {
    const categoria = this.categoriaId();
    return categoria === null ? this.tutteSotto() : this.tutteSotto().filter(s => s.radiceId === categoria);
  });

  constructor(
    private movimentoService: MovimentoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private categoriaService: CategoriaService
  ) {}

  ngOnInit(): void {
    this.caricaMovimenti();
    this.caricaCategorie();
  }

  caricaMovimenti(): void {
    this.movimentoService
      .cerca(this.filtro, this.pagina(), this.dimensione())
      .subscribe(response => {
        this.movimenti.set(response.contenuto);
        this.totaleElementi.set(response.totaleElementi);
      });
  }

  caricaCategorie(): void {
    this.categoriaService.trovaTutte().subscribe({
      next: categorie => this.categorie.set(categorie)
    });
  }

  cambiaPagina(event: PageEvent): void {
    this.pagina.set(event.pageIndex);
    this.dimensione.set(event.pageSize);
    this.caricaMovimenti();
  }

  // ---------- filtri ----------
  scegliCategoria(id: number | null): void {
    this.categoriaId.set(id);

    // una sottocategoria che non appartiene alla nuova categoria si azzera
    const sotto = this.sottocategoriaId();
    if (id !== null && sotto !== null) {
      const voce = this.tutteSotto().find(s => s.id === sotto);
      if (!voce || voce.radiceId !== id) {
        this.sottocategoriaId.set(null);
      }
    }
  }

  scegliSottocategoria(id: number | null): void {
    this.sottocategoriaId.set(id);

    // scegliendo una sottocategoria senza categoria, la categoria diventa la sua madre principale
    if (id !== null && this.categoriaId() === null) {
      const voce = this.tutteSotto().find(s => s.id === id);
      if (voce) this.categoriaId.set(voce.radiceId);
    }
  }

  cerca(): void {
    this.filtro.categoriaId = this.categoriaId() ?? undefined;
    this.filtro.sottocategoriaId = this.sottocategoriaId() ?? undefined;
    this.filtro.dataDa = this.formattaData(this.dataDa, false);
    this.filtro.dataA = this.formattaData(this.dataA, true);
    this.pagina.set(0);
    this.caricaMovimenti();
  }

  resetFiltri(): void {
    this.filtro = {};
    this.categoriaId.set(null);
    this.sottocategoriaId.set(null);
    this.dataDa = undefined;
    this.dataA = undefined;
    this.pagina.set(0);
    this.caricaMovimenti();
  }

  // ---------- azioni ----------
  nuovoMovimento(): void {
    const dialogRef = this.dialog.open(MovimentoDialogComponent, {
      width: '520px',
      maxWidth: '95vw',
      autoFocus: false
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.caricaMovimenti();
    });
  }

  modifica(movimento: MovimentoResponse): void {
    const dialogRef = this.dialog.open(MovimentoDialogComponent, {
      width: '520px',
      maxWidth: '95vw',
      autoFocus: false,
      data: movimento
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.caricaMovimenti();
    });
  }

  elimina(id: number): void {
    if (!confirm('Sei sicuro di voler eliminare questo movimento?')) {
      return;
    }

    this.movimentoService.elimina(id).subscribe({
      next: () => {
        this.snackBar.open('Movimento eliminato', 'OK', { duration: 3000 });
        this.caricaMovimenti();
      },
      error: errore => {
        this.snackBar.open(
          errore.error?.messages?.[0] ?? 'Errore durante eliminazione movimento',
          'OK',
          { duration: 4000 }
        );
      }
    });
  }

  formattaData(data?: Date, fineGiornata = false): string | undefined {
    if (!data) return undefined;
    const anno = data.getFullYear();
    const mese = String(data.getMonth() + 1).padStart(2, '0');
    const giorno = String(data.getDate()).padStart(2, '0');
    return `${anno}-${mese}-${giorno}T${fineGiornata ? '23:59:59' : '00:00:00'}`;
  }
}
