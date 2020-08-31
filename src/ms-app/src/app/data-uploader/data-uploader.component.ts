import { Component, Input, Output, OnInit, OnDestroy, ViewChild, EventEmitter } from '@angular/core';
import { NgForm } from '@angular/forms';
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription';
import { ServerResponseData } from '../server_response'
import { FileuploadPayload } from '../fileupload_payload'

@Component({
  selector: 'app-data-uploader',
  templateUrl: './data-uploader.component.html',
  styleUrls: ['./data-uploader.component.css']
})

export class DataUploaderComponent implements OnInit, OnDestroy {

  @Input() analysis_name: string;
  @Output() notifySuccessfullFileUpload = new EventEmitter<FileuploadPayload>();
  
  private uploadSubscription: Subscription;
  fileToUpload: File  = null;
  submitTouched: Boolean = false;
  removeTouched: Boolean = false;
  selectedDelimiter: string = '';
  selectedIdentifier: string = '';
  selectedUploadType: string = '';
  selectedUploadTypeCode: string = '';
  allowedDelimiters: string[] = ['','Tab','Comma','Space','Pipe','Semi-colon'];
  allowedIdentifiers: string[] = ['', 'Entrez', 'Gene Symbol', 'RefSeq ID', 'Ensembl Gene ID', 'Ensembl Transcript ID', 'Ensembl Protein ID', 'UniProt ID']
  allowedUploadTypes: string[] = ['', 
                                  'Copy Number Variation', 'DNA Methylation', 'Transcriptome', 'microRNA Expression', 
                                  'Protein', 'Phosphoproteome', 'Gene Isoform Expression', 'Metabolome', 'Lipidome', 'Others']
  allowedUploadTypeCodes: string[] = ['', 
                                  'cnv', 'dna_meth', 'm_rna', 'mi_rna', 'protein', 'phosphoproteome', 'gene_isoform_expression', 
                                  'metabolome', 'lipidome', 'others']                             
  _ref:any;
  server_response: ServerResponseData = null;
  fileupload_payload: FileuploadPayload = null;

  @ViewChild('fileInput', { static: true }) fileInputItem: any;
  @ViewChild('uploadForm', { static: true }) uploadForm: NgForm;

  constructor(private uploadService: DatauploadService) {}

  ngOnInit() { 
  }

  ngOnDestroy() {
    if(this.uploadSubscription) {
      this.uploadSubscription.unsubscribe();
    }
  }

  handleFileInput(files: FileList) {
    let fileItem = files.item(0);
    this.fileToUpload = fileItem;
    console.log("file input has changed. The file is", this.fileToUpload);
  }

  handleReset() {
    this.uploadForm.reset()
    this.fileInputItem.nativeElement.value = "";
    this.fileToUpload = null;
    this.submitTouched = false;
    //this.server_response = null;
  }

  uplog() {
    console.log(this.selectedUploadType);
  }

  ftlog() {
    console.log(this.selectedDelimiter);
  }

  idlog(){
    console.log(this.selectedIdentifier);
  }

  handleSubmit() {
    this.submitTouched = true;
    if (this.fileToUpload != null && this.selectedDelimiter != null && this.selectedDelimiter != '') {

      let i = this.allowedUploadTypes.indexOf(this.selectedUploadType);
      this.selectedUploadTypeCode = this.allowedUploadTypeCodes[i];

      console.log("ready to submit");

      var fileupload_payload: FileuploadPayload = {
        'data_action' : "upload",
        'analysis_name' : this.analysis_name,
        'delimiter' : this.selectedDelimiter,
        'upload_type' : this.selectedUploadTypeCode,
        'identifier_type' : this.selectedIdentifier,
        'filename': this.fileToUpload.name
      }

      /*
      var formData = new FormData();
      formData.append("data_action", this.data_action + "");
      formData.append("analysis_name", this.analysis_name + "");
      formData.append("delimiter", this.selectedDelimiter + "");
      formData.append("upload_type", this.selectedUploadType + "");
      formData.append("identifier_type", this.selectedIdentifier + "");
      */
      
      this.uploadSubscription = this.uploadService.postMultipartData(
                                  this.fileToUpload, fileupload_payload).subscribe(
                                    res => {
                                      this.server_response = res
                                      if (this.server_response.status == 0) {

                                      } else if (this.server_response.status == 1) {
                                        this.handleReset()
                                        this.notifySuccessfullFileUpload.emit(fileupload_payload)
                                      }
                                    },
                                    error=>{
                                      this.server_response = error                                
                                    }
                                  ); 
    }
  }

  
}
