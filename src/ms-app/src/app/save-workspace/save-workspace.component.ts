import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material";
import { ServerResponseData } from '../server_response';
import { AnalysisService } from '../analysis.service';
import { StringifyOptions } from 'querystring';

@Component({
  selector: 'app-save-workspace',
  templateUrl: './save-workspace.component.html',
  styleUrls: ['./save-workspace.component.css']
})
export class SaveWorkspaceComponent implements OnInit {

  analysis_name: string;
  message: string;
  show_buttons: boolean;
  filename: string;

  constructor(
      private dialogRef: MatDialogRef<SaveWorkspaceComponent>,
      @Inject(MAT_DIALOG_DATA) data,
      private analysisService: AnalysisService
  ) { 
    this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    //this.dialogRef.updateSize('650px','420px');
  }

  ngOnInit() {
    this.serializeAnalysis();
    this.message = 'Preparing workspace for download. Hang tight this will only take a few mins...';
    this.show_buttons = false;
  }

  serializeAnalysis() {
    this.analysisService.serializeWorkspace(this.analysis_name)
        .subscribe(
            data => this.showResponse(data), 
            () => console.log("observable complete"));
  }

  showResponse(response:ServerResponseData) {
    if(response.status == 1) {
      this.filename = response.message;
      this.message = "File '" + this.filename + ".mslide' is ready.";
      this.show_buttons = true;
    } else if (response.status == 0) {
      this.message = 'Could not prepare workspace file. ' + response.detailed_reason;
      this.show_buttons = false;
    }
  }

  cancel() {
    this.dialogRef.close(this.filename);
  }

  download() {
    this.dialogRef.close(this.filename);
  }

}
