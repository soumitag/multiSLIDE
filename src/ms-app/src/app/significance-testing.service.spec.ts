import { TestBed } from '@angular/core/testing';

import { SignificanceTestingService } from './significance-testing.service';

describe('SignificanceTestingService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SignificanceTestingService = TestBed.get(SignificanceTestingService);
    expect(service).toBeTruthy();
  });
});
