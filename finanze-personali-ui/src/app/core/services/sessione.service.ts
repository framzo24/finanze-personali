import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import {AuthResponse, Utente} from "@app/models/auth";

const CHIAVE = 'finanze-sessione-v1';

interface Sessione {
  token: string;
  scadenzaMs: number;
  utente: Utente;
}

@Injectable({ providedIn: 'root' })
export class SessioneService {
  private readonly router = inject(Router);
  private readonly stato = signal<Sessione | null>(this.leggi());

  readonly utente = computed(() => this.stato()?.utente ?? null);
  readonly token = computed(() => this.stato()?.token ?? null);

  autenticato(): boolean {
    const s = this.stato();
    return !!s && s.scadenzaMs > Date.now();
  }

  salva(risposta: AuthResponse): void {
    const sessione: Sessione = {
      token: risposta.token,
      scadenzaMs: risposta.scadenzaMs,
      utente: risposta.utente
    };
    try {
      localStorage.setItem(CHIAVE, JSON.stringify(sessione));
    } catch {
      // storage non disponibile: la sessione vale solo finché la pagina resta aperta
    }
    this.stato.set(sessione);
  }

  esci(): void {
    try {
      localStorage.removeItem(CHIAVE);
    } catch {
      // niente da fare
    }
    this.stato.set(null);
    this.router.navigate(['/login']);
  }

  private leggi(): Sessione | null {
    try {
      const raw = localStorage.getItem(CHIAVE);
      if (!raw) return null;
      const s = JSON.parse(raw) as Sessione;
      if (s?.token && s.scadenzaMs > Date.now() && s.utente?.username) return s;
      localStorage.removeItem(CHIAVE);
    } catch {
      // dati non leggibili: si riparte da zero
    }
    return null;
  }
}
