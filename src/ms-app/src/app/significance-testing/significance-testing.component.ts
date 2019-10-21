import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material";
import { SignificanceTestingParams } from '../significance_testing_params';
import { SignificanceTestingService } from '../significance-testing.service'
import { ServerResponseData } from '../server_response';

@Component({
  selector: 'app-significance-testing',
  templateUrl: './significance-testing.component.html',
  styleUrls: ['./significance-testing.component.css']
})
export class SignificanceTestingComponent implements OnInit {

  analysis_name: string;
  dataset_names: string[];
  phenotypes: string[];
  significance_testing_params: SignificanceTestingParams;
  showForm: boolean = true;

  constructor(
    private sigtestService: SignificanceTestingService, 
    private dialogRef: MatDialogRef<SignificanceTestingComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) { 
    this.analysis_name = data.analysis_name;
    this.significance_testing_params = data.significance_testing_params;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('550px','440px');
  }

  ngOnInit() {
    this.getParams();
  }

  getParams(): void {
    this.sigtestService.getDatasetsAndPhenotypes(this.analysis_name)
      .subscribe(data => this.processDatasetsFromServer(data), 
                () => console.log("observable complete"));
  }

  /*
  processSignificanceTestingFromServer(significance_testing_params: SignificanceTestingParams) {
    if (significance_testing_params == null) {
        alert("Failed to get current significance testing parameters from server. Falling back on default values.");
        this.significance_testing_params = new SignificanceTestingParams();
        this.significance_testing_params.dataset = this.dataset_names[0];
        this.significance_testing_params.phenotype = this.phenotypes[0];
      } else {
        this.significance_testing_params = significance_testing_params;
      }
  }
  */

  processDatasetsFromServer(datasets_and_phenotypes: string[][]) {
    if (datasets_and_phenotypes == null || datasets_and_phenotypes.length < 2 || datasets_and_phenotypes[0].length == 0) {
      alert("Failed to get datasets and phenotypes from server. This could be because no dataset or phenotype is selected. Select dataset(s) and phenotype(s) in selection panel, apply changes and try again.");
    } else {
      this.dataset_names = datasets_and_phenotypes[0];
      this.phenotypes = datasets_and_phenotypes[1];
    }
  }

  saveChanges() {
    this.dialogRef.close(this.significance_testing_params);
    /*
    this.showForm = false;
    this.sigtestService.setSignificanceTestingParams(this.analysis_name, this.significance_testing_params)
      .subscribe(data => this.processServerResponseOnSet(data), 
                () => console.log("observable complete"));
    */
  }

  close() {
    this.dialogRef.close();
  }

  /*
  processServerResponseOnSet (response: ServerResponseData) {
    this.showForm = true;
    if (response == null || response.status == 0) {
      alert("Significance testing parameter update FAILED. " + response.message + ". " + response.detailed_reason);
    } else {
      alert("Significance testing parameters updated.");
      this.dialogRef.close(this.significance_testing_params);
    }
  }
  */
}
