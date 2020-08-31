import { Component, OnInit, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { FileuploadPayload } from '../fileupload_payload';
import { ServerResponseData } from '../server_response';
import { DatauploadService } from '../dataupload.service';
import { Subscription } from 'rxjs/Subscription';
import { NgForm } from '@angular/forms';
import { AssortedService } from '../assorted.service';
import { FunctionalGroupContainer } from '../functional_group_container';
import { MappedData } from '../mapped_data'
import { AnalysisService } from '../analysis.service';

@Component({
  selector: 'app-upload-search-tab',
  templateUrl: './upload-search-tab.component.html',
  styleUrls: ['./upload-search-tab.component.css']
})
export class UploadSearchTabComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() uploaded_filename: string;
  @Input() functional_groups_in_file: FunctionalGroupContainer[];
  @Input() selected_functional_groups: FunctionalGroupContainer[];
  @Output() notify_changes_applied = new EventEmitter();

  fileToUpload: File = null;
  submitTouched: Boolean = false;
  removeTouched: Boolean = false;
  buttonDisabled: Boolean = false;
  //selectedDelimiter: string = '';
  //selectedIdentifier: string = '';
  //selectedUploadType: string = '';
  server_response: ServerResponseData = null;
  fileupload_payload: FileuploadPayload = null;
  upload_message: string = '';
  //functional_grp_names_response: FunctionalGroupNames;
  mapped_data_response: MappedData;
  currently_selected_functional_group: string;

  @ViewChild('fileInput', { static: true }) fileInputItem: any;
  @ViewChild('uploadForm', { static: true }) uploadForm: NgForm;

  private uploadSubscription: Subscription;

  constructor(
    private assortedService: AssortedService,
    private uploadService: DatauploadService,
    private analysisService: AnalysisService) { }

  ngOnInit() {
    if (this.uploaded_filename != null && this.uploaded_filename != '') {
      this.upload_message = 'File ' + this.uploaded_filename + ' uploaded successfully';
    }
  }

  ngOnDestroy() {
    if (this.uploadSubscription) {
      this.uploadSubscription.unsubscribe();
    }
  }

  handleFileInput(files: FileList) {
    let fileItem = files.item(0);
    this.fileToUpload = fileItem;
  }

  getFunctionalGroups() {
    // when subscribe succeeds populate this.functional_group_names with the data returned by the service
    this.assortedService.getFunctionalGroupsData(this.analysis_name)
      .subscribe(data => this.processServerResponseOnSet(data),
        () => console.log("observable complete"));
  }

  processServerResponseOnSet(mapped_data_response: MappedData) {
    if (mapped_data_response == null || mapped_data_response.status == 0) {
      alert("Could not retrieve functional names. " + mapped_data_response.message + ". " + mapped_data_response.detailed_reason);
    } else {
      this.functional_groups_in_file = JSON.parse(mapped_data_response.values[0].replace(/\'/g, "\""));
      if (this.functional_groups_in_file.length > 0) {
        this.currently_selected_functional_group = this.functional_groups_in_file[0].functional_grp_name;
      }
    }
  }

  handleSubmit() {
    this.submitTouched = true;
    if (this.fileToUpload != null) {
      console.log("ready to submit");

      var fileupload_payload: FileuploadPayload = {
        'data_action': "upload_add_genes",
        'analysis_name': this.analysis_name,
        'delimiter': "Tab",
        'upload_type': "add_genes",
        'identifier_type': "",    // reserved for future use
        'filename': this.fileToUpload.name
      }

      this.uploadSubscription = this.uploadService.postMultipartData(
        this.fileToUpload, fileupload_payload).subscribe(
          res => {
            this.server_response = res
            if (this.server_response.status == 1) {
              this.upload_message = this.server_response.message;
              this.getFunctionalGroups();
            } else if (this.server_response.status == 0) {
              this.upload_message = 'Upload Failed.';
              alert(this.server_response.message);
            }
          },
          error => {
            this.server_response = error
          }
        );
    }
  }

  addFunctionalGroup() {
    if (this.currently_selected_functional_group != null && !this.isFunctionalGroupSelected(this.currently_selected_functional_group)) {
      this.selected_functional_groups.push(this.getFunctionalGroup(this.currently_selected_functional_group));
    }
  }

  removeFunctionalGroup(func_grp: FunctionalGroupContainer) {
    let index = this.selected_functional_groups.indexOf(func_grp);
    this.selected_functional_groups.splice(index, 1);
  }

  isFunctionalGroupSelected(functional_grp_name: string) {
    for (var i = 0; i < this.selected_functional_groups.length; i++) {
      if (this.selected_functional_groups[i].functional_grp_name == functional_grp_name) {
        return true;
      }
    }
    return false;
  }

  getFunctionalGroup(functional_grp_name: string) {
    for (var i = 0; i < this.functional_groups_in_file.length; i++) {
      if (this.functional_groups_in_file[i].functional_grp_name == functional_grp_name) {
        return this.functional_groups_in_file[i];
      }
    }
    return null;
  }

  applyChanges() {

    if (this.selected_functional_groups.length == 0) {
      alert("Select functional groups to visualize");
      return;
    }

    this.buttonDisabled = true;

    let selected_functional_group_ids: string[] = [];
    for (var i = 0; i < this.selected_functional_groups.length; i++) {
      selected_functional_group_ids.push(this.selected_functional_groups[i]._id);
    }
    console.log(selected_functional_group_ids);

    this.analysisService.initializeAnalysis(
      this.analysis_name,
      "init_upload",
      selected_functional_group_ids
    ).subscribe(
      data => this.handleAnalysisInitResponse(data),
      () => console.log("error"));

    console.log("1");
  }

  handleAnalysisInitResponse(response: ServerResponseData) {
    if (response.status == 1) {
      //this.dialogRef.close();
      // ask parent to close dialog
      this.notify_changes_applied.emit();
    } else if (response.status == 0) {
      alert("Changes could not be applied. " +
        response.message +
        response.detailed_reason);
      this.buttonDisabled = false;
    }
  }

}
