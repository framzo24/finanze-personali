import { StatoQuota } from '../models/quote';

const FORMATO = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' });

export function euro(n: number): string {
  return FORMATO.format(n);
}

export function arrotonda(n: number): number {
  return Math.round(n * 100) / 100;
}

/** Stessa regola del backend: tre parti uguali, i centesimi avanzati vanno a Victoria (0), Irene (1), Anita (2). */
export function quotaIrene(importo: number, indice = 1): number {
  const centesimi = Math.round(Math.abs(importo) * 100);
  const base = Math.floor(centesimi / 3);
  const parte = base + (indice < centesimi % 3 ? 1 : 0);
  return (importo < 0 ? -parte : parte) / 100;
}

export const STATO_QUOTA: Record<StatoQuota, { label: string; icona: string }> = {
  DA_INVIARE: { label: 'Da inviare', icona: 'schedule' },
  PARZIALE: { label: 'Inviato in parte', icona: 'timelapse' },
  INVIATA: { label: 'Inviata', icona: 'check_circle' },
  DICHIARATA: { label: 'Dichiarata', icona: 'chat_bubble' },
  A_POSTO: { label: 'A posto', icona: 'task_alt' }
};
