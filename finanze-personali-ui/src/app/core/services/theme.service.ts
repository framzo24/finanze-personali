import { Injectable, computed, effect, signal } from '@angular/core';

export type ThemeMode = 'light' | 'dark';
export type ThemeColors = Record<string, string>;

export interface ThemeToken {
  key: string;
  label: string;
  group: string;
}

/** Elenco dei colori personalizzabili: servirà alla futura pagina "Aspetto". */
export const THEME_TOKENS: ThemeToken[] = [
  { key: 'primary',       label: 'Colore principale',            group: 'Generale' },
  { key: 'on-primary',    label: 'Testo sul colore principale',  group: 'Generale' },
  { key: 'sidebar-bg',    label: 'Sfondo menu',                  group: 'Menu' },
  { key: 'sidebar-text',  label: 'Testo menu',                   group: 'Menu' },
  { key: 'sidebar-muted', label: 'Testo secondario menu',        group: 'Menu' },
  { key: 'canvas',        label: 'Sfondo pagina',                group: 'Pagine' },
  { key: 'card',          label: 'Sfondo card e finestre',       group: 'Pagine' },
  { key: 'text',          label: 'Testo',                        group: 'Pagine' },
  { key: 'text-muted',    label: 'Testo secondario',             group: 'Pagine' },
  { key: 'border',        label: 'Bordi',                        group: 'Pagine' },
  { key: 'income',        label: 'Entrate',                      group: 'Importi' },
  { key: 'expense',       label: 'Uscite',                       group: 'Importi' }
];

export const DEFAULT_COLORS: Record<ThemeMode, ThemeColors> = {
  light: {
    'primary': '#7C3AED',
    'on-primary': '#FFFFFF',
    'sidebar-bg': '#14102A',
    'sidebar-text': '#F3F0FF',
    'sidebar-muted': '#A59FC4',
    'canvas': '#F4F2FA',
    'card': '#FFFFFF',
    'text': '#17132B',
    'text-muted': '#625C7D',
    'border': '#E3DFF0',
    'income': '#15803D',
    'expense': '#DC2626'
  },
  dark: {
    'primary': '#A78BFA',
    'on-primary': '#1E0F4D',
    'sidebar-bg': '#0B0914',
    'sidebar-text': '#F3F0FF',
    'sidebar-muted': '#8B85A8',
    'canvas': '#151220',
    'card': '#1E1A2E',
    'text': '#F3F0FF',
    'text-muted': '#A59FC4',
    'border': '#2E2945',
    'income': '#4ADE80',
    'expense': '#F87171'
  }
};

const STORAGE_KEY = 'finanze-theme-v1';

interface StoredTheme {
  mode: ThemeMode;
  overrides: Record<ThemeMode, ThemeColors>;
}

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly mode = signal<ThemeMode>('light');
  private readonly overrides = signal<Record<ThemeMode, ThemeColors>>({ light: {}, dark: {} });

  readonly isDark = computed(() => this.mode() === 'dark');

  /** Colori effettivi del tema corrente (default + personalizzazioni). */
  readonly colors = computed<ThemeColors>(() => {
    const mode = this.mode();
    const custom = this.overrides()[mode];
    const merged = { ...DEFAULT_COLORS[mode], ...custom };
    // Se l'utente cambia il colore principale ma non il testo sopra, lo calcoliamo noi
    if (custom['primary'] && !custom['on-primary']) {
      merged['on-primary'] = readableOn(custom['primary']);
    }
    return merged;
  });

  constructor() {
    this.load();
    this.applyToDom();
    effect(() => {
      this.applyToDom();
      this.save();
    });
  }

  toggle(): void {
    this.setMode(this.isDark() ? 'light' : 'dark');
  }

  setMode(mode: ThemeMode): void {
    const change = () => {
      this.mode.set(mode);
      this.applyToDom();
    };
    const doc = document as Document & { startViewTransition?: (cb: () => void) => unknown };
    if (doc.startViewTransition) {
      doc.startViewTransition(change);
    } else {
      change();
    }
  }

  /** Cambia un colore del tema corrente. Es: setColor('primary', '#FF5722') */
  setColor(key: string, value: string): void {
    const mode = this.mode();
    this.overrides.update(o => ({ ...o, [mode]: { ...o[mode], [key]: value } }));
  }

  resetColor(key: string): void {
    const mode = this.mode();
    this.overrides.update(o => {
      const { [key]: _removed, ...rest } = o[mode];
      return { ...o, [mode]: rest };
    });
  }

  /** Ripristina tutti i colori del tema corrente. */
  resetAll(): void {
    const mode = this.mode();
    this.overrides.update(o => ({ ...o, [mode]: {} }));
  }

  isCustomized(key: string): boolean {
    return key in this.overrides()[this.mode()];
  }

  private applyToDom(): void {
    const root = document.documentElement;
    for (const [key, value] of Object.entries(this.colors())) {
      root.style.setProperty(`--app-${key}`, value);
    }
    root.style.colorScheme = this.mode();
    root.dataset['theme'] = this.mode();
  }

  private load(): void {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const saved = JSON.parse(raw) as StoredTheme;
        this.mode.set(saved.mode === 'dark' ? 'dark' : 'light');
        this.overrides.set({
          light: saved.overrides?.light ?? {},
          dark: saved.overrides?.dark ?? {}
        });
        return;
      }
    } catch {
      // dati corrotti: si riparte dai default
    }
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.mode.set(prefersDark ? 'dark' : 'light');
  }

  private save(): void {
    try {
      const data: StoredTheme = { mode: this.mode(), overrides: this.overrides() };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    } catch {
      // storage non disponibile: ignoriamo
    }
  }
}

/** Sceglie bianco o nero in base alla luminanza dello sfondo. */
function readableOn(hex: string): string {
  const h = hex.replace('#', '');
  const full = h.length === 3 ? h.split('').map(c => c + c).join('') : h;
  const [r, g, b] = [0, 2, 4]
    .map(i => parseInt(full.slice(i, i + 2), 16) / 255)
    .map(c => (c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4)));
  const luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
  return luminance > 0.179 ? '#111111' : '#FFFFFF';
}
