import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MovimentiComponent } from './features/movimenti/movimenti.component';
import { CategorieComponent } from './features/categorie/categorie.component';
import {authGuard, guestGuard} from "@app/core/guard/auth.guard";
import {LoginComponent} from "@app/features/auth/login/login.component";
import {RegistrazioneComponent} from "@app/features/auth/registrazione/registrazione.component";
import {BolletteComponent} from "@app/features/bollette/bollette.component";


const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'registrazione', component: RegistrazioneComponent, canActivate: [guestGuard] },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [

      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },

      {
        path: 'dashboard',
        component: DashboardComponent
      },

      {
        path: 'movimenti',
        component: MovimentiComponent
      },

      {
        path: 'categorie',
        component: CategorieComponent
      },

      {
        path: 'bollette',
        component: BolletteComponent },

    ]
  },

  {
    path: '**',
    redirectTo: 'dashboard'
  }

];


@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule { }
