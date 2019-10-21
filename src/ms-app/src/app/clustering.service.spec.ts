import { TestBed } from '@angular/core/testing';

import { ClusteringService } from './clustering.service';

describe('ClusteringService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ClusteringService = TestBed.get(ClusteringService);
    expect(service).toBeTruthy();
  });
});
