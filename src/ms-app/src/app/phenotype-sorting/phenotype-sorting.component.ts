import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { PhenotypeSortingParams } from '../phenotype_sorting_params'

@Component({
  selector: 'app-phenotype-sorting',
  templateUrl: './phenotype-sorting.component.html',
  styleUrls: ['./phenotype-sorting.component.css']
})
export class PhenotypeSortingComponent implements OnInit {

  analysis_name: string;
  params: PhenotypeSortingParams;
  phenotypes: string[];

  constructor(
    private dialogRef: MatDialogRef<PhenotypeSortingComponent>,
    @Inject(MAT_DIALOG_DATA) data
  ) { 
    this.analysis_name = data.analysis_name;
    this.phenotypes = data.phenotypes;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('650px','500px');
    if (data.params.phenotypes.length >= 5) {
      var p: string[] = [];
      var s: boolean[] = [];
      this.params = new PhenotypeSortingParams(p,s);
      for (var i=0; i<5; i++) {
        this.params.phenotypes.push(data.params.phenotypes[i]);
        this.params.sort_orders.push(data.params.sort_orders[i]);
      }
    } else {
      this.params = data.params;
      for (var i=this.params.phenotypes.length; i<5; i++) {
        this.params.phenotypes.push('');
        this.params.sort_orders.push(true);
      }
    }
  }

  ngOnInit() {}

  saveChanges() {
    this.dialogRef.close(this.extractData());
  }

  close() {
    this.dialogRef.close();
  }

  extractData() {
    let p: string[] = [];
    let s: boolean[] = [];
    for (var i=0; i<this.params.phenotypes.length; i++) {
      if (this.params.phenotypes[i] != '') {
        p.push(this.params.phenotypes[i]);
        s.push(this.params.sort_orders[i]);
      }
    }
    console.log(p);
    return new PhenotypeSortingParams(p,s);
  }

  setPhenotype(value: string, index: number) {
    this.params.phenotypes[index] = value;
    console.log(this.params.phenotypes);
  }
  
  setSortOrder(value: string, index: number) {
    console.log(value);
    this.params.sort_orders[index] = value == 'true';
    console.log(this.params.sort_orders);
  }
}
