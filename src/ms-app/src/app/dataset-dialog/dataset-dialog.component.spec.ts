import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DatasetDialogComponent } from './dataset-dialog.component';

describe('DatasetDialogComponent', () => {
  let component: DatasetDialogComponent;
  let fixture: ComponentFixture<DatasetDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DatasetDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DatasetDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
