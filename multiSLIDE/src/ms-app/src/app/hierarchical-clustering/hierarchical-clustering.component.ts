import { Component, OnInit, Input } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-hierarchical-clustering',
  templateUrl: './hierarchical-clustering.component.html',
  styleUrls: ['./hierarchical-clustering.component.css']
})
export class HierarchicalClusteringComponent implements OnInit {

  @Input() dataset_names: string[];
    
  selectedDataset: string = '';
  toggleRow: boolean = false;
  toggleCol: boolean = false;
  showRowClustParams: boolean = false;
  showColClustParams: boolean = false;  
  showDefaultRowClustParams: boolean = false;
  showDefaultColClustParams: boolean = false;

  
  constructor(private dialogRef: MatDialogRef<HierarchicalClusteringComponent>) {
    //this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('550px','750px');
  }

  ngOnInit() {    
  }

  toggleRowClustering(){
    //console.log("in toggleRowClustering");
    if(this.toggleRow == false){
      this.toggleRow = true;  
      this.showDefaultRowClustParams = true;  
      this.showRowClustParams = false;     
    } else {
      this.toggleRow = false;
      this.showRowClustParams = false; 
      this.showDefaultRowClustParams = false;     
    }
  }

  toggleColClustering() {
    if(this.toggleCol == false){
      this.toggleCol = true;  
      this.showDefaultColClustParams = true;  
      this.showColClustParams = false;     
    } else {
      this.toggleCol = false;
      this.showColClustParams = false; 
      this.showDefaultColClustParams = false;     
    }
  }

  showDefaultRowClusteringParams(){
    //console.log("in showDefaultClusteringParams");
    if(this.showRowClustParams == true){
      this.showRowClustParams = false;
    }
    if(this.toggleRow == true){
      this.showRowClustParams == false
      this.showDefaultRowClustParams = true;
    } else {
      this.showDefaultRowClustParams = false;
      this.showRowClustParams == false;
    }
  }


  specifyRowClusteringParams(){
    //console.log("in specifyRowClusteringParams");
    if(this.toggleRow == true){
      this.showRowClustParams = true;
      this.showDefaultRowClustParams = false;
    } else {
      this.showRowClustParams = false;
      this.showDefaultRowClustParams = false;
    }
  }
  

  toggleShowColClusteringParams(){
    if(this.toggleCol == false){
      this.toggleCol = true;  
      this.showDefaultColClustParams = true;  
      this.showColClustParams = false;     
    } else {
      this.toggleCol = false;
      this.showColClustParams = false; 
      this.showDefaultColClustParams = false;     
    }
  }

  showDefaultColClusteringParams(){
    //console.log("in showDefaultClusteringParams");
    if(this.showColClustParams == true){
      this.showColClustParams = false;
    }
    if(this.toggleCol == true){
      this.showColClustParams == false
      this.showDefaultColClustParams = true;
    } else {
      this.showDefaultColClustParams = false;
      this.showColClustParams == false;
    }
  }


  specifyColClusteringParams(){
    //console.log("in specifyRowClusteringParams");
    if(this.toggleCol == true){
      this.showColClustParams = true;
      this.showDefaultColClustParams = false;
    } else {
      this.showColClustParams = false;
      this.showDefaultColClustParams = false;
    }
  }
}
