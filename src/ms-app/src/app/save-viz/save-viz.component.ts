import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-save-viz',
  templateUrl: './save-viz.component.html',
  styleUrls: ['./save-viz.component.css']
})
export class SaveVizComponent implements OnInit {

  analysis_name: string;

  constructor(private activatedRoute: ActivatedRoute) { }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe(params => {
      this.analysis_name = params['analysis_name'];
      for (let key in params) {
        console.log(key + ": " + params[key]);
      }
    });
  }

  dummyMethod() {
    
  }
}
