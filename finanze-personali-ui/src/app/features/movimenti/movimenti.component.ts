import { Component, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { MovimentoService } from '../../core/services/movimento.service';
import { MovimentoResponse } from '../../core/models/movimento-response';
import { MatDialog } from '@angular/material/dialog';
import { MovimentoDialogComponent } from './movimento-dialog/movimento-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MovimentoFilter } from '../../core/models/movimento-filter';
import { CategoriaService } from '../../core/services/categoria.service';
import { CategoriaResponse } from '../../core/models/categoria-response';
import { PageEvent } from '@angular/material/paginator';

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

  // modello dei filtri (legato a ngModel, resta un oggetto normale)
  filtro: MovimentoFilter = {};
  dataDa?: Date;
  dataA?: Date;

  displayedColumns: string[] = ['descrizione', 'categoria', 'tipo', 'importo', 'data', 'azioni'];

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

  cerca(): void {
    this.filtro.dataDa = this.formattaData(this.dataDa, false);
    this.filtro.dataA = this.formattaData(this.dataA, true);
    this.pagina.set(0);
    this.caricaMovimenti();
  }

  resetFiltri(): void {
    this.filtro = {};
    this.dataDa = undefined;
    this.dataA = undefined;
    this.pagina.set(0);
    this.caricaMovimenti();
  }

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
