import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MovimentoDialogComponent } from './movimento-dialog.component';

describe('MovimentoDialogComponent', () => {
  let component: MovimentoDialogComponent;
  let fixture: ComponentFixture<MovimentoDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MovimentoDialogComponent]
    });
    fixture = TestBed.createComponent(MovimentoDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
