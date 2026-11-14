import { NaturaBolletta, TipoBolletta } from './bolletta';

export interface StatisticaTipo {
  tipo: TipoBolletta;
  annoCorrente: number;
  annoCorrenteQuota: number;
  annoScorso: number;
  annoScorsoQuota: number;
  numeroAnnoCorrente: number;
  numeroAnnoScorso: number;
}

export interface PuntoCosto {
  periodo: string;
  costo: number;
  importo: number;
  consumo: number;
  natura: NaturaBolletta;
}

export interface CostoUnitario {
  tipo: TipoBolletta;
  unita: string;
  punti: PuntoCosto[];
}

export interface StatisticheBollette {
  anno: number;
  fino: string;
  finoAnnoScorso: string;
  tipi: StatisticaTipo[];
  costi: CostoUnitario[];
}
