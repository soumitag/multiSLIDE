import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MenuPanelComponent } from './menu-panel.component';

describe('MenuPanelComponent', () => {
  let component: MenuPanelComponent;
  let fixture: ComponentFixture<MenuPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MenuPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MenuPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
