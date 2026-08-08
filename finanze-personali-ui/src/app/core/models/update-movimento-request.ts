export interface UpdateMovimentoRequest {

  descrizione: string;

  importo: number;

  tipo: string;

  categoriaId: number;

  dataMovimento?: string;

  stato?: string;

  note?: string;

}
