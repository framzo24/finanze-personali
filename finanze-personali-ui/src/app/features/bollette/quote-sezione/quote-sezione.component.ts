import { Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TipoBolletta } from '@app/core/models/bolletta';
import { CellaProspetto, Prospetto } from '@app/core/models/quote';
import { QuoteService } from '@app/core/services/quote.service';
import { TIPO_META } from '@app/core/utils/bollette';
import { euro } from '@app/core/utils/quote';
import { SaldiConfigDialogComponent } from '../saldi-config-dialog/saldi-config-dialog.component';
import { VersamentoDialogComponent } from '../versamento-dialog/versamento-dialog.component';

@Component({
  selector: 'app-quote-sezione',
  imports: [CurrencyPipe, MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './quote-sezione.component.html',
  styleUrl: './quote-sezione.component.scss'
})
export class QuoteSezioneComponent {
  private readonly quote = inject(QuoteService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  /** Cambia ogni volta che la pagina si ricarica: serve a ricaricare anche il prospetto. */
  versione = input(0);
  soloMia = input(false);
  cambiato = output<void>();

  readonly meta = TIPO_META;
  prospetto = signal<Prospetto | null>(null);
  soloAperte = signal(true);
  caricamento = signal(true);
  private richiesta = 0;

  /** Le colonne mostrate: tutte le persone, oppure solo tu. */
  readonly indici = computed(() => {
    const p = this.prospetto();
    if (!p) return [];
    const tutti = p.persone.map((_, i) => i);
    return this.soloMia() ? tutti.filter(i => p.persone[i].tu) : tutti;
  });

  constructor() {
    effect(() => {
      this.versione();
      const soloAperte = this.soloAperte();
      untracked(() => this.carica(soloAperte));
    });
  }

  private carica(soloAperte: boolean): void {
    const id = ++this.richiesta;
    this.quote.prospetto(soloAperte).subscribe({
      next: p => {
        if (id !== this.richiesta) return;
        this.prospetto.set(p);
        this.caricamento.set(false);
      },
      error: () => {
        if (id !== this.richiesta) return;
        this.caricamento.set(false);
      }
    });
  }

  stato(c: CellaProspetto): 'manca' | 'piu' | 'pari' {
    if (c.mancante > 0.005) return 'manca';
    return c.mancante < -0.005 ? 'piu' : 'pari';
  }

  // ---------- azioni ----------
  registra(indice?: number, tipo?: TipoBolletta | null): void {
    const persona = indice === undefined ? undefined : this.prospetto()?.persone[indice];

    this.dialog
      .open(VersamentoDialogComponent, {
        width: '600px',
        maxWidth: '95vw',
        autoFocus: false,
        data: { coinquilinoId: persona ? persona.coinquilinoId : null, tipo: tipo ?? null }
      })
      .afterClosed()
      .subscribe(registrati => {
        if (registrati) {
          this.snackBar.open('Invio registrato', 'OK', { duration: 3000 });
          this.cambiato.emit();
        }
      });
  }

  impostazioni(): void {
    const p = this.prospetto();
    this.dialog
      .open(SaldiConfigDialogComponent, {
        width: '520px',
        maxWidth: '95vw',
        autoFocus: false,
        data: {
          saldiDal: p?.contaDal ?? null,
          configurato: p?.configurato ?? false,
          modalita: 'IRENE',
          proprietario: p?.proprietario ?? null
        }
      })
      .afterClosed()
      .subscribe(salvato => {
        if (salvato) {
          this.snackBar.open('Impostazioni salvate', 'OK', { duration: 3000 });
          this.cambiato.emit();
        }
      });
  }

  /** Copia un messaggio da incollare nella chat: per una persona, oppure per tutte. */
  copia(indice?: number): void {
    const p = this.prospetto();
    if (!p) return;

    const testo = indice === undefined ? this.messaggioTutti(p) : this.messaggioPersona(p, indice);
    navigator.clipboard.writeText(testo).then(
      () => this.snackBar.open('Messaggio copiato', 'OK', { duration: 3000 }),
      () => this.snackBar.open('Non sono riuscito a copiare il messaggio', 'OK', { duration: 4000 })
    );
  }

  private voci(p: Prospetto, indice: number): { label: string; importo: number }[] {
    return p.utenze
      .map(r => ({ label: TIPO_META[r.tipo as TipoBolletta].label, importo: r.celle[indice].mancante }))
      .filter(v => v.importo > 0.005);
  }

  private messaggioPersona(p: Prospetto, indice: number): string {
    const nome = p.persone[indice].nome;
    const voci = this.voci(p, indice);
    if (voci.length === 0) return `${nome}, per ora non c'è niente da inviare.`;

    const totale = voci.reduce((somma, v) => somma + v.importo, 0);
    return [
      `${nome}, per le bollette devi inviare a ${p.proprietario.toLowerCase()}:`,
      ...voci.map(v => `• ${v.label}: ${euro(v.importo)}`),
      `Totale: ${euro(totale)}`
    ].join('\n');
  }

  private messaggioTutti(p: Prospetto): string {
    const righe = p.persone.map((persona, i) => {
      const voci = this.voci(p, i);
      if (voci.length === 0) return `${persona.nome}: niente da inviare`;
      const totale = voci.reduce((somma, v) => somma + v.importo, 0);
      return `${persona.nome}: ${voci.map(v => `${v.label} ${euro(v.importo)}`).join(' · ')} = ${euro(totale)}`;
    });
    return [`Da inviare a ${p.proprietario.toLowerCase()}:`, ...righe].join('\n');
  }
}
