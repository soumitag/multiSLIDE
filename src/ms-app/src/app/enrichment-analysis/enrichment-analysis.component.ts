import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { EnrichmentAnalysisParams } from '../enrichment_analysis_params'
import { SelectionPanelData } from '../selection-panel_data';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ServerResponseData } from '../server_response';
import { AssortedService } from '../assorted.service';
import { MappedData } from '../mapped_data'
import { EnrichmentAnalysisResults } from '../enrichment_analysis_results';
import { SelectedPathwaysResult } from '../selected_pathways_result';
import { AnalysisService } from '../analysis.service';

@Component({
  selector: 'app-enrichment-analysis',
  templateUrl: './enrichment-analysis.component.html',
  styleUrls: ['./enrichment-analysis.component.css']
})
export class EnrichmentAnalysisComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() enrichment_analysis_params: EnrichmentAnalysisParams;
  @Input() data: SelectionPanelData;
  @Input() enrichment_analysis_results: EnrichmentAnalysisResults[];
  @Input() selected_pathways: SelectedPathwaysResult[];
  @Output() notify_changes_applied = new EventEmitter();

  testtypes: string[];
  results_view: number;
  fetching_view: number;
  mapped_data_response: MappedData;
  server_response: ServerResponseData;
  is_selected: boolean[];
  buttonDisabled: boolean = false;

  constructor(
    private assortedService: AssortedService,
    private analysisService: AnalysisService,
    private dialogRef: MatDialogRef<EnrichmentAnalysisComponent>
  ) {
    this.testtypes = ["Parametric", "Non-Parametric"];
    this.dialogRef.updatePosition({ top: '80px', left: '400px' });
    this.fetching_view = 0;
    //this.dialogRef.updateSize('550px', '700px');
    //this.dialogRef.updateSize('750px','700px');
  }

  ngOnInit() {
    this.selected_pathways = [];
    this.is_selected = [];
    for (let i = 0; i < this.enrichment_analysis_results.length; i++) {
      this.is_selected.push(false);
    }
    console.log(this.is_selected);
  }

  toggleResult(result_index: number) {
    this.is_selected[result_index] = !this.is_selected[result_index];
    this.recreateSelectedPathways();
  }

  recreateSelectedPathways() {
    this.selected_pathways = [];
    for (let i = 0; i < this.is_selected.length; i++) {
      if (this.is_selected[i]) {
        let type = this.enrichment_analysis_results[i].type;
        let path = this.enrichment_analysis_results[i].pathid;
        let pathname = this.enrichment_analysis_results[i].pathname;
        let spr = new SelectedPathwaysResult;
        spr.type = type;
        spr.path_id = path;
        spr.path_name = pathname;
        spr.original_index = i;
        this.selected_pathways.push(spr);
      }
    }

  }

  isPathSelected(result_index: number) {
    let selected_path = this.enrichment_analysis_results[result_index].pathid;
    for (var i = 0; i < this.selected_pathways.length; i++) {
      if (this.selected_pathways[i].path_id == selected_path) {
        return true;
      }
    }
    return false;
  }

  removeSelectedPathway(selected: SelectedPathwaysResult) {
    let index = selected.original_index;
    this.toggleResult(index);
  }

  applyChanges() {

    if(this.selected_pathways.length == 0){
      alert("Select functional groups to visualize");
      return;
    }

    this.buttonDisabled = true;

    let ids = [];
    for (var i = 0; i < this.selected_pathways.length; i++) {
      ids.push(this.selected_pathways[i].path_id);
    }

    this.analysisService.initializeAnalysis (
      this.analysis_name, 
      "init_enrichment",
      ids
    ).subscribe(
        data => this.handleAnalysisInitResponse(data), 
        () => console.log("error"));
  }

  handleAnalysisInitResponse(response: ServerResponseData) {
    if (response.status == 1) {
      // ask parent to close dialog
      this.notify_changes_applied.emit();
    } else if (response.status == 0) {
      alert("Changes could not be applied. " + 
            response.message + 
            response.detailed_reason);
      this.buttonDisabled = false;
    }
  }

  saveChanges() {
    //this.dialogRef.close(this.enrichment_analysis_params);
    this.fetching_view = 1;
    this.assortedService.setEnrichmentAnalysisParams(this.analysis_name, this.enrichment_analysis_params)
      .subscribe(data => this.processServerResponseOnSet(data),
        () => console.log("observable complete"));
  }

  processServerResponseOnSet(mapped_data_response: MappedData) {
    if (mapped_data_response == null || mapped_data_response.status == 0) {
      alert("Enrichment Analysis parameters update FAILED. " + mapped_data_response.message + ". " + mapped_data_response.detailed_reason);
    } else {
      /*
      alert("Enrichment Analysis parameters updated.");
      */
      this.results_view = 1;
      this.fetching_view = 0;
      let f = JSON.parse(mapped_data_response.values[0].replace(/\'/g, "\""));
      console.log(f);
      this.is_selected = []
      for (let i = 0; i < f.length; i++) {
        this.is_selected.push(false);
      }
      console.log(this.is_selected);
      this.enrichment_analysis_results = f;
    }
  }

  saveEnrichmentAnalysisFile() {
    let filename = "Enrichment_Analysis_Results.txt";
    this.assortedService.getEnrichmentAnalysisFile(this.analysis_name, filename)
      .subscribe(
        data => this.downloadFile(data, filename),
        () => console.log("observable complete"));
  }

  downloadFile(data, filename: string) {
    const blob = new Blob([data], { type: 'application/download' });
    const url= window.URL.createObjectURL(blob);
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style.display = "none";
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
