import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GetPreviewComponent } from './get-preview.component';

describe('GetPreviewComponent', () => {
  let component: GetPreviewComponent;
  let fixture: ComponentFixture<GetPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GetPreviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GetPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
