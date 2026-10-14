import { Component, OnInit } from '@angular/core';
import { MovimentoService } from '../../core/services/movimento.service';
import { MovimentoResponse } from '../../core/models/movimento-response';
import {MatDialog} from "@angular/material/dialog";
import {MovimentoDialogComponent} from "./movimento-dialog/movimento-dialog.component";
import { MatSnackBar } from '@angular/material/snack-bar';
import {MovimentoFilter} from "../../core/models/movimento-filter";
import {CategoriaService} from "../../core/services/categoria.service";
import {CategoriaResponse} from "../../core/models/categoria-response";

@Component({
  selector: 'app-movimenti',
  templateUrl: './movimenti.component.html',
  styleUrls: ['./movimenti.component.scss']
})
export class MovimentiComponent implements OnInit {

  pagina = 0;
  dimensione = 20;
  totaleElementi = 0;
  filtro: MovimentoFilter = {};
  categorie: CategoriaResponse[] = [];
  dataDa?: Date;
  dataA?: Date;
  displayedColumns: string[] = [
    'descrizione',
    'categoria',
    'tipo',
    'importo',
    'data',
    'azioni'
  ];
  movimenti: MovimentoResponse[] = [];

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
      .cerca(
        this.filtro,
        this.pagina,
        this.dimensione
      )
      .subscribe(response => {
        console.log(response.contenuto);
        this.movimenti = response.contenuto;
        this.totaleElementi = response.totaleElementi;
      });
  }

  cambiaPagina(event: any): void {

    this.pagina = event.pageIndex;

    this.dimensione = event.pageSize;

    this.caricaMovimenti();

  }

  nuovoMovimento(): void {

    const dialogRef = this.dialog.open(
      MovimentoDialogComponent,
      {
        width: '450px'
      }
    );


    dialogRef.afterClosed()
      .subscribe(result => {

        if (result) {

          this.caricaMovimenti();

        }

      });

  }

  modifica(movimento: MovimentoResponse): void {


    const dialogRef =
      this.dialog.open(
        MovimentoDialogComponent,
        {
          width: '500px',

          data: movimento

        }

      );


    dialogRef.afterClosed()
      .subscribe(result => {


        if (result) {

          this.caricaMovimenti();

        }


      });

  }

  cerca(): void {

    this.filtro.dataDa =
      this.formattaData(
        this.dataDa
      );


    this.filtro.dataA =
      this.formattaData(
        this.dataA
      );


    this.pagina = 0;

    this.caricaMovimenti();

  }

  resetFiltri(): void {

    this.filtro = {};

    this.pagina = 0;

    this.caricaMovimenti();

    this.dataDa = undefined;

    this.dataA = undefined;

  }

  caricaCategorie(): void {

    this.categoriaService
      .trovaTutte()
      .subscribe({

        next: categorie => {

          this.categorie = categorie;

        }

      });

  }

  formattaData(data?: Date): string | undefined {

    if (!data) {
      return undefined;
    }

    const anno = data.getFullYear();

    const mese = String(
      data.getMonth() + 1
    ).padStart(2, '0');

    const giorno = String(
      data.getDate()
    ).padStart(2, '0');

    return `${anno}-${mese}-${giorno}`;

  }

  elimina(id: number): void {

    if (!confirm(
      'Sei sicuro di voler eliminare questo movimento?'
    )) {

      return;

    }


    this.movimentoService
      .elimina(id)
      .subscribe({

        next: () => {

          this.snackBar.open(
            'Movimento eliminato',
            'OK',
            {
              duration: 3000
            }
          );


          this.caricaMovimenti();

        },


        error: errore => {

          this.snackBar.open(

            errore.error?.messages?.[0]
            ??
            'Errore durante eliminazione movimento',

            'OK',

            {
              duration: 4000
            }

          );

        }

      });

  }


}
