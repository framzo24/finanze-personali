import { CategoriaTotaleResponse } from './categoria-totale-response';
import { AndamentoMensileResponse } from './andamento-mensile-response';

export interface DashboardResponse {

  saldo: number;

  totaleEntrate: number;

  totaleUscite: number;

  entrateMeseCorrente: number;

  usciteMeseCorrente: number;

  spesePerCategoria: CategoriaTotaleResponse[];

  andamentoMensile: AndamentoMensileResponse[];

}
