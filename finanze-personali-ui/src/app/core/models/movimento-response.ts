import { TipoMovimento } from './tipo-movimento';

export interface MovimentoResponse {

  id: number;

  descrizione: string;

  importo: number;

  tipo: TipoMovimento;

  categoria: string;

  data: string;

  categoriaId: number;
}
