import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SaveVizComponent } from './save-viz.component';

describe('SaveVizComponent', () => {
  let component: SaveVizComponent;
  let fixture: ComponentFixture<SaveVizComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SaveVizComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveVizComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
