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

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.analysis_name = params['analysis_name'];
      for (let key in params) {
        console.log(key + ": " + params[key]);
      }
    });
    this.containerDisplayOn = false;
  }

  notifyChangesToMapContainer() {
    this.containerDisplayOn = true;
    this.load_count++;
    console.log("2");
  }

}
