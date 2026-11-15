import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ModalitaBollette } from '@app/core/models/auth';
import { ProfiloService } from '@app/core/services/profilo.service';
import { SessioneService } from '@app/core/services/sessione.service';

@Component({
  selector: 'app-scelta-modalita',
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './scelta-modalita.component.html',
  styleUrl: './scelta-modalita.component.scss'
})
export class SceltaModalitaComponent {
  private readonly profilo = inject(ProfiloService);
  private readonly router = inject(Router);
  readonly sessione = inject(SessioneService);

  readonly scelta = signal<ModalitaBollette | null>(null);
  readonly invio = signal(false);

  readonly modalita: { id: ModalitaBollette; titolo: string; icona: string; testo: string; punti: string[] }[] = [
    {
      id: 'CLASSICA',
      titolo: 'Classica',
      icona: 'swap_horiz',
      testo: 'Chi paga la bolletta anticipa i soldi e gli altri gli restituiscono la loro parte.',
      punti: [
        'Chi ha anticipato ogni bolletta',
        'Saldi con i coinquilini e rimborsi',
        'La tua quota nei movimenti'
      ]
    },
    {
      id: 'IRENE',
      titolo: 'Irene',
      icona: 'send',
      testo: 'Le bollette le paga il proprietario di casa e ognuno gli invia la sua quota.',
      punti: [
        'Quanto deve inviare ciascuno, per utenza e in totale',
        'I tuoi invii e quelli dichiarati dagli altri',
        'Tre persone fisse: Victoria, Irene e Anita'
      ]
    }
  ];

  conferma(): void {
    const modalita = this.scelta();
    if (!modalita || this.invio()) return;

    this.invio.set(true);
    this.profilo.scegliModalita(modalita).subscribe({
      next: utente => {
        this.sessione.aggiornaUtente(utente);
        this.router.navigateByUrl('/dashboard');
      },
      error: () => this.invio.set(false)
    });
  }
}
