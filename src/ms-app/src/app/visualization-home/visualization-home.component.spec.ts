import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VisualizationHomeComponent } from './visualization-home.component';

describe('VisualizationHomeComponent', () => {
  let component: VisualizationHomeComponent;
  let fixture: ComponentFixture<VisualizationHomeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisualizationHomeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisualizationHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
