import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { MatSnackBar } from '@angular/material/snack-bar';


@Injectable()
export class ApiInterceptor implements HttpInterceptor {


  constructor(
    private snackBar: MatSnackBar
  ) {}


  intercept(
    req: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {


    return next.handle(req).pipe(

      catchError((error: HttpErrorResponse) => {


        console.error('Errore HTTP', error);


        let messaggio = 'Errore imprevisto';


        if (error.status === 0) {

          messaggio =
            'Backend non raggiungibile';

        }


        else if (error.error?.messages) {

          messaggio =
            error.error.messages.join(', ');

        }


        else {

          switch (error.status) {

            case 401:
              messaggio = 'Utente non autenticato';
              break;

            case 403:
              messaggio = 'Accesso negato';
              break;

            case 404:
              messaggio = 'Risorsa non trovata';
              break;

            case 500:
              messaggio = 'Errore interno del server';
              break;

          }

        }


        this.snackBar.open(
          messaggio,
          'OK',
          {
            duration: 5000
          }
        );


        return throwError(() => error);

      })

    );

  }

}
