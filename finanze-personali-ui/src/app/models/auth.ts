export interface Utente {
  id: number;
  nome: string;
  cognome: string;
  username: string;
}

export interface AuthResponse {
  token: string;
  scadenzaMs: number;
  utente: Utente;
}

export interface Disponibilita {
  username: string;
  disponibile: boolean;
}

export interface RegistrazioneRequest {
  nome: string;
  cognome: string;
  password: string;
}
