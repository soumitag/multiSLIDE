import { TestBed, inject } from '@angular/core/testing';

import { DatauploadService } from './dataupload.service';

describe('DatauploadService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DatauploadService]
    });
  });

  it('should be created', inject([DatauploadService], (service: DatauploadService) => {
    expect(service).toBeTruthy();
  }));
});
