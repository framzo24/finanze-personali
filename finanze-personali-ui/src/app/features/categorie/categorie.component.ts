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
  query = signal('');
  aperte = signal<ReadonlySet<number>>(new Set());

  principali = computed(() => this.categorie().length);
  totale = computed(() => this.appiattisci(this.categorie()).length);
  sottocategorie = computed(() => this.totale() - this.principali());

  filtrate = computed(() => {
    const q = this.query().trim().toLowerCase();
    if (!q) return this.categorie();
    return this.categorie().filter(c => this.corrisponde(c, q));
  });

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

  // ---------- schede ----------
  isAperta(categoria: CategoriaResponse): boolean {
    if (this.aperte().has(categoria.id)) return true;
    // durante la ricerca apre le schede che contengono un risultato
    const q = this.query().trim().toLowerCase();
    return !!q && (categoria.sottoCategorie ?? []).some(s => this.corrisponde(s, q));
  }

  toggle(id: number): void {
    this.aperte.update(set => {
      const nuovo = new Set(set);
      nuovo.has(id) ? nuovo.delete(id) : nuovo.add(id);
      return nuovo;
    });
  }

  // ---------- dialog ----------
  nuovaCategoria(): void {
    this.apriDialog(null);
  }

  nuovaSottocategoria(padre: CategoriaResponse): void {
    this.apriDialog({ padreId: padre.id });
    this.aperte.update(set => new Set(set).add(padre.id));
  }

  modifica(categoria: CategoriaResponse): void {
    this.apriDialog(categoria);
  }

  private apriDialog(data: CategoriaResponse | { padreId: number } | null): void {
    this.dialog
      .open(CategoriaDialogComponent, {
        width: '480px',
        maxWidth: '95vw',
        autoFocus: false,
        data
      })
      .afterClosed()
      .subscribe(result => {
        if (result) this.caricaCategorie();
      });
  }

  elimina(categoria: CategoriaResponse): void {
    // il nuovo padre dei figli non può essere la categoria stessa né un suo discendente
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

  // ---------- utilità ----------
  private corrisponde(categoria: CategoriaResponse, q: string): boolean {
    return categoria.nome.toLowerCase().includes(q)
      || (categoria.sottoCategorie ?? []).some(s => this.corrisponde(s, q));
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
