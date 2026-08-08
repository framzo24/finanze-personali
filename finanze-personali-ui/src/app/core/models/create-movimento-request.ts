import { TipoMovimento } from './tipo-movimento';

export interface CreateMovimentoRequest {

  descrizione: string;

  importo: number;

  tipo: TipoMovimento;

  categoriaId: number;

  dataMovimento?: string;

  stato?: string;

}
