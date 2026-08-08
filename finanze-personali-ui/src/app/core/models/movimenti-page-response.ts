import { MovimentoResponse } from './movimento-response';

export interface MovimentiPageResponse {

  contenuto: MovimentoResponse[];

  pagina: number;

  dimensione: number;

  totaleElementi: number;

  totalePagine: number;
}
