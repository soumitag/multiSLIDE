import { Component, OnInit } from '@angular/core';
import { NeighborhoodSearchResults } from '../neighborhood_search_results';
import { SearchService } from '../search.service';

@Component({
  selector: 'app-network-neighborhood',
  templateUrl: './network-neighborhood.component.html',
  styleUrls: ['./network-neighborhood.component.css']
})
export class NetworkNeighborhoodComponent implements OnInit {

  data: NeighborhoodSearchResults;
  
  searchText: string;
  queryType: string;
  searchType: string;

  selected_results: string[] = [];

  constructor(private searchService: SearchService) { }

  ngOnInit() {
  }

  doSearch(): void {
		this.searchService.doNeighborhoodSearch("demo", this.searchText, this.queryType, this.searchType)
			.subscribe(data => this.data = data, () => console.log("observable complete"));
	}

  addResult(index: number) {
    let gname = this.data[index].group_name;
    if (this.selected_results.indexOf(gname) == -1) {
      this.selected_results.push(gname);
    }
  }

}
