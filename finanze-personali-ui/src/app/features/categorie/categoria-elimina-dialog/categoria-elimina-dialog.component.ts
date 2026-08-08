import { Component, Inject, OnInit } from '@angular/core';

import {
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';

import { CategoriaResponse } from '../../../core/models/categoria-response';


export interface CategoriaEliminaDialogData {

  categoria: CategoriaResponse;

  categorieDisponibili: CategoriaResponse[];

}


export interface CategoriaEliminaDialogResult {

  azione: 'SPOSTA_FIGLI' | 'ELIMINA_ALBERO';

  nuovaCategoriaPadreId?: number;

}


@Component({
  selector: 'app-categoria-elimina-dialog',
  templateUrl: './categoria-elimina-dialog.component.html',
  styleUrls: ['./categoria-elimina-dialog.component.scss']
})
export class CategoriaEliminaDialogComponent
  implements OnInit {


  azione:
    'SPOSTA_FIGLI'
    | 'ELIMINA_ALBERO'
    = 'ELIMINA_ALBERO';


  nuovaCategoriaPadreId:
    number | null = null;


  constructor(

    private dialogRef:
    MatDialogRef<CategoriaEliminaDialogComponent>,

    @Inject(MAT_DIALOG_DATA)
    public data: CategoriaEliminaDialogData

  ) {}


  ngOnInit(): void {

    if (this.haFigli()) {

      this.azione = 'ELIMINA_ALBERO';

    }

  }


  haFigli(): boolean {

    return !!this.data.categoria.sottoCategorie
      && this.data.categoria.sottoCategorie.length > 0;

  }


  numeroFigli(): number {

    return this.data.categoria.sottoCategorie?.length ?? 0;

  }


  conferma(): void {

    if (
      this.azione === 'SPOSTA_FIGLI'
      && this.nuovaCategoriaPadreId === null
    ) {

      return;

    }


    const result:
      CategoriaEliminaDialogResult = {

      azione: this.azione,

      nuovaCategoriaPadreId:
        this.azione === 'SPOSTA_FIGLI'
          ? this.nuovaCategoriaPadreId ?? undefined
          : undefined

    };


    this.dialogRef.close(result);

  }


  annulla(): void {

    this.dialogRef.close();

  }

}
