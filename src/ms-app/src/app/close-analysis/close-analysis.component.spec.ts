import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CloseAnalysisComponent } from './close-analysis.component';

describe('CloseAnalysisComponent', () => {
  let component: CloseAnalysisComponent;
  let fixture: ComponentFixture<CloseAnalysisComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CloseAnalysisComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CloseAnalysisComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
