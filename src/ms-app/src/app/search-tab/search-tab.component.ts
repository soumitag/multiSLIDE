import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { SearchResults } from '../search_results';
import { SearchService } from '../search.service';
import { SearchResultSummary } from '../search_result_summary';
import { ServerResponseData } from '../server_response';
import { AnalysisService } from '../analysis.service';

@Component({
  selector: 'app-search-tab',
  templateUrl: './search-tab.component.html',
  styleUrls: ['./search-tab.component.css']
})
export class SearchTabComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() current_search_results: SearchResults[];
  @Input() selected_searches: SearchResultSummary[];
  @Output() notify_changes_applied = new EventEmitter();

  query: string;
  buttonDisabled: boolean = false;
  closeButtonDisabled: boolean = false;

  constructor(
    private searchService: SearchService,
    private analysisService: AnalysisService
  ) { }

  ngOnInit() {}

  doSearch(query: string): void {
    this.query = query;
		this.searchService.doSearch(this.analysis_name, this.query)
      .subscribe(search_data => this.current_search_results = search_data, 
                () => console.log("observable complete"));
  }
  
  toggleResult(group_index: number, result_index: number) {
    let search_id = this.current_search_results[group_index].search_result_summaries[result_index].search_id;
    if (this.isSearchSelected(search_id)) {
      this.removeResult(group_index,result_index);
    } else {
      this.addResult(group_index,result_index);
    }
  }

  addResult(group_index: number, result_index: number) {
    let search = this.current_search_results[group_index].search_result_summaries[result_index];
    let index = this.selected_searches.indexOf(search);
    if (index == -1) {
      this.selected_searches.push(search);
    }
  }

  removeResult(group_index: number, result_index: number) {
    let search = this.current_search_results[group_index].search_result_summaries[result_index];
    let index = this.selected_searches.indexOf(search);
    this.selected_searches.splice(index, 1);
  }

  /*
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
  */

  removeSearchResult(search: SearchResultSummary) {
    let index = this.selected_searches.indexOf(search);
    this.selected_searches.splice(index, 1);
  }

  isSearchSelected(search_id: String) {
    for ( var i = 0; i < this.selected_searches.length; i++ ) {
      if (this.selected_searches[i].search_id == search_id) {
        return true;
      }
    }
    return false;
  }

  applyChanges() {

    if(this.selected_searches.length == 0){
      alert("'Add Genes' to visualize");
      return;
    }

    this.buttonDisabled = true;

    let selected_search_ids: string[] = [];
    for ( var i = 0; i < this.selected_searches.length; i++ ) {
      selected_search_ids.push(this.selected_searches[i].search_id);
    }

    this.analysisService.initializeAnalysis (
      this.analysis_name, 
      "init_search",
      selected_search_ids
    ).subscribe(
        data => this.handleAnalysisInitResponse(data), 
        () => console.log("error"));

    console.log("1");
  }

  handleAnalysisInitResponse(response: ServerResponseData) {
    if (response.status == 1) {
      //this.dialogRef.close();
      // ask parent to close dialog
      this.notify_changes_applied.emit();
    } else if (response.status == 0) {
      alert("Changes could not be applied. " + 
            response.message + 
            response.detailed_reason);
      this.buttonDisabled = false;
    }
  }

}
