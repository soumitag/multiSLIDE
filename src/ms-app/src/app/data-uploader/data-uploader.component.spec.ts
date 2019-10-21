import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataUploaderComponent } from './data-uploader.component';

describe('DataUploaderComponent', () => {
  let component: DataUploaderComponent;
  let fixture: ComponentFixture<DataUploaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataUploaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataUploaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
