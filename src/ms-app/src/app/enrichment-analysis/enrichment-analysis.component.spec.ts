import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EnrichmentAnalysisComponent } from './enrichment-analysis.component';

describe('EnrichmentAnalysisComponent', () => {
  let component: EnrichmentAnalysisComponent;
  let fixture: ComponentFixture<EnrichmentAnalysisComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EnrichmentAnalysisComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EnrichmentAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
