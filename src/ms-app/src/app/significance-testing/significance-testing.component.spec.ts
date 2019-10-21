import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SignificanceTestingComponent } from './significance-testing.component';

describe('SignificanceTestingComponent', () => {
  let component: SignificanceTestingComponent;
  let fixture: ComponentFixture<SignificanceTestingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SignificanceTestingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SignificanceTestingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
