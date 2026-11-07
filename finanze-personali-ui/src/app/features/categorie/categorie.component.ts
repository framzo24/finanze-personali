import { Component, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoriaService } from '../../core/services/categoria.service';
import { CategoriaResponse } from '../../core/models/categoria-response';
import { CategoriaDialogComponent } from './categoria-dialog/categoria-dialog.component';
import {
  CategoriaEliminaDialogComponent,
  CategoriaEliminaDialogData
} from './categoria-elimina-dialog/categoria-elimina-dialog.component';

@Component({
  selector: 'app-categorie',
  standalone: false,
  templateUrl: './categorie.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./categorie.component.scss']
})
export class CategorieComponent implements OnInit {

  categorie = signal<CategoriaResponse[]>([]);
  caricamento = signal(true);
  totale = computed(() => this.appiattisci(this.categorie()).length);

  readonly childrenAccessor = (c: CategoriaResponse) => c.sottoCategorie ?? [];
  readonly expansionKey = (c: CategoriaResponse) => c.id;
  readonly hasChild = (_: number, c: CategoriaResponse) => !!c.sottoCategorie?.length;

  constructor(
    private categoriaService: CategoriaService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.caricaCategorie();
  }

  caricaCategorie(): void {
    this.categoriaService.trovaTutte().subscribe({
      next: categorie => {
        this.categorie.set(categorie);
        this.caricamento.set(false);
      },
      error: () => {
        this.caricamento.set(false);
        this.snackBar.open('Errore caricamento categorie', 'OK', { duration: 3000 });
      }
    });
  }

  nuovaCategoria(): void {
    this.apriDialog(null);
  }

  modifica(categoria: CategoriaResponse): void {
    this.apriDialog(categoria);
  }

  private apriDialog(categoria: CategoriaResponse | null): void {
    this.dialog
      .open(CategoriaDialogComponent, {
        width: '480px',
        maxWidth: '95vw',
        autoFocus: false,
        data: categoria
      })
      .afterClosed()
      .subscribe(result => {
        if (result) this.caricaCategorie();
      });
  }

  elimina(categoria: CategoriaResponse): void {
    // La nuova categoria padre non può essere la categoria stessa né un suo discendente
    const esclusi = new Set(this.appiattisci([categoria]).map(c => c.id));
    const categorieDisponibili = this.appiattisci(this.categorie()).filter(c => !esclusi.has(c.id));

    const data: CategoriaEliminaDialogData = { categoria, categorieDisponibili };

    this.dialog
      .open(CategoriaEliminaDialogComponent, {
        width: '520px',
        maxWidth: '95vw',
        autoFocus: false,
        data
      })
      .afterClosed()
      .subscribe(result => {
        if (!result) return;

        this.categoriaService
          .elimina(categoria.id, result.azione, result.nuovaCategoriaPadreId)
          .subscribe({
            next: () => {
              this.snackBar.open(
                result.azione === 'ELIMINA_ALBERO'
                  ? 'Categoria ed eventuali sottocategorie eliminate'
                  : 'Categoria eliminata e sottocategorie spostate',
                'OK',
                { duration: 3000 }
              );
              this.caricaCategorie();
            },
            error: errore => {
              this.snackBar.open(
                errore.error?.messages?.[0] ?? 'Errore durante l\'eliminazione',
                'OK',
                { duration: 4000 }
              );
            }
          });
      });
  }

  private appiattisci(categorie: CategoriaResponse[]): CategoriaResponse[] {
    const risultato: CategoriaResponse[] = [];
    for (const categoria of categorie) {
      risultato.push(categoria);
      if (categoria.sottoCategorie?.length) {
        risultato.push(...this.appiattisci(categoria.sottoCategorie));
      }
    }
    return risultato;
  }
}
