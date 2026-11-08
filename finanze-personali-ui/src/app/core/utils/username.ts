export function normalizza(valore: string): string {
  return valore
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .replace(/[^a-z0-9]/g, '');
}

export function usernameDa(nome: string, cognome: string): string | null {
  const n = normalizza(nome);
  const c = normalizza(cognome);
  return n && c ? `${n}.${c}` : null;
}
