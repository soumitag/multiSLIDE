import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NetworkNeighborhoodComponent } from './network-neighborhood.component';

describe('NetworkNeighborhoodComponent', () => {
  let component: NetworkNeighborhoodComponent;
  let fixture: ComponentFixture<NetworkNeighborhoodComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NetworkNeighborhoodComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NetworkNeighborhoodComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
