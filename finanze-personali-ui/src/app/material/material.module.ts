import { NgModule } from '@angular/core';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@NgModule({
  exports: [
    MatToolbarModule,
    MatSidenavModule,
    MatTableModule,
    MatPaginatorModule,

    MatCardModule,
    MatButtonModule,
    MatIconModule
  ]
})
export class MaterialModule { }
