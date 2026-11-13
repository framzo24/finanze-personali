export type TipoBolletta = 'LUCE' | 'GAS' | 'ACQUA' | 'WIFI';
export type NaturaBolletta = 'EFFETTIVA' | 'STIMATA' | 'CONGUAGLIO';
export type StatoBolletta = 'DA_PAGARE' | 'PAGATA';
export type OrigineBolletta = 'MANUALE' | 'CANONE' | 'FILE';

export interface Bolletta {
  movimentoId: boolean;
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

export type LivelloScadenza = 'SCADUTA' | 'IN_SCADENZA' | 'PROSSIMA' | 'MANCANTE';

export interface Scadenza {
  bollettaId: number | null;
  tipo: TipoBolletta;
  fornitore: string;
  data: string;
  importo: number | null;
  quotaTua: number | null;
  prevista: boolean;
  giorni: number;
  livello: LivelloScadenza;
  paganteId: number | null;
  paganteNome: string;
  periodoDal: string;
  periodoAl: string;
}

export interface Notifica {
  id: string;
  livello: LivelloScadenza;
  tipo: TipoBolletta;
  titolo: string;
  messaggio: string;
  bollettaId: number | null;
  data: string;
}

export interface Impostazione {
  tipo: TipoBolletta;
  periodicitaMesi: number;
  preavvisoGiorni: number;
}

export interface Tariffa {
  id: number;
  importo: number;
  validaDal: string;
  inVigore: boolean;
}

export interface Canone {
  id: number;
  tipo: TipoBolletta;
  fornitore: string;
  giornoScadenza: number;
  paganteId: number | null;
  paganteNome: string;
  pagamentoAutomatico: boolean;
  attivo: boolean;
  importoAttuale: number;
  tariffe: Tariffa[];
}

export interface CanoneRequest {
  fornitore: string;
  giornoScadenza: number;
  paganteId: number | null;
  pagamentoAutomatico: boolean;
  attivo: boolean;
}

export interface TariffaEsito {
  canone: Canone;
  aggiornate: number;
  nonModificate: number;
}

export type DirezioneRimborso = 'RICEVUTO' | 'DATO';

export interface SaldoCoinquilino {
  coinquilinoId: number;
  nome: string;
  saldo: number;
  daBollette: number;
  daRimborsi: number;
}

export interface Saldi {
  configurato: boolean;
  saldiDal: string | null;
  coinquilini: SaldoCoinquilino[];
  totale: number;
}

export interface VoceSaldo {
  tipo: 'BOLLETTA' | 'RIMBORSO';
  id: number;
  data: string;
  titolo: string;
  dettaglio: string;
  effetto: number;
  progressivo: number;
  tipoBolletta: TipoBolletta | null;
  movimentoId: number | null;
}

export interface ConfigurazioneSaldi {
  configurato: boolean;
  saldiDal: string | null;
}

export interface RimborsoRequest {
  coinquilinoId: number;
  direzione: DirezioneRimborso;
  importo: number;
  data: string;
  nota: string | null;
  registraMovimento: boolean;
  tipo: TipoBolletta | null;
}

export interface Rimborso {
  id: number;
  coinquilinoId: number;
  coinquilinoNome: string;
  direzione: DirezioneRimborso;
  importo: number;
  data: string;
  nota: string | null;
  movimentoId: number | null;
}
