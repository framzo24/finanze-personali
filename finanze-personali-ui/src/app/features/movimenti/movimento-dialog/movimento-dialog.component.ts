import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MovimentoService} from '../../../core/services/movimento.service';
import {CategoriaService} from '../../../core/services/categoria.service';
import {CategoriaResponse} from '../../../core/models/categoria-response';
import {CreateMovimentoRequest} from '../../../core/models/create-movimento-request';
import {MovimentoResponse} from '../../../core/models/movimento-response';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {CategoriaDialogComponent} from "../../categorie/categoria-dialog/categoria-dialog.component";

interface CategorieFlat extends CategoriaResponse {
  livello: number;
}

@Component({
  selector: 'app-movimento-dialog',
  templateUrl: './movimento-dialog.component.html',
  styleUrls: ['./movimento-dialog.component.scss']
})
export class MovimentoDialogComponent implements OnInit {
  form!: FormGroup;
  categorie: CategoriaResponse[] = [];
  categorieFlat: CategorieFlat[] = [];

  constructor(
    private fb: FormBuilder,
    private movimentoService: MovimentoService,
    private categoriaService: CategoriaService,
    private dialogRef: MatDialogRef<MovimentoDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: MovimentoResponse | null,
    private dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      descrizione: [
        '',
        Validators.required
      ],
      importo: [
        null,
        [
          Validators.required,
          Validators.min(0.01)
        ]
      ],
      tipo: [
        'USCITA',
        Validators.required
      ],
      categoriaId: [
        null,
        Validators.required
      ],
      dataMovimento: [
        new Date(),
        Validators.required
      ]
    });
    this.caricaCategorie();
    if (this.data) {
      this.form.patchValue({
        descrizione: this.data.descrizione,
        importo: this.data.importo,
        tipo: this.data.tipo,
        categoriaId: this.data.categoriaId,
        dataMovimento: [new Date(), Validators.required]
      });
    }
  }

  caricaCategorie(): void {
    this.categoriaService
      .trovaTutte()
      .subscribe({
        next: categorie => {
          this.categorie = categorie;
          this.categorieFlat =
            this.creaListaCategorie(
              categorie
            );
        }
      });
  }

  salva(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valore = this.form.value;
    const movimento: CreateMovimentoRequest = {
      descrizione: valore.descrizione,
      importo: valore.importo,
      tipo: valore.tipo,
      categoriaId: valore.categoriaId,
      dataMovimento: this.formattaData(valore.dataMovimento)
    };
    const chiamata = this.data
      ? this.movimentoService.modifica(
        this.data.id,
        movimento
      )
      : this.movimentoService.crea(movimento);
    console.log('Payload movimento:', movimento);
    chiamata.subscribe({
      next: response => {
        this.dialogRef.close(response);
      }
    });
  }

  annulla(): void {
    this.dialogRef.close();
  }

  nuovaCategoria(): void {
    const dialogRef =
      this.dialog.open(
        CategoriaDialogComponent,
        {
          width: '450px'
        }
      );
    dialogRef.afterClosed()
      .subscribe(result => {
        if (result) {
          this.caricaCategorie();
          this.form.patchValue({
            categoriaId: result.id
          });
        }
      });
  }

  private formattaData(data: Date): string {
    const anno = data.getFullYear();
    const mese = String(data.getMonth() + 1)
      .padStart(2, '0');
    const giorno = String(data.getDate())
      .padStart(2, '0');
    return `${anno}-${mese}-${giorno}T00:00:00`;
  }

  private creaListaCategorie(
    categorie: CategoriaResponse[],
    livello = 0
  ): CategorieFlat[] {
    const risultato: CategorieFlat[] = [];
    categorie.forEach(categoria => {
      risultato.push({
        ...categoria,
        livello
      });
      if (
        categoria.sottoCategorie &&
        categoria.sottoCategorie.length > 0
      ) {
        risultato.push(
          ...this.creaListaCategorie(
            categoria.sottoCategorie,
            livello + 1
          )
        );
      }
    });
    return risultato;
  }
}
