import { Component, OnInit, Input, Inject } from '@angular/core';
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription'
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { FileuploadPayload } from '../fileupload_payload';
import { ServerResponseData } from '../server_response';

@Component({
  selector: 'app-upload-list',
  templateUrl: './upload-list.component.html',
  styleUrls: ['./upload-list.component.css']
})
export class UploadListComponent implements OnInit {

  private uploadSubscription: Subscription;

  analysis_name: string;
  listnameStr: string;
  listNameErrMsg: string = "";
  listNameErr: boolean = false;
  listfileToUpload: File = null;
  listFileUploaded: boolean = false;
  uploadTouched: boolean = false;
  selectedDelimiter: string = '';
  selectedIdentifier: string = '';
  allowedDelimiters: string[] = ['', 'Tab', 'Comma', 'Space', 'Pipe', 'Semi-colon', 'Line'];
  standard_gene_identifiers: string[] = ['entrez','genesymbol','refseq',
                            'ensembl_gene_id','ensembl_transcript_id',
                            'ensembl_protein_id','uniprot_id', 'mirna_id']
  allowedIdentifiers: string[] = ['Entrez', 'Gene Symbol', 'Refseq', 'Ensembl gene ID', 'Ensembl transcript ID', 'Ensembl protein ID', 'UniProt ID', 'miRNA ID'];

  serverResponseOnListUpload: ServerResponseData = null;

  constructor(private dialogRef: MatDialogRef<UploadListComponent>, private uploadService: DatauploadService, @Inject(MAT_DIALOG_DATA) data) {
    this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('550px', '500px');
  }

  ngOnInit() {
  }

  ftlog() {
    console.log(this.selectedDelimiter);
  }

  idlog() {
    console.log(this.selectedIdentifier);
  }

  handleFileInput(files: FileList) {
    let fileItem = files.item(0);
    this.listfileToUpload = fileItem;
    console.log("file input has changed. The file is", this.listfileToUpload);
  }

  /*
  uploadList() {
    this.uploadTouched = true;
    if (this.listfileToUpload != null && this.selectedDelimiter != null && this.selectedDelimiter != '' && this.selectedIdentifier != null && this.selectedIdentifier != '') {
      console.log("ready to submit");

      var fileupload_payload: FileuploadPayload = {
        'data_action': this.listnameStr,   // sending list name here 
        'analysis_name': this.analysis_name,
        'delimiter': this.selectedDelimiter,
        'upload_type': "",
        'identifier_type': this.selectedIdentifier,
        'filename': this.listfileToUpload.name
      }

      this.uploadSubscription = this.uploadService.postMultipartListData(
        this.listfileToUpload, fileupload_payload
      ).subscribe(
        res => this.serverResponseOnListUpload = res,
        () => console.log(this.serverResponseOnListUpload),
        () => this.notifyUploadListStatus()
      );
    }
  }
  */

  notifyUploadListStatus() {
    if (this.serverResponseOnListUpload.status == 1) {
      this.listFileUploaded = true;
      alert(this.serverResponseOnListUpload.detailed_reason);
      this.dialogRef.close(1);
    } else {
      alert("Failed to create list: " + this.serverResponseOnListUpload.message + " " + this.serverResponseOnListUpload.detailed_reason)
      this.dialogRef.close(0);
    }
  }

}
