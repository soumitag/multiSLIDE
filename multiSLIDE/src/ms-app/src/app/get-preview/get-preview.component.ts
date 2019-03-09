import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { FileuploadPayload } from '../fileupload_payload';
import { DatauploadService } from '../dataupload.service';
import { Subscription } from 'rxjs/Subscription';
import { ServerResponseData } from '../server_response';
import { PreviewData } from '../preview_data';

@Component({
  selector: 'app-get-preview',
  templateUrl: './get-preview.component.html',
  styleUrls: ['./get-preview.component.css']
})
export class GetPreviewComponent implements OnInit {

  @Input() previewTouched: boolean;
  @Input() filepreview_payload: FileuploadPayload;

  private uploadSubscription: Subscription;
  uploadedFiles: FileuploadPayload[] = [];
  serverResponseOnFileAction: string = null;
  preview : PreviewData = null;

  constructor(private uploadService: DatauploadService) { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (this.previewTouched) {
      this.previewObject(this.filepreview_payload)
    }
  }

  previewObject(filepreview_payload: FileuploadPayload) {
    if (filepreview_payload) {
      //this.previewTouched = true;
      console.log("You have selected preview")
      //var fileupload_payload: FileuploadPayload = this.uploadedFiles[id];
      filepreview_payload.data_action = "preview"
      console.log(filepreview_payload)

      this.uploadSubscription = this.uploadService.previewDataset(
        filepreview_payload).subscribe(
            data => {
            this.preview = data
            console.log(this.preview)
            this.serverResponseOnFileAction = "File " + filepreview_payload.filename + " preview is successful."                  
          },
          error => {
            this.serverResponseOnFileAction = "File " + filepreview_payload.filename + " preview is unsuccessful."
          }
        );
    }
  }

}
