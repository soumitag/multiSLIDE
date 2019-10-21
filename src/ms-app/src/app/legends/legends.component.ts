import { Component, OnInit, Input, SimpleChange } from '@angular/core';
import { LegendData } from '../legend_data';
import { AssortedService } from '../assorted.service';

@Component({
  selector: 'app-legends',
  templateUrl: './legends.component.html',
  styleUrls: ['./legends.component.css']
})
export class LegendsComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() load_count: number;

  data: LegendData;

  constructor(private assortedService: AssortedService) { }

  ngOnInit() {
    this.getLegendData();
  }

  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		for (let propName in changes) {
			if (propName == "load_count") {
				this.data = null;			// nullify it so that "Loading..." message is displayed
				this.getLegendData();
			}
		}
	}

  getLegendData(): void {
		this.assortedService.getLegendData(this.analysis_name)
			.subscribe(
        data => this.data = data, 
        () => console.log("observable complete"),
        () => console.log(this.data),);
  }
}
