import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { SelectionPanelData } from '../selection-panel_data';
import { SelectionPanelState } from '../selection-state_data';
import { AssortedService } from '../assorted.service';
import { SearchResultSummary } from '../search_result_summary';
import { ServerResponseData } from '../server_response';
import { AnalysisService } from '../analysis.service'


@Component({
  selector: 'app-selection-panel',
  templateUrl: './selection-panel.component.html',
  styleUrls: ['./selection-panel.component.css']
})
export class SelectionPanelComponent implements OnInit {

  @Input() analysis_name: string;
  @Output() notifyChanges = new EventEmitter();

  data: SelectionPanelData;
  state: SelectionPanelState;

  selected_datasets_applied: string[] = [];
  selected_phenotypes_applied: string[] = [];
  selected_searches_applied: SearchResultSummary[] = [];

  selected_datasets_new: string[] = [];
  selected_phenotypes_new: string[] = [];
  selected_searches_new: SearchResultSummary[] = [];

  selectedDataset: string = '';
  selectedPhenotype: string = '';
  selectedSearch: SearchResultSummary;
  /*
  selected_searches: SearchResultSummary[];
  selected_group_ids: string[];
  selected_group_types: number[];
  */
  showDatasets: boolean = false;
  showPhenotypes: boolean = false;
  showSearch: boolean = false;
  showNeighborhood: boolean = false;
  showClustering: boolean = false;

  hasChanged_Datasets: boolean = false;
  hasChanged_Genes: boolean = false;
  hasChanged_Phenotypes: boolean = false;
  hasChanged_Network: boolean= false;
  hasChanged_Clustering: boolean = false;

  server_response_on_init: ServerResponseData;

  constructor(private assortedService: AssortedService,
              private analysisService: AnalysisService) { }

  ngOnInit() {
    this.getSelectionPanelData();
    this.getSelectionPanelState();
    this.showSearch = true;
  }

  getSelectionPanelData(): void {
		this.assortedService.getSelectionPanelData(this.analysis_name)
      .subscribe(
        data => this.data = data, 
        () => console.log("observable complete"));
  }

  getSelectionPanelState(): void {
		this.assortedService.getSelectionPanelState(this.analysis_name)
      .subscribe(
        data => this.state = data, 
        () => console.log("observable complete"),
        () => this.loadCurrentValues());
  }

  loadCurrentValues(){
    this.selected_datasets_applied = this.state.datasets
    this.selected_phenotypes_applied = this.state.selectedPhenotypes;
    this.selected_searches_applied = this.state.searches;
    this.selected_datasets_new = this.state.datasets
    this.selected_phenotypes_new = this.state.selectedPhenotypes;
    this.selected_searches_new = this.state.searches;
    console.log("selection panel data loaded");
    console.log(this.state);
  }
  
  addDataset() {
    this.hasChanged_Datasets = this.addItem(this.selected_datasets_new, 
      this.selected_datasets_applied, this.selectedDataset);
      console.log(this.hasChanged_Datasets)
  }

  removeDataset(dataset_name) {
    this.hasChanged_Datasets = this.removeItem(this.selected_datasets_new, 
      this.selected_datasets_applied, dataset_name);
  }

  addItem(selected_list_new, selected_list_applied, item_name) {
    let i = selected_list_new.indexOf(item_name);
    if (i > -1 || item_name == "") {
      return;
    }
    selected_list_new.push(item_name);
    console.log(selected_list_new);
    if (this.isEqual(selected_list_new, selected_list_applied)) {
      return false;
    } else {
      return true;
    }
  }

  removeItem(selected_list_new, selected_list_applied, item_name) {
    selected_list_new.splice(selected_list_new.indexOf(item_name), 1 );
    console.log(selected_list_new);
    if (this.isEqual(selected_list_new, selected_list_applied)) {
      return false;
    } else {
      return true;
    }
  }

  addPhenotype() {
    this.hasChanged_Phenotypes = this.addItem(this.selected_phenotypes_new, 
      this.selected_phenotypes_applied, this.selectedPhenotype);
      console.log(this.hasChanged_Phenotypes);
  }

  removePhenotype(phenotype_name) {
    this.hasChanged_Phenotypes = this.removeItem(this.selected_phenotypes_new, 
      this.selected_phenotypes_applied, phenotype_name);
  }

  showPanel(panel: number) {
    this.hideAll();
    if (panel == 0) {
      this.showDatasets = true;
    } else if (panel == 1) {
      this.showPhenotypes = true;
    } else if (panel == 2) {
      this.showSearch = true;
    } else if (panel == 3) {
      this.showNeighborhood = true;
    } else if (panel == 4) {
      this.showClustering = true;
    } 
  }

  hideAll() {
    this.showDatasets = false;
    this.showPhenotypes = false;
    this.showSearch = false;
    this.showNeighborhood = false;
    this.showClustering = false;
  }

  applyChanges() {

    var datasets = this.selected_datasets_new;
    var phenotypes = this.selected_phenotypes_new;
    var group_ids: string[] = []
    var group_types: number[] = []
    for (var i=0; i<this.selected_searches_new.length; i++) {
      group_ids[i] = this.selected_searches_new[i]._id
      group_types[i] = this.selected_searches_new[i].type
    }

    this.analysisService.initializeAnalysis (
      this.analysis_name, 
      datasets,
      phenotypes,
      group_ids,
      group_types
    ).subscribe(
        data => this.server_response_on_init = data, 
        () => console.log("error"),
        () => this.handleAnalysisInitResponse());

    console.log("1");
  }

  handleAnalysisInitResponse() {
    if (this.server_response_on_init.status == 1) {
      this.selected_datasets_applied = this.selected_datasets_new;
      this.selected_phenotypes_applied = this.selected_phenotypes_new;
      this.selected_searches_applied = this.selected_searches_new;
      this.hasChanged_Datasets = false;
      this.hasChanged_Phenotypes = false;
      this.hasChanged_Genes = false;
      this.notifyChanges.emit();
    } else if (this.server_response_on_init.status == 0) {
      alert("Changes could not be applied. " + 
            this.server_response_on_init.message + 
            this.server_response_on_init.detailed_reason)
    }
  }

  isEqual(arr1: string[], arr2: string[]) {
    console.log(arr1);
    console.log(arr2);
    if(arr1.length !== arr2.length)
        return false;
    for(var i = arr1.length; i--;) {
        if(arr1[i] !== arr2[i])
            return false;
    }
    return true;
  }

  /*
  applySearchChanges(selected_searches: SearchResultSummary[]) {
    this.selected_searches = selected_searches;
    this.selected_group_ids = [];
    this.selected_group_types = [];
    for (var i=0; i<selected_searches.length; i++) {
      this.selected_group_ids[i] = selected_searches[i]._id;
      this.selected_group_types[i] = selected_searches[i].type;
    }
    console.log(this.selected_group_ids);
    console.log(this.selected_group_types);
  }
  */

  addSearchResult(selectedSearch: SearchResultSummary) {
    let i = this.selected_searches_new.indexOf(selectedSearch);
    if (i > -1) {
      return;
    }
    //this.selected_searches_new.push(selectedSearch);
    if (this.isSearchEqual(this.selected_searches_new, this.selected_searches_applied)) {
      return false;
    } else {
      return true;
    }
  }

  removeSearchResult(selectedSearch: SearchResultSummary) {
    //this.selected_searches_new.splice(this.selected_searches_new.indexOf(selectedSearch), 1 );
    if (this.isSearchEqual(this.selected_searches_new, this.selected_searches_applied)) {
      return false;
    } else {
      return true;
    }
  }

  isSearchEqual(arr1: SearchResultSummary[], arr2: SearchResultSummary[]) {
    console.log(arr1);
    console.log(arr2);
    if(arr1.length !== arr2.length)
        return false;
    for(var i = arr1.length; i--;) {
        if(arr1[i]._id !== arr2[i]._id || arr1[i].type !== arr2[i].type)
            return false;
    }
    return true;
  }

}
