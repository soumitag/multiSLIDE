import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadNextComponent } from './load-next.component';

describe('LoadNextComponent', () => {
  let component: LoadNextComponent;
  let fixture: ComponentFixture<LoadNextComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LoadNextComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadNextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
