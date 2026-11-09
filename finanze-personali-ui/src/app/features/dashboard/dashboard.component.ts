import { Component, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatDialog } from '@angular/material/dialog';
import { DashboardService } from '@app/core/services/dashboard.service';
import { DashboardResponse } from '@app/models/dashboard-response';
import { MovimentoService } from '@app/core/services/movimento.service';
import { MovimentoResponse } from '@app/core/models/movimento-response';
import { MovimentoDialogComponent } from '../movimenti/movimento-dialog/movimento-dialog.component';

const PALETTE = ['#7C3AED', '#2563EB', '#0891B2', '#16A34A', '#CA8A04', '#EA580C', '#DC2626', '#DB2777'];
const MAX_FETTE = 5;

function meseBreve(data: Date): string {
  return data.toLocaleDateString('it-IT', { month: 'short' }).replace('.', '');
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
type StatId = 'andamento' | 'categorie' | 'ultimi' | 'confronto' | 'riepilogo';

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
    { id: 'riepilogo', titolo: 'Riepilogo del mese', larga: true }
  ];

  /** Id delle statistiche mostrate, nell'ordine in cui compaiono. */
  visibili = signal<StatId[]>(['andamento', 'categorie', 'ultimi', 'confronto', 'riepilogo']);

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
    const lista = [...(this.dashboard()?.spesePerCategoria ?? [])].sort((a, b) => b.totale - a.totale);
    const totale = lista.reduce((somma, c) => somma + c.totale, 0);
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
      top: this.spese().voci[0] ?? null
    };
  });

  constructor(
    private dashboardService: DashboardService,
    private movimentoService: MovimentoService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.caricaTutto();
  }

  caricaTutto(): void {
    this.caricaDashboard();
    this.caricaUltimi();
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
