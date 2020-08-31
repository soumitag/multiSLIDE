import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PhenotypeDialogComponent } from './phenotype-dialog.component';

describe('PhenotypeDialogComponent', () => {
  let component: PhenotypeDialogComponent;
  let fixture: ComponentFixture<PhenotypeDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PhenotypeDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PhenotypeDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
