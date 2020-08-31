import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { FileuploadPayload } from '../fileupload_payload';
import { DatauploadService } from '../dataupload.service';
import { Subscription } from 'rxjs/Subscription';
import { ServerResponseData } from '../server_response';
import { PreviewData } from '../preview_data';
import { DatasetSpecs } from '../dataset_specs';

@Component({
  selector: 'app-get-preview',
  templateUrl: './get-preview.component.html',
  styleUrls: ['./get-preview.component.css']
})
export class GetPreviewComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() previewTouched: boolean;
  @Input() dataset_spec: DatasetSpecs;

  private uploadSubscription: Subscription;
  uploadedFiles: FileuploadPayload[] = [];
  serverResponseOnFileAction: string = null;
  preview : PreviewData = null;

  constructor(private uploadService: DatauploadService) { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.previewTouched) {
      this.previewObject(this.dataset_spec)
    }
  }

  previewObject(dataset_spec: DatasetSpecs) {
    if (dataset_spec) {
      this.uploadSubscription = this.uploadService.previewDataset(
        this.analysis_name, dataset_spec.expanded_filename
      ).subscribe(
            data => {
            this.preview = data
            console.log(this.preview)
            this.serverResponseOnFileAction = "File " + dataset_spec.filename + " preview is successful."                  
          },
          error => {
            this.serverResponseOnFileAction = "File " + dataset_spec.filename + " preview is unsuccessful."
          }
        );
    }
  }


}
