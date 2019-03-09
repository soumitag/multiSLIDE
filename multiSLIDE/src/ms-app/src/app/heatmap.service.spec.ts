import { TestBed, inject } from '@angular/core/testing';

import { HeatmapService } from './heatmap.service';

describe('HeatmapService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HeatmapService]
    });
  });

  it('should be created', inject([HeatmapService], (service: HeatmapService) => {
    expect(service).toBeTruthy();
  }));
});
