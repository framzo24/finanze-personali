import { Component, Inject, OnInit, ChangeDetectionStrategy, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CategoriaService } from '../../../core/services/categoria.service';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { CreateCategoriaRequest } from '../../../core/models/create-categoria-request';

@Component({
  selector: 'app-categoria-dialog',
  standalone: false,
  templateUrl: './categoria-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./categoria-dialog.component.scss']
})
export class CategoriaDialogComponent implements OnInit {
  form!: FormGroup;
  categorie = signal<CategoriaResponse[]>([]);

  readonly colori = [
    '#7C3AED', '#2563EB', '#0891B2', '#16A34A', '#CA8A04',
    '#EA580C', '#DC2626', '#DB2777', '#475569', '#111111'
  ];

  readonly icone = ['🛒', '🏠', '🚗', '🍽️', '💼', '💊', '🎓', '🎬', '✈️', '💡', '🎁', '💰'];

  constructor(
    private fb: FormBuilder,
    private categoriaService: CategoriaService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<CategoriaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CategoriaResponse | null
  ) {}

  get nome(): string { return this.form.get('nome')?.value ?? ''; }
  get icona(): string { return this.form.get('icona')?.value ?? ''; }
  get colore(): string { return this.form.get('colore')?.value || '#7C3AED'; }

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: ['', Validators.required],
      icona: [''],
      colore: ['#7C3AED'],
      categoriaPadreId: [null]
    });

    this.caricaCategorie();

    if (this.data) {
      this.form.patchValue({
        nome: this.data.nome,
        icona: this.data.icona,
        colore: this.data.colore || '#7C3AED',
        categoriaPadreId: this.data.categoriaPadreId
      });
    }
  }

  caricaCategorie(): void {
    this.categoriaService.trovaTuttePiatte().subscribe({
      next: categorie => {
        // in modifica: niente se stessa né i propri discendenti come padre
        const esclusi = this.data ? this.conDiscendenti(this.data.id, categorie) : new Set<number>();
        this.categorie.set(categorie.filter(c => !esclusi.has(c.id)));
      },
      error: errore => {
        console.error(errore);
        this.snackBar.open('Errore caricamento categorie', 'OK', { duration: 4000 });
      }
    });
  }

  scegliColore(colore: string): void {
    this.form.patchValue({ colore });
  }

  scegliIcona(icona: string): void {
    this.form.patchValue({ icona });
  }

  salva(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request: CreateCategoriaRequest = this.form.value;
    const chiamata = this.data
      ? this.categoriaService.modifica(this.data.id, request)
      : this.categoriaService.crea(request);

    chiamata.subscribe({
      next: categoria => {
        this.snackBar.open(this.data ? 'Categoria modificata' : 'Categoria creata', 'OK', { duration: 3000 });
        this.dialogRef.close(categoria);
      },
      error: errore => {
        console.error(errore);
        this.snackBar.open(errore.error?.messages?.[0] ?? 'Errore durante il salvataggio', 'OK', { duration: 4000 });
      }
    });
  }

  annulla(): void {
    this.dialogRef.close();
  }

  nomeCategoriaVisualizzato(categoria: CategoriaResponse): string {
    return categoria.categoriaPadreNome ? '↳ ' + categoria.nome : categoria.nome;
  }

  private conDiscendenti(id: number, tutte: CategoriaResponse[]): Set<number> {
    const insieme = new Set<number>([id]);
    let aggiunti = true;
    while (aggiunti) {
      aggiunti = false;
      for (const c of tutte) {
        if (c.categoriaPadreId != null && insieme.has(c.categoriaPadreId) && !insieme.has(c.id)) {
          insieme.add(c.id);
          aggiunti = true;
        }
      }
    }
    return insieme;
  }
}
