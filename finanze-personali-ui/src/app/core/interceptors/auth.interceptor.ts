import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SessioneService } from '../services/sessione.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private readonly sessione = inject(SessioneService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const perApi = req.url.startsWith(environment.apiUrl);
    const pubblica = req.url.startsWith(`${environment.apiUrl}/auth/`);
    const token = this.sessione.token();

    const richiesta = token && perApi && !pubblica
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

    return next.handle(richiesta).pipe(
      catchError((errore: HttpErrorResponse) => {
        // token scaduto o non valido: si torna al login
        if (errore.status === 401 && perApi && !pubblica) {
          this.sessione.esci();
        }
        return throwError(() => errore);
      })
    );
  }
}
