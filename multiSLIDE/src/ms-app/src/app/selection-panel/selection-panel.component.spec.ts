import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionPanelComponent } from './selection-panel.component';

describe('SelectionPanelComponent', () => {
  let component: SelectionPanelComponent;
  let fixture: ComponentFixture<SelectionPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectionPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectionPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
