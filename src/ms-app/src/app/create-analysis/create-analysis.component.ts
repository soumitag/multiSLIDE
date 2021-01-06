import { Component, OnInit, ViewChild, Input, ComponentFactoryResolver, ViewContainerRef, Type } from '@angular/core';
import { DataUploaderComponent } from '../data-uploader/data-uploader.component'
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription'
import { FileuploadPayload } from '../fileupload_payload'
import { DatasetSpecs } from '../dataset_specs'
import { ActivatedRoute } from '@angular/router';
import { ServerResponseData } from '../server_response'
import { PreviewData } from '../preview_data'
import { MetadataMapComponent } from '../metadata-map/metadata-map.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material/dialog";
import { Router } from '@angular/router';
/*import { stringify } from '@angular/core/src/util';*/
import { ReturnStatement, Identifiers } from '@angular/compiler';
/*import { AnimationGroupPlayer } from '@angular/animations/src/players/animation_group_player';*/
import deepEqual from "deep-equal";

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
  identifierCols = new Map([['entrez_2021158607524066', 'Entrez'],
                            ['genesymbol_2021158607524066', 'Gene Symbol'],
                            ['refseq_2021158607524066', 'RefSeq ID'],
                            ['ensembl_gene_id_2021158607524066', 'Ensembl gene ID'],
                            ['uniprot_id_2021158607524066', 'UniProt ID'],
                            ['mirna_id_2021158607524066', 'miRNA ID'],
                            ['other_id_2021158607524066', 'Others']]);

  serverResponseOnFileAction: string = null;
  serverResponseOnClinicalUpload: ServerResponseData = null;
  serverResponseOnRemove: ServerResponseData = null;
  serverResponseOnCreate: ServerResponseData = null;
  serverResponseOnSpecUpdate: ServerResponseData = null;
  //serverResponseOnPreview: ServerResponseData = null;
  uploadedFiles: DatasetSpecs[] = [];
  filepreview_payload: DatasetSpecs = null;
  
  previewTouched: boolean = false;
  showClosedBook: boolean = false;
  preview_curr_id: number = -1;

  missing_metadata_dataset: string;
  
  //@Input() analysis_name: String;
  @ViewChild('fileInput', { static: true }) fileInputItem: any;

  preview : PreviewData;
  metadataMappingDialogRef: MatDialogRef<MetadataMapComponent>;


  constructor(private router: Router, 
    private uploadService: DatauploadService,
    private componentFactoryResolver: ComponentFactoryResolver,
    private container: ViewContainerRef,
    private activatedRoute: ActivatedRoute,
    private dialog: MatDialog
  ) { }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.analysis_name = params['analysis_name'];
      for (let key in params) {
        console.log(key + ": " + params[key]);
      }
    });
    this.getDatasetSpecs();
  }

  handleFileUploadSuccess() {
    this.serverResponseOnFileAction = "File has been uploaded."
    this.getDatasetSpecs();
  }

  getDatasetSpecs() {
    this.uploadSubscription = this.uploadService.getDatasetSpecs(this.analysis_name)
      .subscribe(
          data => this.uploadedFiles = data, 
          () => console.log("create error")
      );
  }

  prepDelimiterForDisplay(delimiter: string) {
    let d = delimiter.charAt(0).toUpperCase() + delimiter.slice(1);
    return d;
  }

  prepIdentifierForDisplay(identifier: string) {
    return this.identifierCols.get(identifier);
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

  /*
  ftlog() {
    console.log(this.selectedClinicalDelimiter);
  }

  splog() {
    console.log(this.selectedSpecies);
  }
  */

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
      && this.uploadedFiles.length > 0 && this.clinicalFileUploaded
      && this.validateIdentifierMappings()) {

      this.uploadSubscription = this.uploadService.createAnalysis(
        this.analysis_name, this.selectedSpecies)
        .subscribe(
            data => this.serverResponseOnCreate = data, 
				    () => console.log("create error"), 
          	() => this.handleCreateResponse()
        );
    }
  }

  validateIdentifierMappings() {
    for (let i=0; i<this.uploadedFiles.length; i++) {
      let ds = this.uploadedFiles[i];
      if (!ds.has_linker && !ds.has_additional_identifiers) {
        this.missing_metadata_dataset = ds.display_name;
        return false;
      }
      if (ds.has_linker) {
        if (ds.linker_colname == null || ds.linker_colname == '' || ds.linker_identifier_type == null || ds.linker_identifier_type == '') {
          this.missing_metadata_dataset = ds.display_name;
          return false;
        }
      }
      if (ds.has_additional_identifiers && ds.identifier_metadata_columns.length == 0) {
        this.missing_metadata_dataset = ds.display_name;
        return false;
      }
    }
    return true;
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
    } else {
      if(this.preview_curr_id != id) {
        console.log("In case 2")
        this.preview_curr_id = id
      } else {
        console.log("In case 3")
        this.preview_curr_id = -1
        this.previewTouched = false
      }
    }
  }  

  removeObject(id: number) {

    this.uploadSubscription = this.uploadService.removeDataset(
      this.analysis_name, 
      this.uploadedFiles[id].expanded_filename
    ).subscribe(
        res => {
          this.serverResponseOnRemove = res
          if (this.serverResponseOnRemove.status == 1) {
            this.serverResponseOnFileAction = "File " + this.uploadedFiles[id].filename + " has been removed."
            this.uploadedFiles.splice(id, 1);
          } else {
            this.serverResponseOnFileAction = "File " + this.uploadedFiles[id].filename + " could not be removed."
          }
        },
        error => {
          this.serverResponseOnFileAction = "File " + this.uploadedFiles[id].filename + " could not be removed."
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
      spec: JSON.parse(JSON.stringify(this.uploadedFiles[id]))
      //expanded_filename: this.get_expanded_filename(this.uploadedFiles[id]),
		};
    this.metadataMappingDialogRef = this.dialog.open(MetadataMapComponent, dialogConfig);
    
    this.metadataMappingDialogRef.afterClosed()
    		.subscribe( data=>this.applyMetdadataMappingChanges(data, id) );
  }
  
  applyMetdadataMappingChanges(new_spec: DatasetSpecs, id: number) {
    if (new_spec) {
      if (!deepEqual(new_spec, this.uploadedFiles[id])) {
        this.uploadSubscription = this.uploadService.updateDatasetSpecs(this.analysis_name, new_spec)
          .subscribe(
            data => this.handleServerResponseOnSpecUpdate(data, new_spec, id), 
            () => console.log("Dataset spec update error")
          );
      }
    }
  }

  handleServerResponseOnSpecUpdate(res: ServerResponseData, new_spec: DatasetSpecs, id: number) {
    if (res.status == 1) {
      this.uploadedFiles[id] = new_spec;
    }
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

 getUploadType(omics_type: string) {

  if (omics_type == "cnv") {
    return "Copy Number Variation";
  } else if (omics_type == "dna_meth") {
    return "DNA Methylation";
  } else if (omics_type == "m_rna") {
    return "Gene Expression (mRNA)";
  } else if (omics_type == "mi_rna") {
    return "microRNA Expression (miRNA)";
  } else if (omics_type == "protein") {
    return "Protein";
  } else {
    return "Other";
  }
 }

}
