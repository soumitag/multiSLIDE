import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadConnectionComponent } from './upload-connection.component';

describe('UploadConnectionComponent', () => {
  let component: UploadConnectionComponent;
  let fixture: ComponentFixture<UploadConnectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UploadConnectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadConnectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
