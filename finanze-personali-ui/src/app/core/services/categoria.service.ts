import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { CategoriaResponse } from '../models/categoria-response';
import { CreateCategoriaRequest } from '../models/create-categoria-request';
import { UpdateCategoriaRequest } from '../models/update-categoria-request';

export type AzioneEliminazioneCategoria =
  'SPOSTA_FIGLI'
  | 'ELIMINA_ALBERO';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

  private readonly api =
    `${environment.apiUrl}/categorie`;


  constructor(
    private http: HttpClient
  ) {}


  trovaTutte(): Observable<CategoriaResponse[]> {

    return this.http.get<CategoriaResponse[]>(
      this.api
    );

  }


  trovaUna(
    id: number
  ): Observable<CategoriaResponse> {

    return this.http.get<CategoriaResponse>(
      `${this.api}/${id}`
    );

  }


  crea(
    request: CreateCategoriaRequest
  ): Observable<CategoriaResponse> {

    return this.http.post<CategoriaResponse>(
      this.api,
      request
    );

  }


  modifica(
    id: number,
    request: UpdateCategoriaRequest
  ): Observable<CategoriaResponse> {

    return this.http.put<CategoriaResponse>(
      `${this.api}/${id}`,
      request
    );

  }


  elimina(
    id: number,
    azione: AzioneEliminazioneCategoria,
    nuovaCategoriaPadreId?: number
  ): Observable<void> {

    let params = new HttpParams()
      .set('azione', azione);


    if (nuovaCategoriaPadreId !== undefined) {

      params = params.set(
        'nuovaCategoriaPadreId',
        nuovaCategoriaPadreId
      );

    }


    return this.http.delete<void>(
      `${this.api}/${id}`,
      {
        params
      }
    );

  }


  trovaTuttePiatte(): Observable<CategoriaResponse[]> {

    return this.http.get<CategoriaResponse[]>(
      `${this.api}/all`
    );

  }

}
