import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

import { DashboardComponent } from './features/dashboard/dashboard.component';
import { MovimentiComponent } from './features/movimenti/movimenti.component';
import { CategorieComponent } from './features/categorie/categorie.component';


const routes: Routes = [

  {
    path: '',
    component: MainLayoutComponent,
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
      }

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
