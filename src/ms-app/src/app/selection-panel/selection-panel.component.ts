import { Component, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { SelectionPanelData } from '../selection-panel_data';
import { SelectionPanelState } from '../selection-state_data';
import { AssortedService } from '../assorted.service';
import { SearchResultSummary } from '../search_result_summary';
import { ServerResponseData } from '../server_response';
import { AnalysisService } from '../analysis.service'
import { DialogBoxComponent } from '../dialog-box/dialog-box.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";

@Component({
  selector: 'app-selection-panel',
  templateUrl: './selection-panel.component.html',
  styleUrls: ['./selection-panel.component.css']
})
export class SelectionPanelComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() featureListName: string;
  @Output() notifyChanges = new EventEmitter();
  @Output() featureListNameReset = new EventEmitter();

  buttonDisabled: boolean = false;
  closeButtonDisabled: boolean = false;

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
  dialogRef: MatDialogRef<DialogBoxComponent>;

  showingFeatureList: boolean = false;

  constructor(private assortedService: AssortedService,
              private analysisService: AnalysisService,
              private dialog: MatDialog) { }

  ngOnInit() {
    if (this.analysis_name.startsWith("demo_2021158607524066_")) {
      this.getInitData();
      this.showSearch = true;
      this.applyChanges();
    } else {
      this.getSelectionPanelData();
      this.getSelectionPanelState();
      this.showSearch = true;
    }
  }

  getInitData() {
    this.assortedService.getSelectionPanelDataAndState(this.analysis_name)
      .subscribe(
        responseList => {
          this.data = responseList[0];
          this.state = responseList[1];
        }, 
        () => console.log("observable complete"));
  }

  /*
  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
    for (let propName in changes) {
			if (propName == "featureListName") {
        alert(this.featureListName);
        if (this.featureListName) {
          this.showingFeatureList = true;
          console.log(this.showingFeatureList);
        }
      }
    }
  }
  */

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

    if(this.selected_searches_new.length == 0){
      alert("'Add Genes' to visualize");
      return;
    }

    if(this.selected_datasets_new.length == 0){
      alert("Select atleast one dataset to visualize");
    }
    
    this.buttonDisabled = true;
    var datasets = this.selected_datasets_new;
    var phenotypes = this.selected_phenotypes_new;
    var group_ids: string[] = []
    var group_types: number[] = []
    var group_display_tags: string[] = []
    var intersection_counts: number[] = []
    var intersection_counts_per_dataset: string = ''
    var background_counts: number[] = []
    for (var i=0; i<this.selected_searches_new.length; i++) {
      group_ids[i] = this.selected_searches_new[i]._id
      group_types[i] = this.selected_searches_new[i].type
      group_display_tags[i] = this.selected_searches_new[i].display_tag
      intersection_counts[i] = this.selected_searches_new[i].intersection_count
      intersection_counts_per_dataset += this.selected_searches_new[i].intersection_counts_per_dataset.toString() + ";"
      background_counts[i] = this.selected_searches_new[i].background_count
    }

    /*
    const dialogConfig = new MatDialogConfig();
		dialogConfig.data = {
			message_txt: 'Applying Changes',
			state: 0
    };
    */
    
		this.dialogRef = this.dialog.open(DialogBoxComponent, {
      height: '200',
      width: '650',
      data: {
			  message_txt: 'Please wait while the changes are applied to the visualizations...',
			  state: 0
      }
    });

    this.analysisService.initializeAnalysis (
      this.analysis_name, 
      datasets,
      phenotypes,
      group_ids,
      group_types,
      group_display_tags,
      intersection_counts,
      intersection_counts_per_dataset,
      background_counts
    ).subscribe(
        data => this.server_response_on_init = data, 
        () => console.log("error"),
        () => this.handleAnalysisInitResponse());

    console.log("1");
  }

  closeList() {
    this.closeButtonDisabled = true;
    this.analysisService.reInitializeAnalysis (this.analysis_name, 'history')
    .subscribe(
        data => this.server_response_on_init = data, 
        () => console.log("error"),
        () => {
          this.handleAnalysisInitResponse_FeatureListClose(); 
          this.featureListName = null;
          this.featureListNameReset.emit();
        });
  }

  handleAnalysisInitResponse_FeatureListClose() {
    if (this.server_response_on_init.status == 1) {
      this.selected_datasets_applied = this.selected_datasets_new;
      this.selected_phenotypes_applied = this.selected_phenotypes_new;
      this.selected_searches_applied = this.selected_searches_new;
      this.hasChanged_Datasets = false;
      this.hasChanged_Phenotypes = false;
      this.hasChanged_Genes = false;
      this.notifyChanges.emit();
      this.closeButtonDisabled = false; 
    } else if (this.server_response_on_init.status == 0) {
      alert("Changes could not be applied. " + 
            this.server_response_on_init.message + 
            this.server_response_on_init.detailed_reason);
      this.closeButtonDisabled = false;      
    }
  }

  handleAnalysisInitResponse() {
    if (this.server_response_on_init.status == 1) {
      this.dialogRef.close();
      this.selected_datasets_applied = this.selected_datasets_new;
      this.selected_phenotypes_applied = this.selected_phenotypes_new;
      this.selected_searches_applied = this.selected_searches_new;
      this.hasChanged_Datasets = false;
      this.hasChanged_Phenotypes = false;
      this.hasChanged_Genes = false;
      this.notifyChanges.emit();
      this.buttonDisabled = false; 
    } else if (this.server_response_on_init.status == 0) {
      /*
      alert("Changes could not be applied. " + 
            this.server_response_on_init.message + 
            this.server_response_on_init.detailed_reason);
      */
      this.buttonDisabled = false;
      this.dialogRef.componentInstance.data = {
        message_txt: 'Changes could not be applied due to the following error: ' + this.server_response_on_init.message + this.server_response_on_init.detailed_reason,
			  state: 1
      };
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
