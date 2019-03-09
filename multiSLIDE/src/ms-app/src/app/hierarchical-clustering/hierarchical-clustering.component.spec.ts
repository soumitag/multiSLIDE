import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HierarchicalClusteringComponent } from './hierarchical-clustering.component';

describe('HierarchicalClusteringComponent', () => {
  let component: HierarchicalClusteringComponent;
  let fixture: ComponentFixture<HierarchicalClusteringComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HierarchicalClusteringComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HierarchicalClusteringComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
