import { Component, Inject, OnInit } from '@angular/core';

import {
  FormBuilder,
  FormGroup,
  Validators
} from '@angular/forms';

import {
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';

import { MatSnackBar } from '@angular/material/snack-bar';

import { CategoriaService } from '../../../core/services/categoria.service';

import { CategoriaResponse } from '../../../core/models/categoria-response';
import { CreateCategoriaRequest } from '../../../core/models/create-categoria-request';



@Component({
  selector: 'app-categoria-dialog',
  templateUrl: './categoria-dialog.component.html',
  styleUrls: ['./categoria-dialog.component.scss']
})
export class CategoriaDialogComponent implements OnInit {


  form!: FormGroup;


  categorie: CategoriaResponse[] = [];


  constructor(

    private fb: FormBuilder,

    private categoriaService: CategoriaService,

    private snackBar: MatSnackBar,

    private dialogRef:
    MatDialogRef<CategoriaDialogComponent>,


    @Inject(MAT_DIALOG_DATA)
    public data: CategoriaResponse | null

  ) {}



  ngOnInit(): void {


    this.form = this.fb.group({

      nome: [
        '',
        Validators.required
      ],


      icona: [
        ''
      ],


      colore: [
        '#000000'
      ],


      categoriaPadreId: [

        null

      ]

    });


    this.caricaCategorie();


    if (this.data) {


      this.form.patchValue({

        nome: this.data.nome,

        icona: this.data.icona,

        colore: this.data.colore,

        categoriaPadreId:
        this.data.categoriaPadreId

      });

    }

  }



  caricaCategorie(): void {


    this.categoriaService
      .trovaTuttePiatte()
      .subscribe({

        next: categorie => {

          this.categorie =
            categorie.filter(
              c => !this.data || c.id !== this.data.id
            );

        },

        error: errore => {

          console.error(errore);

          this.snackBar.open(

            'Errore caricamento categorie',

            'OK',

            {

              duration: 4000

            }

          );

        }

      });

  }




  salva(): void {


    if (this.form.invalid) {

      this.form.markAllAsTouched();

      return;

    }



    const request:
      CreateCategoriaRequest =
      this.form.value;



    const chiamata = this.data

      ?

      this.categoriaService.modifica(
        this.data.id,
        request
      )

      :

      this.categoriaService.crea(
        request
      );



    chiamata.subscribe({

      next: categoria => {


        this.snackBar.open(

          this.data
            ?
            'Categoria modificata'
            :
            'Categoria creata',

          'OK',

          {
            duration: 3000
          }

        );


        this.dialogRef.close(categoria);


      },


      error: errore => {


        console.error(
          errore
        );


        this.snackBar.open(

          errore.error?.messages?.[0]
          ??
          'Errore durante il salvataggio',

          'OK',

          {
            duration: 4000
          }

        );


      }


    });


  }




  annulla(): void {

    this.dialogRef.close();

  }

  nomeCategoriaVisualizzato(
    categoria: CategoriaResponse
  ): string {

    if (categoria.categoriaPadreNome) {

      return '↳ ' + categoria.nome;

    }

    return categoria.nome;

  }


}
