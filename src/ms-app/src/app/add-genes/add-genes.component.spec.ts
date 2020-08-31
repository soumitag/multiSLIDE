import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddGenesComponent } from './add-genes.component';

describe('AddGenesComponent', () => {
  let component: AddGenesComponent;
  let fixture: ComponentFixture<AddGenesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddGenesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddGenesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
