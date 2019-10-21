import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material";
import { ServerResponseData } from '../server_response';
import { AnalysisService } from '../analysis.service';
import { LocalSettings } from '../local-settings'

@Component({
  selector: 'app-close-analysis',
  templateUrl: './close-analysis.component.html',
  styleUrls: ['./close-analysis.component.css']
})
export class CloseAnalysisComponent implements OnInit {

  analysis_name: string;
  message: string;
  is_closed: boolean;

  constructor(
    private dialogRef: MatDialogRef<CloseAnalysisComponent>,
    @Inject(MAT_DIALOG_DATA) data,
    private analysisService: AnalysisService
  ) { 
    this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('650px','260px');
    this.is_closed = false;
  }

  ngOnInit() {}

  deleteAndForget() {
    this.analysisService.deleteAnalysis(this.analysis_name)
        .subscribe(
            data => this.showResponse(data), 
            () => console.log("observable complete"));
  }

  showResponse(response:ServerResponseData) {
    if(response.status == 1) {
      this.dialogRef.disableClose = true;
      this.message = "Analysis '" + this.analysis_name + "' has been removed from server.";
      this.is_closed = true;
    } else if (response.status == 0) {
      alert('Could not close analysis. Possibly because session has timed-out due to more than an hour of inactivity.');
    }
  }

  goHome() {
    window.open(LocalSettings.HOME_URL, '_blank');
  }
}
