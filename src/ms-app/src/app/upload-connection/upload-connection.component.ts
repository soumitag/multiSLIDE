import { Component, OnInit, Input, Inject } from '@angular/core';
import { DatauploadService } from '../dataupload.service'
import { AssortedService } from '../assorted.service'
import { Subscription } from 'rxjs/Subscription'
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { FileuploadPayload } from '../fileupload_payload';
import { ServerResponseData } from '../server_response';

@Component({
  selector: 'app-upload-connection',
  templateUrl: './upload-connection.component.html',
  styleUrls: ['./upload-connection.component.css']
})
export class UploadConnectionComponent implements OnInit {

  private uploadSubscription: Subscription;
  private assortedSubscription: Subscription;
  analysis_name: string;
  name: string;
  file: File = null;
  isUploaded: boolean = false;
  uploadTouched: boolean = false;
  delimiter: string = '';
  available_delimiters: string[] = ['', 'Tab', 'Comma', 'Space', 'Pipe', 'Semi-colon', 'Line'];
  available_connections: string[][] = [];
  hasChanged: number = 0;

  serverResponse: ServerResponseData = null;

  constructor(
    private dialogRef: MatDialogRef<UploadConnectionComponent>, 
    private uploadService: DatauploadService, 
    private assortedServices: AssortedService,
    @Inject(MAT_DIALOG_DATA) data
  ) {
    this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '150px', left: '300px' });
    this.dialogRef.updateSize('920px', '450px');

    /*
    let a = ['display_name_1','filename_1'];
    this.available_connections.push(a);
    let b = ['display_name_2','filename_2'];
    this.available_connections.push(b);
    let c = ['display_name_3','filename_3'];
    this.available_connections.push(c);
    */
  }

  ngOnInit(): void {
    this.getUserSpecifiedConnections();
  }

  getUserSpecifiedConnections() {
    this.assortedSubscription = this.assortedServices.getUserSpecifiedConnections(
      this.analysis_name
    ).subscribe(
      data => this.available_connections = data,
      () => console.log("error getting headers")
    );
  }

  removeUserSpecifiedConnections(filename: string) {
    this.assortedSubscription = this.assortedServices.removeUserSpecifiedConnections(
      this.analysis_name, filename
    ).subscribe(
      res => this.serverResponse = res,
      () => console.log(this.serverResponse),
      () => this.notifyDelete()
    );
  }

  notifyDelete() {
    if (this.serverResponse.status == 1) {
      alert(this.serverResponse.message);
      this.getUserSpecifiedConnections();
      this.hasChanged = 1;
    } else {
      alert("Failed to remove connections: " + this.serverResponse.message + " " + this.serverResponse.detailed_reason)
    }
  }

  handleFileInput(files: FileList) {
    let fileItem = files.item(0);
    this.file = fileItem;
    console.log("file input has changed. The file is", this.file);
  }

  uploadConnection() {
    this.uploadTouched = true;
    if (this.file != null && this.delimiter != null && this.delimiter != '') {
      console.log("ready to submit");

      var fileupload_payload: FileuploadPayload = {
        'data_action': "upload_connections",   // sending list name here 
        'analysis_name': this.analysis_name,
        'delimiter': this.delimiter,
        'upload_type': "",
        'identifier_type': this.name,
        'filename': this.file.name
      }

      this.uploadSubscription = this.uploadService.postMultipartConnectionData(
        this.file, fileupload_payload
      ).subscribe(
        res => this.serverResponse = res,
        () => console.log(this.serverResponse),
        () => this.notifyUpload()
      );
    }
  }

  notifyUpload() {
    if (this.serverResponse.status == 1) {
      alert("File '" + this.file.name + "' uploaded successfully");
      this.getUserSpecifiedConnections();
      this.hasChanged = 1;
    } else {
      alert("Failed to upload connections: " + this.serverResponse.message + " " + this.serverResponse.detailed_reason)
    }
  }

  close() {
    this.dialogRef.close(this.hasChanged);
  }

}
