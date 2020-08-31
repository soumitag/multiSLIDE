import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadSearchTabComponent } from './upload-search-tab.component';

describe('UploadSearchTabComponent', () => {
  let component: UploadSearchTabComponent;
  let fixture: ComponentFixture<UploadSearchTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UploadSearchTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadSearchTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
