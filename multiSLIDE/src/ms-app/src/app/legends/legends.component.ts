import { Component, OnInit, Input } from '@angular/core';
import { LegendData } from '../legend_data';
import { AssortedService } from '../assorted.service';

@Component({
  selector: 'app-legends',
  templateUrl: './legends.component.html',
  styleUrls: ['./legends.component.css']
})
export class LegendsComponent implements OnInit {

  @Input() analysis_name: string;

  data: LegendData;

  constructor(private assortedService: AssortedService) { }

  ngOnInit() {
    this.getLegendData();
  }

  getLegendData(): void {
		this.assortedService.getLegendData(this.analysis_name)
			.subscribe(data => this.data = data, () => console.log("observable complete"));
  }
}
