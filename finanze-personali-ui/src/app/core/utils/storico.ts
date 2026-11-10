import { Coinquilino, RigaStorico } from '../models/bolletta';

export interface RisultatoStorico {
  righe: RigaStorico[];
  errori: string[];
}

/** Accetta GG/MM/AAAA, GG-MM-AAAA, GG.MM.AAAA oppure AAAA-MM-GG. */
export function parseData(valore: string): string | null {
  const s = valore.trim();
  let anno: number, mese: number, giorno: number;

  let m = /^(\d{1,2})[\/.\-](\d{1,2})[\/.\-](\d{4})$/.exec(s);
  if (m) {
    [giorno, mese, anno] = [Number(m[1]), Number(m[2]), Number(m[3])];
  } else {
    m = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(s);
    if (!m) return null;
    [anno, mese, giorno] = [Number(m[1]), Number(m[2]), Number(m[3])];
  }

  const d = new Date(anno, mese - 1, giorno);
  if (d.getFullYear() !== anno || d.getMonth() !== mese - 1 || d.getDate() !== giorno) return null;
  return `${anno}-${String(mese).padStart(2, '0')}-${String(giorno).padStart(2, '0')}`;
}

/** Accetta "1.234,56", "1234,56", "1234.56" e il simbolo €. */
export function parseNumero(valore: string): number | null {
  let s = valore.replace(/[€\s]/g, '');
  if (!s) return null;
  if (s.includes(',')) {
    s = s.replace(/\./g, '').replace(',', '.');
  }
  const n = Number(s);
  return Number.isFinite(n) ? n : null;
}

/**
 * Una bolletta per riga, colonne separate da ; oppure da tabulazione (incolla da un foglio di calcolo):
 * inizio periodo; fine periodo; scadenza; importo; consumo (facoltativo); pagante (facoltativo).
 */
export function parseStorico(
  testo: string,
  coinquilini: Coinquilino[],
  paganteDefault: number | null
): RisultatoStorico {
  const righe: RigaStorico[] = [];
  const errori: string[] = [];

  testo.split(/\r?\n/).forEach((linea, indice) => {
    if (!linea.trim()) return;

    const celle = linea.split(/[;\t]/).map(c => c.trim());
    const numero = indice + 1;

    // la prima riga può essere un'intestazione
    if (indice === 0 && !parseData(celle[0])) return;

    if (celle.length < 4) {
      errori.push(`Riga ${numero}: servono almeno inizio, fine, scadenza e importo`);
      return;
    }

    const dal = parseData(celle[0]);
    const al = parseData(celle[1]);
    const scadenza = parseData(celle[2]);
    const importo = parseNumero(celle[3]);

    if (!dal || !al || !scadenza) {
      errori.push(`Riga ${numero}: data non valida`);
      return;
    }
    if (al < dal) {
      errori.push(`Riga ${numero}: la fine del periodo precede l'inizio`);
      return;
    }
    if (importo === null || importo <= 0) {
      errori.push(`Riga ${numero}: importo non valido`);
      return;
    }

    let consumo: number | null = null;
    if (celle[4]) {
      consumo = parseNumero(celle[4]);
      if (consumo === null || consumo < 0) {
        errori.push(`Riga ${numero}: consumo non valido`);
        return;
      }
    }

    let paganteId = paganteDefault;
    const nomePagante = (celle[5] ?? '').toLowerCase();
    if (nomePagante) {
      if (nomePagante === 'tu' || nomePagante === 'io') {
        paganteId = null;
      } else {
        const trovato = coinquilini.find(c => c.nome.toLowerCase() === nomePagante);
        if (!trovato) {
          errori.push(`Riga ${numero}: pagante «${celle[5]}» sconosciuto`);
          return;
        }
        paganteId = trovato.id;
      }
    }

    righe.push({ periodoDal: dal, periodoAl: al, dataScadenza: scadenza, importo, consumo, paganteId });
  });

  return { righe, errori };
}
