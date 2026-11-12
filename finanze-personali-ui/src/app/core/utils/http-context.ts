import { HttpContextToken } from '@angular/common/http';

/** Se vale true, l'ApiInterceptor non mostra la snackbar d'errore. */
export const SILENZIOSO = new HttpContextToken<boolean>(() => false);
