import { Component, OnInit, ViewChild, Input, ComponentFactoryResolver, ViewContainerRef, Type } from '@angular/core';
import { DataUploaderComponent } from '../data-uploader/data-uploader.component'
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription'
import { FileuploadPayload } from '../fileupload_payload'
import { ActivatedRoute } from '@angular/router';
import { ServerResponseData } from '../server_response'
import { PreviewData } from '../preview_data'
import { MetadataMapComponent } from '../metadata-map/metadata-map.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";
import { Router } from '@angular/router';
import { stringify } from '@angular/core/src/util';
import { ReturnStatement } from '@angular/compiler';

@Component({
  selector: 'app-create-analysis',
  templateUrl: './create-analysis.component.html',
  styleUrls: ['./create-analysis.component.css'],
  entryComponents: [DataUploaderComponent]
})
export class CreateAnalysisComponent implements OnInit {

  private uploadSubscription: Subscription;
  analysis_name: string = null;
  clinicalfileToUpload: File = null;
  selectedClinicalDelimiter: string = '';
  submitTouched: boolean = false;
  createTouched: boolean = false;
  clinicalFileUploaded: boolean = false;
  selectedSpecies: string = '';
  allowedClinicalDelimiters: string[] = ['', 'Tab', 'Comma', 'Space', 'Pipe', 'Semi-colon'];
  allowedSpeciesTypes: string[] = ['', 'Homo sapiens', 'Mus musculus', 'Other'];
  allowedSpeciesValues: string[] = ['', 'human', 'mouse', 'Other'];

  serverResponseOnFileAction: string = null;
  serverResponseOnClinicalUpload: ServerResponseData = null;
  serverResponseOnRemove: ServerResponseData = null;
  serverResponseOnCreate: ServerResponseData = null;
  //serverResponseOnPreview: ServerResponseData = null;
  uploadedFiles: FileuploadPayload[] = [];
  filepreview_payload: FileuploadPayload = null;
  
  previewTouched: boolean = false;
  showClosedBook: boolean = false;
  preview_curr_id: number = -1;
  
  //@Input() analysis_name: String;
  @ViewChild('fileInput') fileInputItem: any;

  preview : PreviewData;
  metadataMappingDialogRef: MatDialogRef<MetadataMapComponent>;
  metadata_mappings: string[][] = [];

  /*
  // Keep track of list of generated components for removal purposes
  components = [];

  // Expose class so that it can be used in the template
  dataUploaderComponentClass = DataUploaderComponent;
  */

  constructor(private router: Router, 
    private uploadService: DatauploadService,
    private componentFactoryResolver: ComponentFactoryResolver,
    private container: ViewContainerRef,
    private activatedRoute: ActivatedRoute,
    private dialog: MatDialog
  ) { }
  /*
  addComponent(componentClass: Type<any>) {
    // Create component dynamically inside the ng-template
    const componentFactory = this.componentFactoryResolver.resolveComponentFactory(componentClass);
    const component = this.container.createComponent(componentFactory);
    component.instance.analysis_name = this.analysis_name;
    //component.instance.visibility.subscribe(v => this.serverResponseOnFileRemove);
    component.instance.output.subscribe(event => console.log(event));

    // Push the component so that we can keep track of which components are created
    this.components.push(component);
  }
  */

  /*
  setNumOmics () {
    this.numOmicsDatasetArray = Array(parseInt(this.numOmicsDataset)).fill(0).map((x,i)=>i);
    console.log(this.numOmicsDatasetArray);
  } 
  */

  handleFileUploadSuccess(notification: FileuploadPayload) {
    console.log("handleFileUploadSuccess() called");
    this.serverResponseOnFileAction = "File '" + notification.filename + "' added.";
    this.uploadedFiles.push(notification);
    var t = ["",""];
    this.metadata_mappings.push(t);
    console.log(this.metadata_mappings);
  }

  handleFileInput(files: FileList) {
    let fileItem = files.item(0);
    this.clinicalfileToUpload = fileItem;
    console.log("file input has changed. The file is", this.clinicalfileToUpload);
  }

  handleReset() {
    this.fileInputItem.nativeElement.value = "";
    this.clinicalfileToUpload = null;
    this.submitTouched = false;
  }

  ftlog() {
    console.log(this.selectedClinicalDelimiter);
  }

  splog() {
    console.log(this.selectedSpecies);
  }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.analysis_name = params['analysis_name'];
      for (let key in params) {
        console.log(key + ": " + params[key]);
      }
    });
  }

  ngOnDestroy() {
    if (this.uploadSubscription) {
      this.uploadSubscription.unsubscribe();
    }
  }

  uploadClinicalFile() {
    this.submitTouched = true;
    if (this.clinicalfileToUpload != null && this.selectedClinicalDelimiter != null && this.selectedClinicalDelimiter != '') {
      console.log("ready to submit");

      var fileupload_payload: FileuploadPayload = {
        'data_action': "upload",
        'analysis_name': this.analysis_name,
        'delimiter': this.selectedClinicalDelimiter,
        'upload_type': "clinical-info",
        'identifier_type': "",
        'filename': this.clinicalfileToUpload.name
      }

      this.uploadSubscription = this.uploadService.postMultipartData(
        this.clinicalfileToUpload, fileupload_payload).subscribe(
          res => {
            this.serverResponseOnClinicalUpload = res
            console.log(this.serverResponseOnClinicalUpload)
            this.clinicalFileUploaded = true;
          },
          error => {
            console.log("Server error")
          }
        );

    }
  }

  handleCreate() {
    this.createTouched = true;
    if (this.selectedSpecies != null && this.selectedSpecies != ''
      && this.uploadedFiles.length > 0 && this.clinicalFileUploaded) {

      this.uploadSubscription = this.uploadService.createAnalysis(
        this.analysis_name, this.selectedSpecies)
        .subscribe(
            data => this.serverResponseOnCreate = data, 
				    () => console.log("create error"), 
          	() => this.handleCreateResponse()
        );
    }
  }

  handleCreateResponse() {
    if (this.serverResponseOnCreate.status == 1) {
      // route to visualization home
      this.router.navigateByUrl(
        this.router.createUrlTree(
          ['visualization_home'], { queryParams: { analysis_name: this.analysis_name } }
        )
      );
    }
  }

  togglePreview(id: number) {
    this.filepreview_payload = this.uploadedFiles[id]
    document.getElementById("btn-"+id).innerHTML = document.getElementById("btn-"+id).innerHTML == "Preview" ? "Close Preview" : "Preview";
    
    if(this.preview_curr_id == id){
      if(this.previewTouched == true){
        this.previewTouched = false              
      } else if(this.previewTouched == false){
        this.previewTouched = true
      }
    } else if(this.preview_curr_id != id) {
        this.previewTouched = true        
        this.preview_curr_id = id
    }
  }  

  togglePreview2 (id: number) {
    this.filepreview_payload = this.uploadedFiles[id]
        
    if(this.previewTouched == false) {
      console.log("In case 1")
      // All are currently closed - User Asked to Open One
      this.previewTouched = true
      this.preview_curr_id = id
      //document.getElementById("btn-"+id).innerHTML = "Close Preview"
    } else {
      if(this.preview_curr_id != id) {
        console.log("In case 2")
        // preview_curr_id is Open - User Asked to Open a different One (id)
        //document.getElementById("btn-"+this.preview_curr_id).innerHTML = "Preview"
        //document.getElementById("btn-"+id).innerHTML = "Close Preview"
        this.preview_curr_id = id
      } else {
        console.log("In case 3")
        // preview_curr_id is Open - User Asked to close it
        this.preview_curr_id = -1
        //document.getElementById("btn-"+id).innerHTML = "Preview"
        this.previewTouched = false
        //this.showClosedBook = true;
      }
    }
  }  

  removeObject(id: number) {
    console.log(id);
    console.log("Deleting..")
    var fileupload_payload: FileuploadPayload = this.uploadedFiles[id];
    fileupload_payload.data_action = "remove"

    console.log(fileupload_payload)

    this.uploadSubscription = this.uploadService.removeDataset(
      fileupload_payload).subscribe(
        res => {
          this.serverResponseOnRemove = res
          console.log(res)
          console.log(this.serverResponseOnRemove)
          if (this.serverResponseOnRemove.status == 1) {
            this.serverResponseOnFileAction = "File " + fileupload_payload.filename + " has been removed."
            this.uploadedFiles.splice(id, 1);
          } else {
            this.serverResponseOnFileAction = "File " + fileupload_payload.filename + " could not be removed."
          }
        },
        error => {
          this.serverResponseOnFileAction = "File " + fileupload_payload.filename + " could not be removed."
        }
      );
  }

  openMetadataMapDialog(id: number) {
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '500';
		dialogConfig.width = '200';
		dialogConfig.position = {
			top: '300',
			left: '300'
    };
    
    dialogConfig.data = {
      analysis_name: this.analysis_name,
      expanded_filename: this.get_expanded_filename(this.uploadedFiles[id]),
		};
    this.metadataMappingDialogRef = this.dialog.open(MetadataMapComponent, dialogConfig);
    
    this.metadataMappingDialogRef.afterClosed()
    		.subscribe( data=>this.applyMetdadataMappingChanges(data, id) );
  }
  
  applyMetdadataMappingChanges(return_val: string[], id: number) {
    this.metadata_mappings[id] = return_val;
  }

  /*
  openPreviewDialog(id: number) {
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '500';
		dialogConfig.width = '200';
		dialogConfig.position = {
			top: '300',
			left: '300'
		};
		this.dialog.open(GetPreviewComponent, dialogConfig);
  }
  */

 get_expanded_filename(file_details: FileuploadPayload) {

  var upload_type = "";
  var ut = file_details.upload_type.toLowerCase();
  if (ut == "copy number variation") {
    upload_type = "cnv";
  } else if (ut == "dna methylation") {
    upload_type =  "dna_meth";
  } else if (ut == "gene expression (mrna)") {
    upload_type =  "m_rna";
  } else if (ut == "microrna expression (mirna)") {
    upload_type =  "mi_rna";
  } else if (ut == "protein") {
    upload_type =  "protein";
  } else {
    upload_type = file_details.upload_type;
  }

  var expanded_filename = file_details.analysis_name.toLowerCase()  + "_"
                        + upload_type  + "_"
                        + file_details.identifier_type.toLowerCase()  + "_"
                        + file_details.delimiter.toLowerCase() + "_"
                        + file_details.filename.toLowerCase() ;
  return expanded_filename;
 }

}
