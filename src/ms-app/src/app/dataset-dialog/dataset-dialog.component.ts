import { Component, OnInit, Input, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-dataset-dialog',
  templateUrl: './dataset-dialog.component.html',
  styleUrls: ['./dataset-dialog.component.css']
})
export class DatasetDialogComponent implements OnInit {

  analysis_name: string;
  datasets: string[];
  selected_datasets: string[];
  selected_dataset: string;

  phenotypes: string[];
  selected_phenotypes: string[];
  selected_phenotype: string;

  dialogNum: number;

  constructor(private datasetSelectionDialogRef: MatDialogRef<DatasetDialogComponent>,
    @Inject(MAT_DIALOG_DATA) data) {

    this.analysis_name = data.analysis_name;
    this.datasets = data.datasets;
    this.selected_datasets = data.selected_datasets;
    this.phenotypes = data.phenotypes;
    this.selected_phenotypes = data.selected_phenotypes;
    this.dialogNum = data.dialogNum;
    this.datasetSelectionDialogRef.updatePosition({ top: '110px', left: '700px' });
    this.datasetSelectionDialogRef.updateSize('500px', '600px');
    console.log(this.phenotypes);
    console.log(data.phenotypes);

  }

  ngOnInit(){}

  addPhenotype() {
    console.log(this.selected_phenotypes);
    if (!this.selected_phenotypes.includes(this.selected_phenotype)) {
      this.selected_phenotypes.push(this.selected_phenotype);
    }
    console.log(this.selected_phenotypes);
  }

  removePhenotype(phenotype_name: string) {
    console.log(this.selected_phenotypes);
    console.log(this.selected_phenotypes.indexOf(phenotype_name));
    this.selected_phenotypes.splice(this.selected_phenotypes.indexOf(phenotype_name), 1);
    console.log(this.selected_phenotypes);
  }


  addDataset() {
    console.log(this.selected_datasets);
    if (!this.selected_datasets.includes(this.selected_dataset)) {
      this.selected_datasets.push(this.selected_dataset);
    }
    console.log(this.selected_datasets);
  }

  removeDataset(dataset_name: string) {
    console.log(this.selected_datasets);
    console.log(this.selected_datasets.indexOf(dataset_name));
    this.selected_datasets.splice(this.selected_datasets.indexOf(dataset_name), 1);
    console.log(this.selected_datasets);
  }

  applyChanges() {
    if(this.dialogNum == 0) {
      if(this.selected_datasets.length == 0) {
        alert("Select atleast one dataset before applying changes");
      } else {
        this.datasetSelectionDialogRef.close(1);
      }
    } else if (this.dialogNum == 1) {
      this.datasetSelectionDialogRef.close(1);
    }
  }

  close() {
    this.datasetSelectionDialogRef.close(0);
  }

}
