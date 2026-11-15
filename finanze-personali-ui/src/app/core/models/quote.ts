import { StatoBolletta, TipoBolletta } from './bolletta';

export type StatoQuota = 'DA_INVIARE' | 'PARZIALE' | 'INVIATA' | 'DICHIARATA' | 'A_POSTO';

export interface QuotaPersona {
  coinquilinoId: number | null;
  nome: string;
  tu: boolean;
  quota: number;
  inviato: number;
  mancante: number;
  stato: StatoQuota;
}

export interface VersamentoQuote {
  id: number;
  bollettaId: number;
  coinquilinoId: number | null;
  personaNome: string;
  tu: boolean;
  importo: number;
  data: string;
  nota: string | null;
  dichiarato: boolean;
  movimentoId: number | null;
}

export interface BollettaQuote {
  bollettaId: number;
  tipo: TipoBolletta;
  fornitore: string;
  periodoDal: string;
  periodoAl: string;
  dataScadenza: string;
  importo: number;
  stato: StatoBolletta;
  scaduta: boolean;
  storica: boolean;
  persone: QuotaPersona[];
  versamenti: VersamentoQuote[];
}

export interface PersonaProspetto {
  coinquilinoId: number | null;
  nome: string;
  tu: boolean;
}

export interface CellaProspetto {
  quota: number;
  inviato: number;
  mancante: number;
}

export interface RigaProspetto {
  tipo: TipoBolletta | null;
  bollette: number;
  totale: number;
  celle: CellaProspetto[];
}

export interface Prospetto {
  configurato: boolean;
  contaDal: string | null;
  proprietario: string;
  persone: PersonaProspetto[];
  utenze: RigaProspetto[];
  somma: RigaProspetto;
}

export interface VoceVersamento {
  bollettaId: number;
  importo: number;
}

export interface VersamentiRequest {
  coinquilinoId: number | null;
  data: string;
  nota: string | null;
  registraMovimento: boolean;
  voci: VoceVersamento[];
}
