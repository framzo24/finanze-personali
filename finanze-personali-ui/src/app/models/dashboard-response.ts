export interface DashboardResponse {
  saldo: number;
  totaleEntrate: number;
  totaleUscite: number;
  entrateMeseCorrente: number;
  usciteMeseCorrente: number;

  spesePerCategoria: CategoriaTotaleResponse[];

  andamentoMensile: AndamentoMensileResponse[];
}


export interface CategoriaTotaleResponse {
  categoriaId: number;
  categoria: string;
  totale: number;
}


export interface AndamentoMensileResponse {
  mese: string;
  entrate: number;
  uscite: number;
}
