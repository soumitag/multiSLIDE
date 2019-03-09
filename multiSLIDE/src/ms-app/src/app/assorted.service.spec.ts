import { TestBed, inject } from '@angular/core/testing';

import { AssortedService } from './assorted.service';

describe('AssortedService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AssortedService]
    });
  });

  it('should be created', inject([AssortedService], (service: AssortedService) => {
    expect(service).toBeTruthy();
  }));
});
