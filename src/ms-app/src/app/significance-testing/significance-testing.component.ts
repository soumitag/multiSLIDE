import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { SignificanceTestingParams } from '../significance_testing_params';
import { SignificanceTestingService } from '../significance-testing.service'
import { ServerResponseData } from '../server_response';
import { AssortedService } from '../assorted.service';

@Component({
  selector: 'app-significance-testing',
  templateUrl: './significance-testing.component.html',
  styleUrls: ['./significance-testing.component.css']
})
export class SignificanceTestingComponent implements OnInit {

  analysis_name: string;
  dataset_names: string[];
  phenotypes: string[];
  testtypes: string[];
  significance_testing_params: SignificanceTestingParams;
  showForm: boolean = true;

  constructor(
    private sigtestService: SignificanceTestingService, 
    private dialogRef: MatDialogRef<SignificanceTestingComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) { 
    this.analysis_name = data.analysis_name;
    this.dataset_names = data.dataset_names;
    this.phenotypes = data.phenotypes;
    this.testtypes = ['Parametric','Non-parametric'];
    this.significance_testing_params = data.significance_testing_params;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('550px','700px');
  }

  ngOnInit() {}

  saveChanges() {
    this.dialogRef.close(this.significance_testing_params);
  }

  close() {
    this.dialogRef.close();
  }

}
