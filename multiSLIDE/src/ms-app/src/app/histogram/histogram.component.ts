import { Component, OnInit, AfterViewInit, Input } from '@angular/core';
import { HistogramData } from '../histogram_data';
import { HistogramService } from '../histogram.service'

@Component({
  selector: 'app-histogram',
  templateUrl: './histogram.component.html',
  styleUrls: ['./histogram.component.css']
})
export class HistogramComponent implements OnInit, AfterViewInit {

  @Input() analysis_name: number;
  @Input() data_type: String;
  @Input() img_width: number;
  @Input() img_height: number;

  hist_data: HistogramData;

  constructor(private histogramService: HistogramService) { }

  ngOnInit() {
    console.log(this.analysis_name);
    this.getHistogramData();
    console.log("Started.");
    //console.log(this.hist_data);
  }

  ngAfterViewInit() {
    
  }

  getHistogramData(): void {
		this.histogramService.getHistogramData(this.analysis_name, this.data_type, this.img_width, this.img_height)
			.subscribe(data => this.hist_data = data, () => console.log("observable complete"));
	}

}
