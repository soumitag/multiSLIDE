import { TestBed } from '@angular/core/testing';

import { GlobalMapConfigService } from './global-map-config.service';

describe('GlobalMapConfigService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: GlobalMapConfigService = TestBed.get(GlobalMapConfigService);
    expect(service).toBeTruthy();
  });
});
