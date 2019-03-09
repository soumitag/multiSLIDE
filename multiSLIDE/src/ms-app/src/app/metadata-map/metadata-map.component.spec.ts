import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataMapComponent } from './metadata-map.component';

describe('MetadataMapComponent', () => {
  let component: MetadataMapComponent;
  let fixture: ComponentFixture<MetadataMapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetadataMapComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
