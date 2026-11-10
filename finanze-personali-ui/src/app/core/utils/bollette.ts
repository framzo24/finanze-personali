import { NaturaBolletta, TipoBolletta } from '../models/bolletta';

export interface TipoMeta {
  tipo: TipoBolletta;
  label: string;
  icona: string;
  colore: string;
  unita: string | null;
}

export const TIPI: TipoMeta[] = [
  { tipo: 'LUCE', label: 'Luce', icona: 'bolt', colore: '#CA8A04', unita: 'kWh' },
  { tipo: 'GAS', label: 'Gas', icona: 'local_fire_department', colore: '#EA580C', unita: 'Smc' },
  { tipo: 'ACQUA', label: 'Acqua', icona: 'water_drop', colore: '#2563EB', unita: 'm³' },
  { tipo: 'WIFI', label: 'Wi-Fi', icona: 'wifi', colore: '#7C3AED', unita: null }
];

export const TIPO_META = Object.fromEntries(TIPI.map(t => [t.tipo, t])) as Record<TipoBolletta, TipoMeta>;

export const NATURE: { valore: NaturaBolletta; label: string }[] = [
  { valore: 'EFFETTIVA', label: 'Effettiva' },
  { valore: 'STIMATA', label: 'Stimata' },
  { valore: 'CONGUAGLIO', label: 'Conguaglio' }
];

/** Data locale in formato AAAA-MM-GG, senza passare dal fuso orario. */
export function isoData(d: Date): string {
  const mese = String(d.getMonth() + 1).padStart(2, '0');
  const giorno = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mese}-${giorno}`;
}

/** Da "AAAA-MM-GG" a Date locale. */
export function daIso(s: string): Date {
  const [anno, mese, giorno] = s.split('-').map(Number);
  return new Date(anno, mese - 1, giorno);
}

/** Stessa regola del backend: un terzo, e il centesimo che avanza resta al pagante. */
export function quotaDi(importo: number, paganteId: number | null): number {
  const centesimi = Math.round(importo * 100);
  const terzo = Math.trunc(centesimi / 3);
  return (paganteId === null ? centesimi - 2 * terzo : terzo) / 100;
}
