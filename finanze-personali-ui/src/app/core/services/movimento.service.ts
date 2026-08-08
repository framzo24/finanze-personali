import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { MovimentoResponse } from '../models/movimento-response';
import { CreateMovimentoRequest } from '../models/create-movimento-request';
import { UpdateMovimentoRequest } from '../models/update-movimento-request';
import { MovimentoFilter } from '../models/movimento-filter';
import {MovimentiPageResponse} from "../models/movimenti-page-response";

@Injectable({
  providedIn: 'root'
})
export class MovimentoService {

  private readonly apiUrl = `${environment.apiUrl}/movimenti`;

  constructor(
    private http: HttpClient
  ) {}

  trovaTutti(): Observable<MovimentoResponse[]> {
    return this.http.get<MovimentoResponse[]>(this.apiUrl);
  }


  trovaUno(id: number): Observable<MovimentoResponse> {
    return this.http.get<MovimentoResponse>(
      `${this.apiUrl}/${id}`
    );
  }


  crea(
    movimento: CreateMovimentoRequest
  ): Observable<MovimentoResponse> {

    return this.http.post<MovimentoResponse>(
      this.apiUrl,
      movimento
    );
  }


  modifica(
    id: number,
    movimento: CreateMovimentoRequest
  ): Observable<MovimentoResponse> {

    return this.http.put<MovimentoResponse>(
      `${this.apiUrl}/${id}`,
      movimento
    );
  }


  elimina(id: number) {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }


  cerca(
    filtro: MovimentoFilter,
    pagina: number = 0,
    dimensione: number = 20
  ): Observable<MovimentiPageResponse> {

    let params = new HttpParams()
      .set('pagina', pagina)
      .set('dimensione', dimensione);


    Object.entries(filtro).forEach(([key, value]) => {

      if (value !== undefined && value !== null && value !== '') {
        params = params.set(
          key,
          value.toString()
        );
      }

    });


    return this.http.get<MovimentiPageResponse>(
      `${this.apiUrl}/search`,
      {
        params
      }
    );
  }

  trovaPagina(
    pagina: number = 0,
    dimensione: number = 20
  ): Observable<MovimentiPageResponse> {

    const params = new HttpParams()
      .set('pagina', pagina)
      .set('dimensione', dimensione);


    return this.http.get<MovimentiPageResponse>(
      `${this.apiUrl}/pagina`,
      {
        params
      }
    );
  }
}
