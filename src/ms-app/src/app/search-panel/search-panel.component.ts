import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { SearchResults } from '../search_results';
import { SearchService } from '../search.service';
import { SearchResultSummary } from '../search_result_summary';
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material/dialog";
import { EnrichmentAnalysisParams } from '../enrichment_analysis_params';
import { EnrichmentAnalysisComponent } from '../enrichment-analysis/enrichment-analysis.component'

@Component({
  selector: 'app-search-panel',
  templateUrl: './search-panel.component.html',
  styleUrls: ['./search-panel.component.css']
})
export class SearchPanelComponent implements OnInit {

  @Input() selected_searches: SearchResultSummary[];
  @Input() analysis_name: string;
  @Input() dataset_names: string[];
  @Output() notifySearchResultAdded = new EventEmitter<SearchResultSummary>();
  @Output() notifySearchResultRemoved = new EventEmitter<SearchResultSummary>();

  data: SearchResults[];
  
  query: string;
  searchText: string;
  queryType: string;
  searchType: string;

  //selected_searches: SearchResultSummary[] = [];
  results_selection_ind: boolean[][] = [];

  search_panel_open: boolean = false;
  search_panel_handler_text: string = "Add Genes";

  enrichmentDialogRef: MatDialogRef<EnrichmentAnalysisComponent>;

  constructor(private dialog: MatDialog,
              private searchService: SearchService
            ) { }

  ngOnInit() { }

  doAdvancedSearch(): void {
		this.searchService.doAdvancedSearch(this.analysis_name, this.searchText, this.queryType, this.searchType)
			.subscribe(data => this.data = data, () => console.log("observable complete"));
  }
  
  doSearch(query: string): void {
    this.query = query;
		this.searchService.doSearch(this.analysis_name, this.query)
			.subscribe(data => this.handleSearchResponse(data), () => console.log("observable complete"));
	}

  handleSearchResponse(data: SearchResults[]) {
    this.data = data;
    for ( var i = 0; i < data.length; i++ ) {
      this.results_selection_ind[i] = [];
      for (var j = 0; j < data[i].search_result_summaries.length; j++) {
        this.results_selection_ind[i][j] = false;
      }
    }
  }

  toggleResult(group_index: number, result_index: number) {
    if (this.results_selection_ind[group_index][result_index]) {
      this.removeResult(group_index,result_index);
    } else {
      this.addResult(group_index,result_index);
    }
  }

  addResult(group_index: number, result_index: number) {
    let search = this.data[group_index].search_result_summaries[result_index];
    let index = this.selected_searches.indexOf(search);
    if (index == -1) {
      this.selected_searches.push(search);
    }
    this.results_selection_ind[group_index][result_index] = true;
    this.notifySearchResultAdded.emit(search);
  }

  removeResult(group_index: number, result_index: number) {
    let search = this.data[group_index].search_result_summaries[result_index];
    let index = this.selected_searches.indexOf(search);
    this.selected_searches.splice(index, 1);
    this.results_selection_ind[group_index][result_index] = false;
    this.notifySearchResultRemoved.emit(search);
  }

  removeSearchResult(search: SearchResultSummary) {
    let index = this.selected_searches.indexOf(search);
    this.selected_searches.splice(index, 1);
    for (var i=0; i<this.results_selection_ind.length; i++) {
      for (var j=0; j<this.results_selection_ind[i].length; j++) {
        if (this.data[i].search_result_summaries[j] === search) {
          this.results_selection_ind[i][j] = false;
        }
      }
    }
    this.notifySearchResultRemoved.emit(search);
  }

  toggleSearchPanel() {
    if (this.search_panel_open) {
      this.search_panel_open = false;
      this.search_panel_handler_text = "Add Genes";
    } else {
      this.search_panel_open = true;
      this.search_panel_handler_text = "Add Genes";
    }
  }

  openEnrichmentDialog() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {
      analysis_name: this.analysis_name,
      enrichment_analysis_params: new EnrichmentAnalysisParams()
    };
    
    var prev_data = new EnrichmentAnalysisParams();
    
    this.enrichmentDialogRef = this.dialog.open(EnrichmentAnalysisComponent, dialogConfig);
    this.enrichmentDialogRef.afterClosed()
    		.subscribe( data=>this.handleEnrichmentParamsUpdate(prev_data, data) );
  }

  handleEnrichmentParamsUpdate(prev_data, data) {
    console.log(prev_data);
    console.log(data)
  }

}
