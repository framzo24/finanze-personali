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
