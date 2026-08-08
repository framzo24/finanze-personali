import { Component, OnInit } from '@angular/core';

import { NestedTreeControl } from '@angular/cdk/tree';

import {
  MatTreeNestedDataSource
} from '@angular/material/tree';

import { MatDialog } from '@angular/material/dialog';

import { MatSnackBar } from '@angular/material/snack-bar';

import { CategoriaService } from '../../core/services/categoria.service';

import { CategoriaResponse } from '../../core/models/categoria-response';

import {
  CategoriaDialogComponent
} from './categoria-dialog/categoria-dialog.component';

import {
  CategoriaEliminaDialogComponent,
  CategoriaEliminaDialogData
} from './categoria-elimina-dialog/categoria-elimina-dialog.component';


@Component({
  selector: 'app-categorie',
  templateUrl: './categorie.component.html',
  styleUrls: ['./categorie.component.scss']
})
export class CategorieComponent implements OnInit {


  treeControl =
    new NestedTreeControl<CategoriaResponse>(
      categoria => categoria.sottoCategorie
    );


  dataSource =
    new MatTreeNestedDataSource<CategoriaResponse>();


  constructor(

    private categoriaService:
    CategoriaService,

    private dialog:
    MatDialog,

    private snackBar:
    MatSnackBar

  ) {}


  ngOnInit(): void {

    this.caricaCategorie();

  }


  caricaCategorie(): void {

    this.categoriaService
      .trovaTutte()
      .subscribe({

        next: categorie => {

          this.dataSource.data = categorie;

        },

        error: () => {

          this.snackBar.open(
            'Errore caricamento categorie',
            'OK',
            {
              duration: 3000
            }
          );

        }

      });

  }


  hasChild(
    _: number,
    categoria: CategoriaResponse
  ): boolean {

    return !!categoria.sottoCategorie
      && categoria.sottoCategorie.length > 0;

  }


  nuovaCategoria(): void {

    const dialogRef =
      this.dialog.open(
        CategoriaDialogComponent,
        {
          width: '450px',
          data: null
        }
      );


    dialogRef
      .afterClosed()
      .subscribe(result => {

        if (result) {

          this.caricaCategorie();

        }

      });

  }


  modifica(
    categoria: CategoriaResponse
  ): void {

    const dialogRef =
      this.dialog.open(
        CategoriaDialogComponent,
        {
          width: '450px',
          data: categoria
        }
      );


    dialogRef
      .afterClosed()
      .subscribe(result => {

        if (result) {

          this.caricaCategorie();

        }

      });

  }


  elimina(
    categoria: CategoriaResponse
  ): void {


    const categorieDisponibili =
      this.flattenCategorie(
        this.dataSource.data
      )
        .filter(
          c => c.id !== categoria.id
        );


    const data:
      CategoriaEliminaDialogData = {

      categoria,

      categorieDisponibili

    };


    const dialogRef =
      this.dialog.open(
        CategoriaEliminaDialogComponent,
        {
          width: '500px',
          data
        }
      );


    dialogRef
      .afterClosed()
      .subscribe(result => {


        if (!result) {

          return;

        }


        this.categoriaService
          .elimina(
            categoria.id,
            result.azione,
            result.nuovaCategoriaPadreId
          )
          .subscribe({

            next: () => {


              this.snackBar.open(

                result.azione === 'ELIMINA_ALBERO'

                  ? 'Categoria ed eventuali sottocategorie eliminate'

                  : 'Categoria eliminata e sottocategorie spostate',

                'OK',

                {
                  duration: 3000
                }

              );


              this.caricaCategorie();

            },


            error: errore => {


              this.snackBar.open(

                errore.error?.messages?.[0]
                ??
                'Errore durante l\'eliminazione',

                'OK',

                {
                  duration: 4000
                }

              );

            }

          });

      });

  }


  private flattenCategorie(
    categorie: CategoriaResponse[]
  ): CategoriaResponse[] {


    const risultato:
      CategoriaResponse[] = [];


    for (const categoria of categorie) {


      risultato.push(categoria);


      if (
        categoria.sottoCategorie
        && categoria.sottoCategorie.length > 0
      ) {

        risultato.push(
          ...this.flattenCategorie(
            categoria.sottoCategorie
          )
        );

      }

    }


    return risultato;

  }

}
