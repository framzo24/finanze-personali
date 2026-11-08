import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { ApiInterceptor } from './core/interceptors/api.interceptor';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTreeModule } from '@angular/material/tree';

import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { NavbarComponent } from './layout/navbar/navbar.component';
import { SidebarComponent } from './layout/sidebar/sidebar.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { MovimentiComponent } from './features/movimenti/movimenti.component';
import { CategorieComponent } from './features/categorie/categorie.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MaterialModule} from "./material/material.module";
import { MovimentoDialogComponent } from './features/movimenti/movimento-dialog/movimento-dialog.component';
import {MatSnackBar} from "@angular/material/snack-bar";
import { CategoriaDialogComponent } from './features/categorie/categoria-dialog/categoria-dialog.component';
import { CategoriaEliminaDialogComponent } from './features/categorie/categoria-elimina-dialog/categoria-elimina-dialog.component';
import {MatRadioModule} from "@angular/material/radio";
import {MatIconModule} from "@angular/material/icon";
import { LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import {MatTooltipModule} from "@angular/material/tooltip";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {AuthInterceptor} from "@app/core/interceptors/auth.interceptor";

@NgModule({ declarations: [
    AppComponent,
    DashboardComponent,
    MovimentiComponent,
    CategorieComponent,
    MovimentoDialogComponent,
    CategoriaDialogComponent,
    CategoriaEliminaDialogComponent
  ],
  bootstrap: [AppComponent]
  , imports: [BrowserModule,
    AppRoutingModule,
    NavbarComponent,
    SidebarComponent,
    MainLayoutComponent,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MaterialModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    FormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTreeModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatRadioModule,
    MatTooltipModule,
  DragDropModule], providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
      provideZoneChangeDetection({
        eventCoalescing: true
      }),
    {
      provide: LOCALE_ID, useValue: 'it-IT'
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ApiInterceptor,
      multi: true
    },
    provideHttpClient(withXhr(), withInterceptorsFromDi())
  ] })
export class AppModule { }

registerLocaleData(localeIt);
