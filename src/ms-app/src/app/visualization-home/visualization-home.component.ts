import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ServerResponseData } from '../server_response';

@Component({
  selector: 'app-visualization-home',
  templateUrl: './visualization-home.component.html',
  styleUrls: ['./visualization-home.component.css']
})
export class VisualizationHomeComponent implements OnInit {

  analysis_name: string;
  userSelectionHasChanged: boolean = false;
  containerDisplayOn: boolean = false;
  server_response: ServerResponseData;
  load_count: number = 0;
  list_change_count: number = 0;
  showSelectionPanel: boolean = true;
  featureListNameInSelectionPanel: string;

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit() {
    let is_load_analysis = false;
    this.activatedRoute.queryParams.subscribe(params => {
      this.analysis_name = params['analysis_name'];
      for (let key in params) {
        if (key == 'source') {
          if (params['source'] == 'load_analysis' || params['source'] == 'load_demo') {
            is_load_analysis = true;
          }
        }
        console.log(key + ": " + params[key]);
      }
    });
    if (is_load_analysis) {
      this.containerDisplayOn = true;
    } else {
      this.containerDisplayOn = false;
    }
  }

  notifyChangesToMapContainer() {
    this.containerDisplayOn = true;
    this.load_count++;
  }

  onListChange() {
    this.list_change_count++;
  }

  onConnectionChange() {
    this.load_count++;
  }

  notifyToggleToSelectionPanel(){
    
    this.showSelectionPanel = !this.showSelectionPanel;
    /*
    if(this.toggleSelectionPanel == false){
      this.toggleSelectionPanel = true;
    } else if (this.toggleSelectionPanel == true){
      this.toggleSelectionPanel = false;
    }*/
  }

  notifyListVisualization (feature_list: string) {
    alert(feature_list);
    this.featureListNameInSelectionPanel = feature_list;
  }

  featureListNameReset() {
    this.featureListNameInSelectionPanel = null;
    alert(this.featureListNameInSelectionPanel);
  }

  /*
  onClusteringParamChange() {
    this.load_count++;
  }
  */

}
