export type TipoBolletta = 'LUCE' | 'GAS' | 'ACQUA' | 'WIFI';
export type NaturaBolletta = 'EFFETTIVA' | 'STIMATA' | 'CONGUAGLIO';
export type StatoBolletta = 'DA_PAGARE' | 'PAGATA';
export type OrigineBolletta = 'MANUALE' | 'CANONE' | 'FILE';

export interface Bolletta {
  id: number;
  tipo: TipoBolletta;
  fornitore: string;
  periodoDal: string;
  periodoAl: string;
  dataEmissione: string | null;
  dataScadenza: string;
  importo: number;
  consumo: number | null;
  unitaConsumo: string | null;
  costoUnitario: number | null;
  numeroDocumento: string | null;
  natura: NaturaBolletta;
  stato: StatoBolletta;
  scaduta: boolean;
  dataPagamento: string | null;
  paganteId: number | null;
  paganteNome: string;
  quotaTua: number;
  note: string | null;
  origine: OrigineBolletta;
}

export interface BollettaRequest {
  tipo: TipoBolletta;
  fornitore: string;
  periodoDal: string;
  periodoAl: string;
  dataEmissione: string | null;
  dataScadenza: string;
  importo: number;
  consumo: number | null;
  numeroDocumento: string | null;
  natura: NaturaBolletta;
  stato: StatoBolletta;
  dataPagamento: string | null;
  paganteId: number | null;
  note: string | null;
}

export interface BollettePage {
  contenuto: Bolletta[];
  pagina: number;
  dimensione: number;
  totaleElementi: number;
  totalePagine: number;
}

export interface Coinquilino {
  id: number;
  nome: string;
}

export interface PuntoAndamento {
  periodo: string;
  importo: number;
  quota: number;
}

export interface RiepilogoTipo {
  tipo: TipoBolletta;
  ultima: Bolletta | null;
  totaleAnno: number;
  totaleAnnoQuota: number;
  media: number;
  mediaQuota: number;
  daPagare: number;
  andamento: PuntoAndamento[];
}

export interface RiepilogoBollette {
  tipi: RiepilogoTipo[];
  daPagare: number;
  daPagareImporto: number;
  daPagareQuota: number;
}

export interface RigaStorico {
  periodoDal: string;
  periodoAl: string;
  dataScadenza: string;
  importo: number;
  consumo: number | null;
  paganteId: number | null;
}

export interface BatchStoricoRequest {
  tipo: TipoBolletta;
  fornitore: string;
  righe: RigaStorico[];
}

export interface BatchStoricoResponse {
  creati: number;
  saltati: number;
}
