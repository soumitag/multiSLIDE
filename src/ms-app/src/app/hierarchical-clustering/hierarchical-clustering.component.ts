import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ClusteringParams } from '../clustering_params';
/*import { ClusteringService } from '../clustering.service' */
import { ServerResponseData } from '../server_response';

@Component({
  selector: 'app-hierarchical-clustering',
  templateUrl: './hierarchical-clustering.component.html',
  styleUrls: ['./hierarchical-clustering.component.css']
})
export class HierarchicalClusteringComponent implements OnInit {

  analysis_name: string;
  clustering_params: ClusteringParams;
  dataset_names: string[];

  toggleView: boolean = false;
  showClustParams: boolean = false;
  showDefaultClustParams: boolean = false;
  rc_type: number;

  /*
  toggleCol: boolean = false;
  showColClustParams: boolean = false;  
  showDefaultColClustParams: boolean = false;
  */

  linkage_function_values = [0,1,2,3,4,5,6]
  linkage_function_disps = ['Average','Complete','Median','Centroid','Ward','Weighted','Single']
  distance_function_values = [0,1,2,3,4]
  distance_function_disps = ['Euclidean','Manhattan','Cosine','Correlation','Chebyshev']
  leaf_ordering_values = [0,1,2,3,4];
  leaf_ordering_disps = ['Optimal (can be slow for large datasets)', 'Smallest Child First (based on count)', 'Largest Child First (based on count)', 
  'Closest Child First (based on distance)', 'Farthest Child First (based on distance)'];
  //leaf_ordering_disps = ['Smallest Child First','Largest Child First','Most Diverse Child First','Least Diverse Child First'];
  //allowed_aggregate_values = ['Mean', 'Max', 'Sum'];
  
  TYPE_ROW: number = 0;
  TYPE_COL: number = 1;
  type: number;

  constructor(
      private dialogRef: MatDialogRef<HierarchicalClusteringComponent>,
      @Inject(MAT_DIALOG_DATA) data
  ) {
    this.clustering_params = data.clustering_params;
    this.analysis_name = data.analysis_name;
    this.dataset_names = data.dataset_names;
    this.type = data.clustering_params.type;
    this.rc_type = data.rc_type;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    if (this.type==this.TYPE_ROW) {
      this.dialogRef.updateSize('550px','620px');
    } else if (this.type==this.TYPE_COL) {
      this.dialogRef.updateSize('550px','700px');
    }
    console.log(this.clustering_params)
  }

  ngOnInit() {
    //this.getClusteringParams();
  }

  /*
  getClusteringParams(): void {
    this.clusteringService.getDatasets(this.analysis_name)
      .subscribe(data => this.processDatasetsFromServer(data), 
                () => console.log("observable complete"));
  }

  processClusteringParamsFromServer(clustering_params: ClusteringParams, type: number) {
    if (clustering_params == null) {
      alert("Failed to get current clustering parameters from server. Falling back on default values.");
      this.clustering_params = new ClusteringParams();
    } else {
      this.clustering_params = clustering_params;
    }
  }

  processDatasetsFromServer(datasets: string[]) {
    if (datasets == null || datasets.length == 0) {
      alert("Failed to get current column clustering parameters from server. This could be because no dataset is selected. Select dataset(s) in selection panel, apply changes and try again.");
    } else {
      this.dataset_names = datasets;
    }
  }
  */

  saveChanges() {
    this.dialogRef.close(this.clustering_params);
    /*
    this.showForm = false;
    this.clusteringService.setClusteringParams(this.analysis_name, this.row_clustering_params, this.col_clustering_params)
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
      alert("Clustering parameter update FAILED. " + response.message + ". " + response.detailed_reason);
    } else {
      alert("Clustering parameters updated.");
      this.dialogRef.close(true);
    }
  }
  */

  toggleRowClustering(){
    //console.log("in toggleRowClustering");
    if(this.toggleView == false){
      this.toggleView = true;  
      this.showDefaultClustParams = true;  
      this.showClustParams = false;     
    } else {
      this.toggleView = false;
      this.showClustParams = false; 
      this.showDefaultClustParams = false;     
    }
  }

  /*
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
  */

  showDefaultRowClusteringParams(){
    //console.log("in showDefaultClusteringParams");
    if(this.showClustParams == true){
      this.showClustParams = false;
    }
    if(this.toggleView == true){
      this.showClustParams == false
      this.showDefaultClustParams = true;
    } else {
      this.showDefaultClustParams = false;
      this.showClustParams == false;
    }
  }

  specifyRowClusteringParams(){
    //console.log("in specifyRowClusteringParams");
    if(this.toggleView == true){
      this.showClustParams = true;
      this.showDefaultClustParams = false;
    } else {
      this.showClustParams = false;
      this.showDefaultClustParams = false;
    }
  }

  /*
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
  */
}
