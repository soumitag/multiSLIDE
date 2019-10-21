import { Component, OnInit, Input } from '@angular/core';
import { HeatmapService } from '../heatmap.service';
import { HeatmapComponent } from '../heatmap/heatmap.component';

@Component({
  selector: 'app-load-next',
  templateUrl: './load-next.component.html',
  styleUrls: ['./load-next.component.css']
})
export class LoadNextComponent implements OnInit {

  //@Input() heatmapComponent: HeatmapComponent;

  message = '';

  constructor(private heatmapService: HeatmapService) { }

  ngOnInit() { }

  load_data_next(): void {
    this.message = 'Clicked!'
    //this.heatmapComponent.getHeatmapData();
    console.log("new data");
  }

}
