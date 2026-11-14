import { Component, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatDialog } from '@angular/material/dialog';
import { DashboardService } from '@app/core/services/dashboard.service';
import {CategoriaTotaleResponse, DashboardResponse} from '@app/models/dashboard-response';
import { MovimentoService } from '@app/core/services/movimento.service';
import { MovimentoResponse } from '@app/core/models/movimento-response';
import { MovimentoDialogComponent } from '../movimenti/movimento-dialog/movimento-dialog.component';
import {TipoBolletta} from "@app/core/models/bolletta";
import {PuntoCosto, StatisticheBollette} from "@app/core/models/statistiche";
import {StatisticheService} from "@app/core/services/statistiche.service";
import {TIPO_META} from "@app/core/utils/bollette";

const PALETTE = ['#7C3AED', '#2563EB', '#0891B2', '#16A34A', '#CA8A04', '#EA580C', '#DC2626', '#DB2777'];
const MAX_FETTE = 5;

function meseBreve(data: Date): string {
  return data.toLocaleDateString('it-IT', { month: 'short' }).replace('.', '');
}

function stessoMese(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth();
}

function maiuscola(testo: string): string {
  return testo.charAt(0).toUpperCase() + testo.slice(1);
}

interface Variazione {
  testo: string;
  stato: 'buono' | 'cattivo' | 'neutro';
  direzione: 'su' | 'giu' | 'pari';
}

interface PuntoGrafico {
  x: number;
  y: number;
  costo: number;
  stimata: boolean;
  label: string;
  lungo: string;
  etichetta: boolean;
}

interface SerieCosto {
  unita: string;
  punti: PuntoGrafico[];
  linea: string;
  ultimo: PuntoCosto | null;
  variazione: Variazione | null;
}

interface RigaBolletta {
  tipo: TipoBolletta;
  attuale: number;
  precedente: number;
  wAtt: number;
  wPrec: number;
  variazione: Variazione;
}

// ---------- layout delle card ----------
type CardId = 'saldo' | 'entrate' | 'uscite' | 'mese' | 'nuovo';
type CardSize = 'S' | 'M' | 'W';

interface CardLayout {
  id: CardId;
  size: CardSize;
}

const LAYOUT_KEY = 'finanze-dashboard-layout-v1';

const LAYOUT_DEFAULT: CardLayout[] = [
  { id: 'saldo', size: 'M' },
  { id: 'entrate', size: 'S' },
  { id: 'uscite', size: 'S' },
  { id: 'mese', size: 'S' },
  { id: 'nuovo', size: 'S' }
];

function copiaLayout(layout: CardLayout[]): CardLayout[] {
  return layout.map(c => ({ id: c.id, size: c.size }));
}

function caricaLayout(): CardLayout[] {
  try {
    const raw = localStorage.getItem(LAYOUT_KEY);
    if (raw) {
      const salvato = JSON.parse(raw) as CardLayout[];
      const valido =
        Array.isArray(salvato) &&
        salvato.length === LAYOUT_DEFAULT.length &&
        LAYOUT_DEFAULT.every(d =>
          salvato.some(c => c.id === d.id && ['S', 'M', 'W'].includes(c.size))
        );
      if (valido) return copiaLayout(salvato);
    }
  } catch {
    // dati non leggibili: si usa il layout di default
  }
  return copiaLayout(LAYOUT_DEFAULT);
}

// ---------- statistiche ----------
type StatId = 'andamento' | 'categorie' | 'ultimi' | 'confronto' | 'bollette' | 'costo' | 'riepilogo';

interface Statistica {
  id: StatId;
  titolo: string;
  /** Occupa tutta la larghezza della pagina. */
  larga?: boolean;
}

interface Pagina {
  chiave: string;
  titolo: string;
  stats: Statistica[];
}

interface RigaConfronto {
  label: string;
  icona: string;
  attuale: number;
  precedente: number;
  wAtt: number;
  wPrec: number;
  testo: string;
  stato: 'buono' | 'cattivo' | 'neutro';
  direzione: 'su' | 'giu' | 'pari';
  colore: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  dashboard = signal<DashboardResponse | undefined>(undefined);
  errore = signal(false);
  ultimi = signal<MovimentoResponse[]>([]);
  readonly today = new Date();

  private readonly meseScorsoData = new Date(this.today.getFullYear(), this.today.getMonth() - 1, 1);

  readonly meseCorrenteLabel = this.today.toLocaleDateString('it-IT', { month: 'long' });
  readonly mesePrecedenteLabel = this.meseScorsoData.toLocaleDateString('it-IT', { month: 'long' });
  readonly mesePrecedenteBreve = meseBreve(this.meseScorsoData);

  readonly meta = TIPO_META;

// ---------- spese per categoria: periodo ----------
  periodoSpese = signal<'mese' | 'sempre'>('mese');
  meseSpese = signal<Date>(this.meseScorsoData);
  speseMese = signal<CategoriaTotaleResponse[]>([]);
  caricamentoSpese = signal(false);
  private richiestaSpese = 0;

  /** Gli ultimi 24 mesi, dal più recente, tra cui scegliere. */
  readonly mesiScelta = Array.from(
    { length: 24 },
    (_, i) => new Date(this.today.getFullYear(), this.today.getMonth() - i, 1)
  );

  readonly etichettaMese = computed(() => {
    const d = this.meseSpese();
    return stessoMese(d, this.meseScorsoData)
      ? 'Mese scorso'
      : maiuscola(d.toLocaleDateString('it-IT', { month: 'long', year: 'numeric' }));
  });

  isMeseSelezionato(d: Date): boolean {
    return this.periodoSpese() === 'mese' && stessoMese(d, this.meseSpese());
  }

  scegliSempre(): void {
    this.periodoSpese.set('sempre');
  }

  /** Click sulla parte sinistra del pulsante: il mese mostrato (all'inizio, il mese scorso). */
  scegliMeseCorrenteDelPulsante(): void {
    this.apriMese(this.meseSpese());
  }

  scegliMese(d: Date): void {
    this.apriMese(d);
  }

  private apriMese(d: Date): void {
    this.meseSpese.set(d);
    this.periodoSpese.set('mese');
    this.caricaSpeseMese();
  }

  private caricaSpeseMese(): void {
    const id = ++this.richiestaSpese;
    const m = this.meseSpese();

    this.speseMese.set([]);
    this.caricamentoSpese.set(true);

    this.statisticheService.spesePerMese(m.getFullYear(), m.getMonth() + 1).subscribe({
      next: lista => {
        if (id !== this.richiestaSpese) return;
        this.speseMese.set(lista);
        this.caricamentoSpese.set(false);
      },
      error: () => {
        if (id !== this.richiestaSpese) return;
        this.caricamentoSpese.set(false);
      }
    });
  }

  /** La categoria che pesa di più su tutte le spese (per il riepilogo del mese). */
  readonly topCategoria = computed(() => {
    const lista = [...(this.dashboard()?.spesePerCategoria ?? [])].sort((a, b) => b.totale - a.totale);
    const primo = lista[0];
    return primo ? { nome: primo.categoria, icona: primo.icona ?? '', valore: primo.totale } : null;
  });

// ---------- bollette dell'anno ----------
  statBollette = signal<StatisticheBollette | null>(null);
  bolletteQuota = signal(true);

  readonly righeBollette = computed(() => {
    const s = this.statBollette();
    if (!s) return null;
    const quota = this.bolletteQuota();

    const base = s.tipi
      .map(t => ({
        tipo: t.tipo,
        attuale: quota ? t.annoCorrenteQuota : t.annoCorrente,
        precedente: quota ? t.annoScorsoQuota : t.annoScorso
      }))
      .filter(r => r.attuale !== 0 || r.precedente !== 0);

    const max = Math.max(1, ...base.flatMap(r => [Math.abs(r.attuale), Math.abs(r.precedente)]));
    const larghezza = (v: number) => (v === 0 ? 0 : Math.max((Math.abs(v) / max) * 100, 3));

    const righe: RigaBolletta[] = base.map(r => ({
      ...r,
      wAtt: larghezza(r.attuale),
      wPrec: larghezza(r.precedente),
      variazione: this.variazione(r.attuale, r.precedente)
    }));

    const attuale = base.reduce((somma, r) => somma + r.attuale, 0);
    const precedente = base.reduce((somma, r) => somma + r.precedente, 0);

    return { righe, totale: { attuale, precedente, variazione: this.variazione(attuale, precedente) } };
  });

// ---------- costo unitario ----------
  costoTipo = signal<TipoBolletta>('LUCE');
  readonly tipiConsumo: TipoBolletta[] = ['LUCE', 'GAS', 'ACQUA'];

  readonly costo = computed<SerieCosto>(() => {
    const serie = this.statBollette()?.costi.find(c => c.tipo === this.costoTipo());
    const unita = serie?.unita ?? '';
    const dati = serie?.punti ?? [];

    if (dati.length === 0) {
      return { unita, punti: [], linea: '', ultimo: null, variazione: null };
    }

    const valori = dati.map(p => p.costo);
    const min = Math.min(...valori);
    const spazio = Math.max(...valori) - min;
    const n = dati.length;

    const punti: PuntoGrafico[] = dati.map((p, i) => {
      const [anno, mese] = p.periodo.split('-').map(Number);
      const data = new Date(anno, mese - 1, 1);
      return {
        x: n === 1 ? 50 : (i / (n - 1)) * 100,
        y: spazio === 0 ? 50 : 88 - ((p.costo - min) / spazio) * 76,
        costo: p.costo,
        stimata: p.natura === 'STIMATA',
        label: meseBreve(data),
        lungo: maiuscola(data.toLocaleDateString('it-IT', { month: 'long', year: 'numeric' })),
        // con tanti punti si mostra un mese su due, partendo dall'ultimo
        etichetta: n <= 6 || (n - 1 - i) % 2 === 0
      };
    });

    const ultimo = dati[n - 1];
    const prima = n > 1 ? dati[n - 2] : null;

    return {
      unita,
      punti,
      linea: punti.map(p => `${p.x},${p.y}`).join(' '),
      ultimo,
      // per le bollette, spendere di più è un peggioramento
      variazione: prima ? this.variazione(ultimo.costo, prima.costo) : null
    };
  });

  private variazione(attuale: number, precedente: number): Variazione {
    const delta = attuale - precedente;
    let testo: string;

    if (precedente === 0) {
      testo = attuale === 0 ? '—' : 'Nuovo';
    } else {
      testo = new Intl.NumberFormat('it-IT', { maximumFractionDigits: 0, signDisplay: 'exceptZero' })
        .format((delta / Math.abs(precedente)) * 100) + '%';
    }

    return {
      testo,
      stato: delta === 0 ? 'neutro' : delta > 0 ? 'cattivo' : 'buono',
      direzione: delta > 0 ? 'su' : delta < 0 ? 'giu' : 'pari'
    };
  }

  // ---------- entrate e uscite: mese corrente / mese scorso ----------
  scorsoEntrate = signal(false);
  scorsoUscite = signal(false);

  /** Entrate e uscite del mese scorso, cercate per chiave "AAAA-MM". */
  private precedente = computed(() => {
    const d = this.meseScorsoData;
    const chiave = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
    const riga = (this.dashboard()?.andamentoMensile ?? []).find(m => m.mese === chiave);
    return { entrate: riga?.entrate ?? 0, uscite: riga?.uscite ?? 0 };
  });

  entrateMostrate = computed(() =>
    this.scorsoEntrate() ? this.precedente().entrate : (this.dashboard()?.entrateMeseCorrente ?? 0)
  );

  usciteMostrate = computed(() =>
    this.scorsoUscite() ? this.precedente().uscite : (this.dashboard()?.usciteMeseCorrente ?? 0)
  );

  // ---------- modifica layout ----------
  layout = signal<CardLayout[]>(caricaLayout());
  modifica = signal(false);
  private snapshot: CardLayout[] = [];

  readonly misure: { size: CardSize; label: string }[] = [
    { size: 'S', label: 'Piccola' },
    { size: 'M', label: 'Media' },
    { size: 'W', label: 'Larga' }
  ];

  iniziaModifica(): void {
    this.snapshot = copiaLayout(this.layout());
    this.modifica.set(true);
  }

  conferma(): void {
    try {
      localStorage.setItem(LAYOUT_KEY, JSON.stringify(this.layout()));
    } catch {
      // storage non disponibile: il layout vale solo per questa sessione
    }
    this.modifica.set(false);
  }

  annulla(): void {
    this.layout.set(copiaLayout(this.snapshot));
    this.modifica.set(false);
  }

  ripristina(): void {
    this.layout.set(copiaLayout(LAYOUT_DEFAULT));
  }

  impostaMisura(id: CardId, size: CardSize): void {
    this.layout.update(l => l.map(c => (c.id === id ? { ...c, size } : c)));
  }

  sposta(evento: CdkDragDrop<CardLayout[]>): void {
    if (evento.previousIndex === evento.currentIndex) return;
    this.layout.update(l => {
      const copia = [...l];
      moveItemInArray(copia, evento.previousIndex, evento.currentIndex);
      return copia;
    });
  }

  // ---------- carosello ----------
  /** Tutte le statistiche disponibili (in futuro: elenco da cui scegliere con il "+"). */
  readonly statistiche: Statistica[] = [
    { id: 'andamento', titolo: 'Entrate e uscite' },
    { id: 'categorie', titolo: 'Spese per categoria' },
    { id: 'ultimi', titolo: 'Ultimi movimenti' },
    { id: 'confronto', titolo: 'Mese su mese' },
    { id: 'bollette', titolo: 'Bollette dell\'anno' },
    { id: 'costo', titolo: 'Costo unitario' },
    { id: 'riepilogo', titolo: 'Riepilogo del mese', larga: true }
  ];

  /** Id delle statistiche mostrate, nell'ordine in cui compaiono. */
  visibili = signal<StatId[]>(['andamento', 'categorie', 'ultimi', 'confronto', 'bollette', 'costo', 'riepilogo']);

  /** Raggruppa le statistiche: due per pagina, quelle "larghe" da sole. */
  pagine = computed<Pagina[]>(() => {
    const stats = this.visibili()
      .map(id => this.statistiche.find(s => s.id === id))
      .filter((s): s is Statistica => !!s);

    const risultato: Pagina[] = [];
    let corrente: Statistica[] = [];

    const chiudi = () => {
      if (corrente.length) {
        risultato.push({
          chiave: corrente.map(s => s.id).join('+'),
          titolo: corrente.map(s => s.titolo).join(' e '),
          stats: corrente
        });
        corrente = [];
      }
    };

    for (const s of stats) {
      if (s.larga) {
        chiudi();
        corrente = [s];
        chiudi();
      } else {
        corrente.push(s);
        if (corrente.length === 2) chiudi();
      }
    }
    chiudi();
    return risultato;
  });

  attiva = signal(0);

  onScroll(event: Event): void {
    const el = event.target as HTMLElement;
    const figli = el.children;
    if (figli.length < 2) {
      this.attiva.set(0);
      return;
    }
    const passo = (figli[1] as HTMLElement).offsetLeft - (figli[0] as HTMLElement).offsetLeft;
    const indice = Math.round(el.scrollLeft / passo);
    this.attiva.set(Math.min(Math.max(indice, 0), figli.length - 1));
  }

  vai(indice: number, track: HTMLElement): void {
    const figlio = track.children[indice] as HTMLElement | undefined;
    if (figlio) {
      track.scrollTo({ left: figlio.offsetLeft, behavior: 'smooth' });
    }
  }

  // ---------- dati ----------
  saldoMese = computed(() =>
    (this.dashboard()?.entrateMeseCorrente ?? 0) - (this.dashboard()?.usciteMeseCorrente ?? 0)
  );

  /** Barre: entrate e uscite per mese. */
  andamento = computed(() => {
    const dati = this.dashboard()?.andamentoMensile ?? [];
    const max = Math.max(1, ...dati.flatMap(d => [d.entrate, d.uscite]));
    const chiaveCorrente =
      `${this.today.getFullYear()}-${String(this.today.getMonth() + 1).padStart(2, '0')}`;
    const altezza = (valore: number) => (valore > 0 ? Math.max((valore / max) * 100, 3) : 0);

    return dati.map(d => {
      const [anno, mese] = d.mese.split('-').map(Number);
      return {
        chiave: d.mese,
        label: meseBreve(new Date(anno, mese - 1, 1)),
        entrate: d.entrate,
        uscite: d.uscite,
        hIn: altezza(d.entrate),
        hOut: altezza(d.uscite),
        corrente: d.mese === chiaveCorrente
      };
    });
  });

  andamentoVuoto = computed(() => this.andamento().every(m => m.entrate === 0 && m.uscite === 0));

  /** Ciambella: spese per categoria (prime fette + "Altre"). */
  spese = computed(() => {
    const sorgente =
      this.periodoSpese() === 'mese' ? this.speseMese() : (this.dashboard()?.spesePerCategoria ?? []);
    const lista = [...sorgente].sort((a, b) => b.totale - a.totale);    const totale = lista.reduce((somma, c) => somma + c.totale, 0);
    if (totale <= 0) return { totale: 0, voci: [] };

    const voci = lista.slice(0, MAX_FETTE).map((c, i) => ({
      nome: c.categoria,
      icona: c.icona ?? '',
      colore: c.colore || PALETTE[i % PALETTE.length],
      valore: c.totale
    }));

    const resto = lista.slice(MAX_FETTE).reduce((somma, c) => somma + c.totale, 0);
    if (resto > 0) {
      voci.push({ nome: 'Altre', icona: '', colore: '#94A3B8', valore: resto });
    }

    let accumulato = 0;
    return {
      totale,
      voci: voci.map(v => {
        const pct = (v.valore / totale) * 100;
        const visibile = Math.max(pct - 0.8, 0.1); // piccolo spazio tra le fette
        const fetta = {
          ...v,
          pct,
          dash: `${visibile} ${100 - visibile}`,
          offset: -accumulato
        };
        accumulato += pct;
        return fetta;
      })
    };
  });

  /** Mese corrente contro mese precedente. */
  confronto = computed<RigaConfronto[] | null>(() => {
    const dati = this.dashboard()?.andamentoMensile ?? [];
    if (dati.length < 2) return null;

    const cur = dati[dati.length - 1];
    const prec = dati[dati.length - 2];

    return [
      this.rigaConfronto('Entrate', 'south_west', cur.entrate, prec.entrate, true, 'var(--app-income)'),
      this.rigaConfronto('Uscite', 'north_east', cur.uscite, prec.uscite, false, 'var(--app-expense)'),
      this.rigaConfronto(
        'Saldo', 'account_balance_wallet',
        cur.entrate - cur.uscite, prec.entrate - prec.uscite,
        true, 'var(--app-primary)'
      )
    ];
  });

  private rigaConfronto(
    label: string,
    icona: string,
    attuale: number,
    precedente: number,
    piuEBuono: boolean,
    colore: string
  ): RigaConfronto {
    const max = Math.max(Math.abs(attuale), Math.abs(precedente), 1);
    const larghezza = (v: number) => (v === 0 ? 0 : Math.max((Math.abs(v) / max) * 100, 3));
    const delta = attuale - precedente;

    let testo: string;
    if (precedente === 0) {
      testo = attuale === 0 ? '—' : 'Nuovo';
    } else {
      testo = new Intl.NumberFormat('it-IT', { maximumFractionDigits: 0, signDisplay: 'exceptZero' })
        .format((delta / Math.abs(precedente)) * 100) + '%';
    }

    return {
      label,
      icona,
      attuale,
      precedente,
      wAtt: larghezza(attuale),
      wPrec: larghezza(precedente),
      testo,
      stato: delta === 0 ? 'neutro' : (delta > 0) === piuEBuono ? 'buono' : 'cattivo',
      direzione: delta > 0 ? 'su' : delta < 0 ? 'giu' : 'pari',
      colore
    };
  }

  /** Statistiche rapide del mese corrente. */
  riepilogo = computed(() => {
    const d = this.dashboard();
    const giorno = this.today.getDate();
    const giorniMese = new Date(this.today.getFullYear(), this.today.getMonth() + 1, 0).getDate();
    const usciteMese = d?.usciteMeseCorrente ?? 0;
    const entrateMese = d?.entrateMeseCorrente ?? 0;
    const mediaGiorno = usciteMese / giorno;

    return {
      mediaGiorno,
      proiezione: mediaGiorno * giorniMese,
      risparmio: entrateMese > 0 ? ((entrateMese - usciteMese) / entrateMese) * 100 : null,
      top: this.topCategoria()
    };
  });

  constructor(
    private dashboardService: DashboardService,
    private movimentoService: MovimentoService,
    private statisticheService: StatisticheService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.caricaTutto();
  }

  caricaTutto(): void {
    this.caricaDashboard();
    this.caricaUltimi();
    this.caricaBollette();
    if (this.periodoSpese() === 'mese') {
      this.caricaSpeseMese();
    }
  }

  caricaBollette(): void {
    this.statisticheService.bollette().subscribe({
      next: s => this.statBollette.set(s),
      error: error => console.error('Errore caricamento statistiche bollette', error)
    });
  }

  caricaDashboard(): void {
    this.dashboardService.getDashboard().subscribe({
      next: response => {
        this.dashboard.set(response);
        this.errore.set(false);
      },
      error: error => {
        this.errore.set(true);
        console.error('Errore caricamento dashboard', error);
      }
    });
  }

  caricaUltimi(): void {
    // la ricerca è già ordinata per data decrescente
    this.movimentoService.cerca({}, 0, 10).subscribe({
      next: response => this.ultimi.set(response.contenuto),
      error: error => console.error('Errore caricamento ultimi movimenti', error)
    });
  }

  nuovoMovimento(): void {
    this.dialog
      .open(MovimentoDialogComponent, {
        width: '520px',
        maxWidth: '95vw',
        autoFocus: false
      })
      .afterClosed()
      .subscribe(result => {
        if (result) this.caricaTutto();
      });
  }
}
