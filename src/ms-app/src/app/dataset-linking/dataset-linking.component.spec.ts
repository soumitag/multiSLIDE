import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatasetLinkingComponent } from './dataset-linking.component';

describe('DatasetLinkingComponent', () => {
  let component: DatasetLinkingComponent;
  let fixture: ComponentFixture<DatasetLinkingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatasetLinkingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatasetLinkingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
