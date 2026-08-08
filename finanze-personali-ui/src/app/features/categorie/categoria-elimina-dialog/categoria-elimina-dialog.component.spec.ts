import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoriaEliminaDialogComponent } from './categoria-elimina-dialog.component';

describe('CategoriaEliminaDialogComponent', () => {
  let component: CategoriaEliminaDialogComponent;
  let fixture: ComponentFixture<CategoriaEliminaDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CategoriaEliminaDialogComponent]
    });
    fixture = TestBed.createComponent(CategoriaEliminaDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
