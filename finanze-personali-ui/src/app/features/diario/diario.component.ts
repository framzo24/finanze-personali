import { Component, ElementRef, HostListener, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TextFieldModule } from '@angular/cdk/text-field';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Observable, catchError, finalize, map, of, switchMap, tap } from 'rxjs';
import { VoceDiario } from '@app/core/models/diario';
import { DiarioService } from '@app/core/services/diario.service';
import { daIso, isoData } from './data';
import { EsitoModifiche, ModificheDialogComponent } from './modifiche-dialog/modifiche-dialog.component';

const ULTIMI_PENSIERI = 10;
const ULTIMI_GIORNI = 14;
const MAX_CARATTERI = 20000;

interface Cella {
  numero: number;
  iso: string;
  oggi: boolean;
  futuro: boolean;
  conTesto: boolean;
  selezionato: boolean;
}

interface VoceElenco {
  giorno: string;
  data: Date;
  testo: string;
}

@Component({
  selector: 'app-diario',
  imports: [
    DatePipe, ReactiveFormsModule, TextFieldModule,
    MatButtonModule, MatDatepickerModule, MatFormFieldModule, MatIconModule, MatInputModule, MatTooltipModule
  ],
  templateUrl: './diario.component.html',
  styleUrl: './diario.component.scss'
})
export class DiarioComponent implements OnInit {
  private readonly service = inject(DiarioService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  private readonly contenitore = viewChild<ElementRef<HTMLElement>>('editor');
  private readonly area = viewChild<ElementRef<HTMLTextAreaElement>>('area');

  readonly maxCaratteri = MAX_CARATTERI;
  readonly giorniSettimana = ['L', 'M', 'M', 'G', 'V', 'S', 'D'];

  readonly oggi = new Date();
  readonly oggiIso = isoData(this.oggi);
  private readonly ieriIso = isoData(new Date(this.oggi.getFullYear(), this.oggi.getMonth(), this.oggi.getDate() - 1));
  private readonly primoMeseCorrente = new Date(this.oggi.getFullYear(), this.oggi.getMonth(), 1);

  // ---------- giorno aperto nell'editor ----------
  giorno = signal(this.oggiIso);
  testoSalvato = signal('');
  aggiornatoIl = signal<string | null>(null);
  bozza = signal('');
  inModifica = signal(false);
  caricamento = signal(true);
  salvataggio = signal(false);
  readonly dataCtrl = new FormControl<Date | null>(this.oggi);

  readonly giornoData = computed(() => daIso(this.giorno()));

  /** Ci sono modifiche non ancora salvate. */
  readonly sporco = computed(() => this.inModifica() && this.bozza().trim() !== this.testoSalvato().trim());

  /** Se salvi ora, il giorno resta senza pensieri. */
  readonly svuotera = computed(
    () => this.inModifica() && this.testoSalvato() !== '' && this.bozza().trim() === ''
  );

  // ---------- calendario ----------
  mese = signal(this.primoMeseCorrente);
  giorniConTesto = signal<ReadonlySet<string>>(new Set());

  readonly titoloMese = computed(() =>
    this.mese().toLocaleDateString('it-IT', { month: 'long', year: 'numeric' })
  );

  readonly puoAvanzare = computed(() => this.mese().getTime() < this.primoMeseCorrente.getTime());

  readonly celle = computed<(Cella | null)[]>(() => {
    const primo = this.mese();
    const anno = primo.getFullYear();
    const m = primo.getMonth();
    const giorni = new Date(anno, m + 1, 0).getDate();
    const vuote = (primo.getDay() + 6) % 7; // la settimana parte da lunedì
    const conTesto = this.giorniConTesto();
    const selezionato = this.giorno();

    const celle: (Cella | null)[] = Array.from({ length: vuote }, () => null);
    for (let g = 1; g <= giorni; g++) {
      const iso = isoData(new Date(anno, m, g));
      celle.push({
        numero: g,
        iso,
        oggi: iso === this.oggiIso,
        futuro: iso > this.oggiIso,
        conTesto: conTesto.has(iso),
        selezionato: iso === selezionato
      });
    }
    return celle;
  });

  // ---------- elenco ----------
  tutti = signal(false);
  elenco = signal<VoceElenco[]>([]);
  elencoCaricato = signal(false);

  private richiesta = 0;
  private richiestaElenco = 0;

  ngOnInit(): void {
    this.caricaGiorniMese();
    this.caricaElenco();
    this.apriGiorno(this.oggiIso, false);
  }

  // ---------- navigazione tra i giorni ----------
  /** Click su un giorno (calendario o elenco): chiede conferma se ci sono modifiche non salvate. */
  apri(iso: string): void {
    if (iso === this.giorno()) {
      this.scorriAlEditor();
      return;
    }
    this.proteggi(() => this.apriGiorno(iso, true));
  }

  cambiaData(data: Date | null): void {
    const riporta = () => this.dataCtrl.setValue(daIso(this.giorno()), { emitEvent: false });

    if (!data || isoData(data) === this.giorno() || isoData(data) > this.oggiIso) {
      riporta();
      return;
    }
    this.proteggi(() => this.apriGiorno(isoData(data), true), riporta);
  }

  tornaAOggi(): void {
    if (this.giorno() !== this.oggiIso) {
      this.apri(this.oggiIso);
    } else {
      this.mostraMese(this.oggi);
    }
  }

  cambiaMese(delta: number): void {
    const m = this.mese();
    const nuovo = new Date(m.getFullYear(), m.getMonth() + delta, 1);
    if (nuovo.getTime() > this.primoMeseCorrente.getTime()) return;
    this.mostraMese(nuovo);
  }

  // ---------- scrittura ----------
  modifica(): void {
    this.inModifica.set(true);
    this.mettiFuoco();
  }

  /** Torna al testo salvato. È già una scelta esplicita di scartare, quindi non chiede conferma. */
  annulla(): void {
    this.bozza.set(this.testoSalvato());
    this.inModifica.set(this.testoSalvato() === '');
  }

  salvaClick(): void {
    if (this.sporco()) {
      this.salva().subscribe();
    }
  }

  salva(): Observable<boolean> {
    if (this.salvataggio()) return of(false);

    this.salvataggio.set(true);
    return this.service.salva(this.giorno(), this.bozza().trim()).pipe(
      tap(voce => this.dopoSalvataggio(voce)),
      map(() => true),
      catchError(() => of(false)),
      finalize(() => this.salvataggio.set(false))
    );
  }

  /** Vero se si può lasciare il giorno aperto: niente modifiche, oppure salvate o scartate dal dialog. */
  confermaUscita(): Observable<boolean> {
    if (!this.sporco()) return of(true);

    return this.dialog
      .open<ModificheDialogComponent, void, EsitoModifiche>(ModificheDialogComponent, {
        width: '460px',
        maxWidth: '95vw',
        autoFocus: false
      })
      .afterClosed()
      .pipe(
        switchMap(esito => {
          if (esito === 'salva') return this.salva();
          if (esito === 'scarta') {
            this.bozza.set(this.testoSalvato());
            return of(true);
          }
          return of(false);
        })
      );
  }

  @HostListener('window:beforeunload', ['$event'])
  avvisaChiusura(evento: BeforeUnloadEvent): void {
    if (this.sporco()) {
      evento.preventDefault();
    }
  }

  // ---------- elenco ----------
  alternaElenco(): void {
    this.tutti.update(v => !v);
    this.caricaElenco();
  }

  etichetta(v: VoceElenco): string {
    if (v.giorno === this.oggiIso) return 'Oggi';
    if (v.giorno === this.ieriIso) return 'Ieri';
    return v.data.toLocaleDateString('it-IT', { weekday: 'long' });
  }

  // ---------- appoggio ----------
  private proteggi(azione: () => void, alRifiuto?: () => void): void {
    if (this.salvataggio()) {
      alRifiuto?.();
      return;
    }
    this.confermaUscita().subscribe(ok => (ok ? azione() : alRifiuto?.()));
  }

  private apriGiorno(iso: string, daUtente: boolean): void {
    const id = ++this.richiesta;
    const prima = this.giorno();

    this.giorno.set(iso);
    this.dataCtrl.setValue(daIso(iso), { emitEvent: false });
    this.mostraMese(daIso(iso));
    this.caricamento.set(true);

    this.service.giorno(iso).subscribe({
      next: voce => {
        if (id !== this.richiesta) return;
        this.testoSalvato.set(voce.testo);
        this.aggiornatoIl.set(voce.aggiornatoIl);
        this.bozza.set(voce.testo);
        this.inModifica.set(voce.testo === '');
        this.caricamento.set(false);

        if (voce.testo === '') this.mettiFuoco();
        if (daUtente) this.scorriAlEditor();
      },
      error: () => {
        if (id !== this.richiesta) return;
        // il testo mostrato è ancora quello del giorno precedente: si torna lì
        this.giorno.set(prima);
        this.dataCtrl.setValue(daIso(prima), { emitEvent: false });
        this.caricamento.set(false);
      }
    });
  }

  private dopoSalvataggio(voce: VoceDiario): void {
    const presente = voce.testo !== '';

    this.testoSalvato.set(voce.testo);
    this.aggiornatoIl.set(voce.aggiornatoIl);
    this.bozza.set(voce.testo);
    this.inModifica.set(!presente);

    this.segna(voce.giorno, presente);
    this.caricaElenco();
    this.snackBar.open(presente ? 'Pensiero salvato' : 'Il giorno è di nuovo vuoto', 'OK', { duration: 3000 });
  }

  /** Aggiorna il segno nel calendario senza ricaricare il mese. */
  private segna(iso: string, presente: boolean): void {
    const m = this.mese();
    const chiaveMese = `${m.getFullYear()}-${String(m.getMonth() + 1).padStart(2, '0')}`;
    if (iso.slice(0, 7) !== chiaveMese) return;

    this.giorniConTesto.update(insieme => {
      const nuovo = new Set(insieme);
      if (presente) {
        nuovo.add(iso);
      } else {
        nuovo.delete(iso);
      }
      return nuovo;
    });
  }

  private mostraMese(d: Date): void {
    const m = this.mese();
    if (d.getFullYear() === m.getFullYear() && d.getMonth() === m.getMonth()) return;

    this.mese.set(new Date(d.getFullYear(), d.getMonth(), 1));
    this.giorniConTesto.set(new Set());
    this.caricaGiorniMese();
  }

  private caricaGiorniMese(): void {
    const m = this.mese();
    const anno = m.getFullYear();
    const mese = m.getMonth() + 1;

    this.service.giorni(anno, mese).subscribe(lista => {
      const attuale = this.mese();
      if (attuale.getFullYear() === anno && attuale.getMonth() + 1 === mese) {
        this.giorniConTesto.set(new Set(lista));
      }
    });
  }

  private caricaElenco(): void {
    const id = ++this.richiestaElenco;

    if (!this.tutti()) {
      this.service.recenti(ULTIMI_PENSIERI).subscribe(lista => {
        if (id !== this.richiestaElenco) return;
        this.elenco.set(lista.map(v => ({ giorno: v.giorno, data: daIso(v.giorno), testo: v.testo })));
        this.elencoCaricato.set(true);
      });
      return;
    }

    const da = new Date(this.oggi.getFullYear(), this.oggi.getMonth(), this.oggi.getDate() - (ULTIMI_GIORNI - 1));
    this.service.periodo(isoData(da), this.oggiIso).subscribe(lista => {
      if (id !== this.richiestaElenco) return;

      const testi = new Map(lista.map(v => [v.giorno, v.testo]));
      const voci: VoceElenco[] = [];
      for (let i = 0; i < ULTIMI_GIORNI; i++) {
        const data = new Date(this.oggi.getFullYear(), this.oggi.getMonth(), this.oggi.getDate() - i);
        const iso = isoData(data);
        voci.push({ giorno: iso, data, testo: testi.get(iso) ?? '' });
      }
      this.elenco.set(voci);
      this.elencoCaricato.set(true);
    });
  }

  private mettiFuoco(): void {
    setTimeout(() => {
      const campo = this.area()?.nativeElement;
      if (campo) {
        campo.focus();
        campo.setSelectionRange(campo.value.length, campo.value.length);
      }
    });
  }

  private scorriAlEditor(): void {
    setTimeout(() => this.contenitore()?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'nearest' }));
  }
}
