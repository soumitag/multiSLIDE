import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PhenotypeSortingComponent } from './phenotype-sorting.component';

describe('PhenotypeSortingComponent', () => {
  let component: PhenotypeSortingComponent;
  let fixture: ComponentFixture<PhenotypeSortingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PhenotypeSortingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PhenotypeSortingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
