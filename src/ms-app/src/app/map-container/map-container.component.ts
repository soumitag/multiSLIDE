import { Component, OnInit, Input, Output, SimpleChange, EventEmitter } from '@angular/core';
import { HeatmapService } from '../heatmap.service'
import { AnalysisService } from '../analysis.service'
import { MapContainerLayout } from '../map-container_layout'
import { ListData } from '../list_data'
import { ServerResponseData } from '../server_response'
import { ListService } from '../list.service';
import { GlobalMapConfig } from '../global-map-config_data';
import { SignificanceTestingComponent } from '../significance-testing/significance-testing.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material/dialog";
import { SignificanceTestingParams } from '../significance_testing_params';
import { GlobalHeatmapData } from '../global_heatmap_data';
import { HeatmapLayout } from '../heatmap_layout';
import { GlobalMapConfigService } from '../global-map-config.service'
import { ClusteringParams } from '../clustering_params';
import { MappedData } from '../mapped_data'
import { PhenotypeSortingParams } from '../phenotype_sorting_params'
import { HierarchicalClusteringComponent } from '../hierarchical-clustering/hierarchical-clustering.component'
import { PhenotypeSortingComponent } from '../phenotype-sorting/phenotype-sorting.component'
import { AssortedService } from '../assorted.service'
import { SelectionPanelState } from '../selection-state_data';
import { SelectionPanelData } from '../selection-panel_data';
import { DatasetDialogComponent } from '../dataset-dialog/dataset-dialog.component'
import { DatasetLinkingComponent } from '../dataset-linking/dataset-linking.component';


@Component({
  selector: 'app-map-container',
  templateUrl: './map-container.component.html',
  styleUrls: ['./map-container.component.css']
})
export class MapContainerComponent implements OnInit {

  @Input() load_count: number;
  @Input() list_change_count: number;
  @Input() analysis_name: string;
  @Input() containerDisplayOn: boolean;
  @Input() showSelectionPanel: boolean;
  @Input() isForPrint: number;

  @Output() notifyListVisualization = new EventEmitter<string>();

  /*
  nSamples: number = 40;
  nEntrez: number = 30;
  mapResolution: string = 'M';
  gridLayout: string = 'A';
  sortBy: string = 'No_Sort';
  rowLabelWidth: number = 100;
  colHeaderHeight: number = 100;
  sampleStartIndex: number = 0;
  entrezStartIndex: number = 0;
  */

  load_layout_count: number = 0;

  selection_panel_state: SelectionPanelState;
  selection_panel_data: SelectionPanelData;
  config: GlobalMapConfig;
  common_heatmap_data: GlobalHeatmapData;
  server_response: ServerResponseData;
  //mapped_data_response: MappedData;
  layout: MapContainerLayout;
  heatmap_layout: HeatmapLayout;

  lists: ListData[];
  feature_list_names: string[] = [];
  sample_list_names: string[] = [];

  //selected_phenotypes: string[];
  //selected_datasets: string[];

  N: number[] = [];
  N_minus_one: number[] = [];
  //column_ordering_schemes: string[] = ['Gene Groups', 'Hierarchical Clusters - Linked', 'Hierarchical Clusters - Unlinked' , 'Significance Level'];
  column_ordering_schemes: string[] = ['Gene Groups', 'Hierarchical Clusters', 'Significance Level'];
  sample_ordering_schemes: string[] = ['Phenotypes', 'Hierarchical Clusters'];

  isRowsPerPageEditable: boolean = false;
  isColsPerPageEditable: boolean = false;
  isRowLabelWidthEditable: boolean = false;
  isColHeaderHeightEditable: boolean = false;

  tempPrevRowsPerPageValue: number;
  tempPrevColsPerPageValue: number;
  tempPrevRowLabelWidthValue: number;
  tempPrevColHeaderHeightValue: number;

  sigtestDialogRef: MatDialogRef<SignificanceTestingComponent>;
  clusteringDialogRef: MatDialogRef<HierarchicalClusteringComponent>;
  phensortDialogRef: MatDialogRef<PhenotypeSortingComponent>;
  datasetSelectionDialogRef: MatDialogRef<DatasetDialogComponent>;
  datasetLinkingDialogRef: MatDialogRef<DatasetLinkingComponent>;

  control_ribbon_left = 240;
  heatmap_panel_left = 0;

  constructor(private dialog: MatDialog,
    private heatmapService: HeatmapService,
    private analysisService: AnalysisService,
    private globalMapConfigService: GlobalMapConfigService,
    private listService: ListService,
    private assortedService: AssortedService) { }

  ngOnInit() {
    if (this.containerDisplayOn) {
      this.getConfig();
      //this.getHeatmapLayout();
      this.getMapContainerLayout();
      this.getUserLists();
      this.getSelectionPanelState();
      this.getSelectionPanelData();
      this.load_count++;
    }
    console.log(this.isForPrint);
  }

  initContainer(server_config: GlobalMapConfig) {
    this.config = server_config;
    this.getGlobalHeatmapData();
    console.log(this.config);
  }

  /*
  offsetSelectionPanel() {
    if (this.showSelectionPanel) {
      this.panel_left = 240;
      console.log(this.panel_left);
    } else {
      this.panel_left = 10;
      console.log(this.panel_left);
    }
  }
  */

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    for (let propName in changes) {
      if (propName == "load_count") {
        this.layout = null;
        this.ngOnInit()
      }
      if (propName == "showSelectionPanel") {
        //this.offsetSelectionPanel();
      }
      if (propName == "list_change_count") {
        this.childListChanged();
      }
    }
  }

  getGlobalHeatmapData(): void {
    this.heatmapService.getGlobalHeatmapData(
      this.analysis_name
    ).subscribe(data => this.common_heatmap_data = data,
      () => console.log("observable complete"));
    //this.load_count++;
  }

  getMapContainerLayout(): void {
    this.heatmapService.getMapContainerLayout(
      this.analysis_name
    ).subscribe(data => this.layout = data,
      () => console.log("observable complete"),
      () => this.initializeN());
    //this.load_count++;
  }

  initializeN() {
    this.N = [];
    this.N_minus_one = [];
    for (var i = 0; i < this.layout.nMaps; i++) {
      this.N[i] = i;
      if (i < this.layout.nMaps-1) {
        this.N_minus_one[i] = i;
      }
    }
    console.log("N: " + this.N);
  }

  getUserLists(): void {
    this.listService.getUserLists(this.analysis_name)
      .subscribe(data => this.parseListData(data), () => console.log("observable complete"));
  }

  getConfig(): void {
    this.globalMapConfigService.getGlobalMapConfig(this.analysis_name)
      .subscribe(data => this.initContainer(data), () => console.log("observable complete"));
  }


  getSelectionPanelState(): void {
    this.assortedService.getSelectionPanelState(this.analysis_name)
      .subscribe(data => this.selection_panel_state = data,
        () => console.log("observable complete"));
  }

  getSelectionPanelData(): void {
    this.assortedService.getSelectionPanelData(this.analysis_name)
      .subscribe(data => this.selection_panel_data = data,
        () => console.log("observable complete"));
  }


  parseListData(data: ListData[]) {
    this.lists = data
    this.feature_list_names = [];
    this.sample_list_names = [];
    for (let list of this.lists) {
      if (list.type == "feature_list") {
        this.feature_list_names.push(list.name);
      } else if (list.type == "sample_list") {
        this.sample_list_names.push(list.name);
      }
    }
    console.log(this.feature_list_names);
  }

  /*
  parseSelectionData(data: SelectionPanelState) {
    if (data) {
      this.selected_datasets = data.selected_datasets;
      this.selected_phenotypes = data.selected_phenotypes;
    } else {
      alert('Could not get selected datasets and phenotypes from server.')
    }
  }
  */

  /*
  Moved to Heatmap component
  getHeatmapLayout(): void {
    this.heatmapService.getHeatmapLayout(this.analysis_name)
      .subscribe(data => this.heatmap_layout = data, () => console.log("observable complete"));
  }
  */
  
  childSortOrderChanged(sortBy: string) {

    let phens: string[] = [sortBy];
    let soords: boolean[] = [true];
    this.setPhenotypeSortingParams(new PhenotypeSortingParams(phens, soords));

  }

  childListChanged() {
    this.getUserLists();
  }

  neighborsChanged() {
    this.analysisService.reInitializeAnalysis(this.analysis_name, 'reinit')
      .subscribe(
        data => this.server_response = data,
        () => console.log("error"),
        () => this.reloadOnNeighborChange());
  }

  visualizeList(feature_list: string) {
    this.notifyListVisualization.emit(feature_list);
    this.getConfig();
    //this.getHeatmapLayout();
    this.getMapContainerLayout();
    this.load_count++;
  }

  reloadOnNeighborChange() {
    if (this.server_response.status == 1) {
      this.getConfig();
      //this.getHeatmapLayout();
      this.getMapContainerLayout();
      this.load_count++;
    } else {
      alert(this.server_response.message + '. ' + this.server_response.detailed_reason);
    }
  }

  toggleRowLabelWidth(n: number) {
    if (n == 1) {
      this.isRowLabelWidthEditable = true;
      this.tempPrevRowLabelWidthValue = this.config.rowLabelWidth;
    } else if (n == 0) {
      this.isRowLabelWidthEditable = false;
      if (this.tempPrevRowLabelWidthValue != this.config.rowLabelWidth) {
        this.setRowLabelWidth(this.tempPrevRowLabelWidthValue);
      }
    }
  }

  toggleColHeaderHeight(n: number) {
    if (n == 1) {
      this.isColHeaderHeightEditable = true;
      this.tempPrevColHeaderHeightValue = this.config.colHeaderHeight;
    } else if (n == 0) {
      this.isColHeaderHeightEditable = false;
      if (this.tempPrevColHeaderHeightValue != this.config.colHeaderHeight) {
        this.setColHeaderHeight(this.tempPrevColHeaderHeightValue);
      }
    }
  }

  openRowOrderingPanel() {
    if (this.config.sampleOrderingScheme == 0) {
      //pheotype sorting
      this.openPhenotypeSortingPanel();
    } else if (this.config.sampleOrderingScheme == 1) {
      // hierarchical clustering
      this.openClusteringDialog(0);
    }
  }

  openColOrderingPanel() {
    if (this.config.columnOrderingScheme == 0) {
      //gene group
      alert("There are no settings for Gene Group based sorting.");
    } else if (this.config.columnOrderingScheme == 1) {
      // hierarchical clustering
      this.openClusteringDialog(1);
    } else if (this.config.columnOrderingScheme == 2) {
      // hierarchical clustering
      this.openClusteringDialog(2);
    } else if (this.config.columnOrderingScheme == 3) {
      // significance
      this.openSignificanceTestingPanel();
    }
  }

  openClusteringDialog(rc: number) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.height = '500';
    dialogConfig.width = '200';
    dialogConfig.position = {
      top: '300',
      left: '300'
    };
    let params: ClusteringParams;
    if (rc == 0) {
      params = this.config.row_clustering_params;
    } else if (rc == 1 || rc == 2) {
      params = this.config.col_clustering_params;
    }

    var prev_data = new ClusteringParams();
    /*
    prev_data.is_joint = params.is_joint;
    prev_data.use_aggregate = params.use_aggregate;
    prev_data.aggregate_func = params.aggregate_func;
    */
    prev_data.type = params.type;
    prev_data.dataset = params.dataset;
    prev_data.use_defaults = params.use_defaults;
    prev_data.linkage_function = params.linkage_function;
    prev_data.distance_function = params.distance_function;
    prev_data.leaf_ordering = params.leaf_ordering;

    dialogConfig.data = {
      clustering_params: params,
      analysis_name: this.analysis_name,
      //dataset_names: this.selected_datasets
      dataset_names: this.selection_panel_state.selected_datasets,
      rc_type: rc
    };
    this.clusteringDialogRef = this.dialog.open(HierarchicalClusteringComponent, dialogConfig);
    this.clusteringDialogRef.afterClosed()
      .subscribe(data => this.handleClusteringParamsUpdate(rc, data, prev_data));
  }

  handleClusteringParamsUpdate(
    rc: number,
    params: ClusteringParams,
    prev_params: ClusteringParams
  ) {
    if (params) {
      if (rc == 0) {
        if (params != prev_params) {
          this.setRowClusteringParams(params)
        }
      } else if (rc == 1) {
        if (params != prev_params) {
          this.setColClusteringParams(params)
        }
      }
    }
  }

  openPhenotypeSortingPanel() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.height = '500';
    dialogConfig.width = '200';
    dialogConfig.position = {
      top: '300',
      left: '300'
    };
    dialogConfig.data = {
      params: this.config.phenotype_sorting_params,
      analysis_name: this.analysis_name,
      //phenotypes: this.selected_phenotypes
      phenotypes: this.selection_panel_state.selected_phenotypes
    };
    this.phensortDialogRef = this.dialog.open(PhenotypeSortingComponent, dialogConfig);
    this.phensortDialogRef.afterClosed()
      .subscribe(data => this.handlephenotypeSortUpdate(data));
  }

  handlephenotypeSortUpdate(params: PhenotypeSortingParams) {
    if (params) {
      if (params != this.config.phenotype_sorting_params) {
        this.setPhenotypeSortingParams(params);
      }
    }
  }

  openDatasetLinkingPanel() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.height = '500';
    dialogConfig.width = '200';
    dialogConfig.position = {
      top: '300',
      left: '300'
    };

    var prev_dataset_linked_vals = new Map();
    for (let [key, value] of this.config.dataset_linkings) {
      prev_dataset_linked_vals.set(key, value);
    }

    /*    
    for (let [key, value] of prev_dataset_linked_vals) {
      console.log(key + ' = ' + value)
    }
    */
    dialogConfig.data = {
      analysis_name: this.analysis_name,
      is_dataset_linked: this.config.dataset_linkings
    };

    this.datasetLinkingDialogRef = this.dialog.open(DatasetLinkingComponent, dialogConfig);
    this.datasetLinkingDialogRef.afterClosed()
        .subscribe(x => this.handleDatasetLinkingUpdate(x, prev_dataset_linked_vals));
  }

  handleDatasetLinkingUpdate(x, prev_dataset_linked_vals) {
    if (x == 0) {
      this.config.dataset_linkings = prev_dataset_linked_vals;
    } else if (x == 1) {
      console.log(this.config.dataset_linkings);
      if(!this.compareMaps(this.config.dataset_linkings, prev_dataset_linked_vals)) {
        this.setDatasetLinkings(prev_dataset_linked_vals);
      }
    }
  }

  setDatasetLinkings(prev_dataset_linked_vals) {
    let dataset_names = Object.keys(this.config.dataset_linkings);
    let is_dataset_linked_arr = [];
    console.log(dataset_names.length)
    /*
    for (var i=0 in is_dataset_linked) {
      console.log(is_dataset_linked[i]);
    }
    */

    for (let [key, value] of Object.entries(this.config.dataset_linkings)) {
      is_dataset_linked_arr.push(value);
    }
    
    this.globalMapConfigService.updateDatasetLinkings(
      this.analysis_name, dataset_names, is_dataset_linked_arr
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          if (this.config.isDatasetLinkingOn) {
            //this.getHeatmapLayout();
            this.getMapContainerLayout();
            this.getGlobalHeatmapData();
            this.load_count++;
          }
        } else {
          this.config.dataset_linkings = prev_dataset_linked_vals
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
      
  }

  compareMaps(map1, map2) {
    var testVal;
    if (map1.size !== map2.size) {
        return false;
    }
    for (var [key, val] of map1) {
        testVal = map2.get(key);
        // in cases of an undefined value, make sure the key
        // actually exists on the object so there are no false positives
        if (testVal !== val || (testVal === undefined && !map2.has(key))) {
            return false;
        }
    }
    return true;
  }

  openDatasetSelectionPanel(dialogNum: number) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.height = '500';
    dialogConfig.width = '200';
    dialogConfig.position = {
      top: '300',
      left: '300'
    };

    if (dialogNum == 0) {
      var prev_selected_datasets: string[] = [];
      for (var i = 0; i < this.selection_panel_state.selected_datasets.length; i++) {
        prev_selected_datasets[i] = this.selection_panel_state.selected_datasets[i];
      }

      dialogConfig.data = {
        analysis_name: this.analysis_name,
        datasets: this.selection_panel_data.dataset_names,
        selected_datasets: this.selection_panel_state.selected_datasets,
        dialogNum: 0
      };

      this.datasetSelectionDialogRef = this.dialog.open(DatasetDialogComponent, dialogConfig);

      this.datasetSelectionDialogRef.afterClosed()
        .subscribe(x => this.handleDatasetSelectionUpdate(x, prev_selected_datasets, this.selection_panel_state.selected_datasets));
      // data is the argument, f(x) {this.handleDatasetSelectionUpdate(x, prev_selected_datasets, this.selection_panel_state.selected_datasets); }
    } else if (dialogNum == 1) {
      var prev_selected_phenotypes: string[] = [];
      for (var i = 0; i < this.selection_panel_state.selected_phenotypes.length; i++) {
        prev_selected_phenotypes[i] = this.selection_panel_state.selected_phenotypes[i];
      }

      dialogConfig.data = {
        analysis_name: this.analysis_name,
        phenotypes: this.selection_panel_data.phenotypes,
        selected_phenotypes: this.selection_panel_state.selected_phenotypes,
        dialogNum: 1
      };

      this.datasetSelectionDialogRef = this.dialog.open(DatasetDialogComponent, dialogConfig);

      this.datasetSelectionDialogRef.afterClosed()
        .subscribe(x => this.handlePhenotypeSelectionUpdate(x, prev_selected_phenotypes, this.selection_panel_state.selected_phenotypes));

    }
  }

  handlePhenotypeSelectionUpdate(x: number, prev_selected_phenotypes: string[], selected_phenotypes: string[]) {
    if (x == 0) {
      this.selection_panel_state.selected_phenotypes = prev_selected_phenotypes;
    } else if (x == 1) {
      if (selected_phenotypes != prev_selected_phenotypes) {
        this.setSelectedPhenotype(prev_selected_phenotypes, selected_phenotypes);
      }
    }
  }

  setSelectedPhenotype(prev_selected_phenotypes: string[], selected_phenotypes: string[]) {
    this.globalMapConfigService.updateSelectedPhenotypes(
      this.analysis_name, selected_phenotypes
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 0) {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          this.selection_panel_state.selected_phenotypes = prev_selected_phenotypes;
        } else if (this.server_response.status == 1) {
            this.getMapContainerLayout();
            this.getGlobalHeatmapData();
            this.getConfig();     // to update phenotype_sorting_params
            //this.getHeatmapLayout();
            this.load_count++;
        }
      });
  }

  handleDatasetSelectionUpdate(x: number, prev_selected_datasets: string[], selected_datasets: string[]) {
    if (x == 0) {
      this.selection_panel_state.selected_datasets = prev_selected_datasets;
    } else if (x == 1) {
      if (selected_datasets != prev_selected_datasets) {
        this.setSelectedDataset(prev_selected_datasets, selected_datasets);
      }
    }
  }

  setSelectedDataset(prev_selected_datasets: string[], selected_datasets: string[]) {
    this.globalMapConfigService.updateSelectedDatasets(
      this.analysis_name, selected_datasets
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 0) {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          this.selection_panel_state.selected_datasets = prev_selected_datasets;
        } else {
          this.config = null;
          this.layout = null;
          this.getConfig();
          //this.getHeatmapLayout();
          this.getMapContainerLayout();
          this.load_count++;
        }
      });
  }

  openSignificanceTestingPanel() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.height = '500';
    dialogConfig.width = '200';
    dialogConfig.position = {
      top: '300',
      left: '300'
    };
    dialogConfig.data = {
      analysis_name: this.analysis_name,
      dataset_names: this.selection_panel_data.dataset_names,
      phenotypes: this.selection_panel_data.phenotypes,
      significance_testing_params: this.config.significance_testing_params
    };

    var prev_data = new SignificanceTestingParams();
    prev_data.dataset = this.config.significance_testing_params.dataset;
    prev_data.phenotype = this.config.significance_testing_params.phenotype;
    prev_data.testtype = this.config.significance_testing_params.testtype;
    prev_data.significance_level = this.config.significance_testing_params.significance_level;
    prev_data.apply_fdr = this.config.significance_testing_params.apply_fdr;
    prev_data.fdr_threshold = this.config.significance_testing_params.fdr_threshold;

    this.sigtestDialogRef = this.dialog.open(SignificanceTestingComponent, dialogConfig);
    this.sigtestDialogRef.afterClosed()
      .subscribe(data => this.handleSignificanceTestingParamsUpdate(prev_data, data));
  }

  handleSignificanceTestingParamsUpdate(prev_params: SignificanceTestingParams, params: SignificanceTestingParams) {
    if (params) {
      console.log(prev_params)
      console.log(params)
      if (params != prev_params) {
        this.setGeneFilteringParams(params);
      }
    }
  }

  
  setMapResolution(sz: string) {
    //this.setMapContainerConfig();
    //this.globalMapConfigService.setMapResolution(this.analysis_name, this.config)
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_map_resolution', sz
    )
      .subscribe(
        data => this.server_response = data,
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.mapResolution = sz;
            //this.getHeatmapLayout();
            this.getMapContainerLayout();
            this.load_layout_count++;
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setMapOrientation(orientation: number) {
    //this.setMapContainerConfig();
    //this.globalMapConfigService.setMapResolution(this.analysis_name, this.config)
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_map_orientation', orientation.toString()
    )
      .subscribe(
        data => this.server_response = data,
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.mapOrientation = orientation;
            //this.getHeatmapLayout();
            this.getMapContainerLayout();
            this.load_layout_count++;
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setGridLayout(n_maps_per_row: number) {
    //this.config.gridLayout = n_maps_per_row;
    //this.setMapContainerConfig();
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_grid_layout', n_maps_per_row.toString()
    )
      .subscribe(
        data => this.server_response = data,
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.gridLayout = n_maps_per_row;
            this.getMapContainerLayout();
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setColHeaderHeight(fallback_value: number) {
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_col_header_height', this.config.colHeaderHeight.toString()
    )
      .subscribe(
        data => this.server_response = data,
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            //this.getHeatmapLayout();
            this.getMapContainerLayout();
            this.load_layout_count++;
          } else {
            this.config.colHeaderHeight = fallback_value;
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setRowLabelWidth(fallback_value: number) {
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_row_label_width', this.config.rowLabelWidth.toString()
    )
      .subscribe(
        data => this.server_response = data,
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            //this.getHeatmapLayout();
            this.getMapContainerLayout();
            this.load_layout_count++;
          } else {
            this.config.rowLabelWidth = fallback_value;
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setRowClusteringParams(params: ClusteringParams) {
    this.globalMapConfigService.setClusteringParams(
      this.analysis_name, params
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          this.config.row_clustering_params = params;
          if (this.config.sampleOrderingScheme == 1) {
            //this.config.current_sample_start = 0;
            this.getGlobalHeatmapData();
            this.load_count++;
          }
        } else {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  setColClusteringParams(params: ClusteringParams) {
    this.globalMapConfigService.setClusteringParams(
      this.analysis_name, params
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          this.config.col_clustering_params = params;
          if (this.config.columnOrderingScheme == 1) {
            //this.config.current_feature_start = 0;
            this.getGlobalHeatmapData();
            this.load_count++;
          }
        } else {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  setColumnOrderingScheme(scheme: string) {
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_col_ordering', scheme
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          this.config.columnOrderingScheme = parseInt(scheme);
          //this.config.current_feature_start = 0;
          this.getGlobalHeatmapData();
          this.load_count++;
        } else {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  setSampleOrderingScheme(scheme: string) {
    this.globalMapConfigService.setGlobalMapConfigParam(
      this.analysis_name, 'set_row_ordering', scheme
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          this.config.sampleOrderingScheme = parseInt(scheme);
          //this.config.current_sample_start = 0;
          this.getGlobalHeatmapData();
          this.load_count++;
        } else {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  toggleDatasetLinking() {
    this.globalMapConfigService.updateGlobalMapConfigParam(
      this.analysis_name, 'set_is_dataset_linking_on', this.config.isDatasetLinkingOn.toString()
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          //this.getHeatmapLayout();
          this.getMapContainerLayout();
          this.getGlobalHeatmapData();
          this.load_count++;
        } else {
          // roll (toggle) back
          this.config.isDatasetLinkingOn = !this.config.isDatasetLinkingOn;
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  toggleGeneFiltering() {
    this.globalMapConfigService.updateGlobalMapConfigParam(
      this.analysis_name, 'set_gene_filtering', this.config.isGeneFilteringOn.toString()
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          //this.getHeatmapLayout();
          this.getMapContainerLayout();
          this.getGlobalHeatmapData();
          this.load_count++;
        } else {
          // roll (toggle) back
          this.config.isGeneFilteringOn = !this.config.isGeneFilteringOn;
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  setGeneFilteringParams(params: SignificanceTestingParams) {
    console.log('in setGeneFilteringParams()');
    this.globalMapConfigService.updateGeneFilteringParams(
      this.analysis_name, params
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          this.config.significance_testing_params = params;
          if (this.config.isGeneFilteringOn || this.config.columnOrderingScheme == 2) {
            //this.getHeatmapLayout();
            this.getMapContainerLayout();
            this.getGlobalHeatmapData();
            this.load_count++;
          }
        } else {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  setPhenotypeSortingParams(params: PhenotypeSortingParams) {
    this.globalMapConfigService.setPhenotypeSortingParams(
      this.analysis_name, params
    ).subscribe(
      data => this.server_response = data,
      () => console.log("error"),
      () => {
        if (this.server_response.status == 1) {
          this.config.phenotype_sorting_params = params;
          if (this.config.sampleOrderingScheme == 0) {
            //this.config.current_sample_start = 0;
            this.getGlobalHeatmapData();
            this.load_count++;
          }
        } else {
          alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
        }
      });
  }

  /*
  updateParams(config: GlobalMapConfig, mapped_data: MappedData) {
    for (var i = 0; i < mapped_data.names.length; i++) {
      this.updateField(config, mapped_data.names[i], mapped_data.values[i]);
    }
  }

  updateField(config: GlobalMapConfig, name: string, value: string) {
    if (name == 'mapResolution') config.mapResolution = value;
    else if (name == 'gridLayout') config.gridLayout = Number(value);
    else if (name == 'rowsPerPageDisplayed') config.rowsPerPageDisplayed = Number(value);
    else if (name == 'colsPerPageDisplayed') config.colsPerPageDisplayed = Number(value);
    else if (name == 'userSpecifiedRowsPerPage') config.userSpecifiedRowsPerPage = Number(value);
    else if (name == 'userSpecifiedColsPerPage') config.userSpecifiedColsPerPage = Number(value);
    else if (name == 'colHeaderHeight') config.colHeaderHeight = Number(value);
    else if (name == 'rowLabelWidth') config.rowLabelWidth = Number(value);
    else if (name == 'current_sample_start') config.current_sample_start = Number(value);
    else if (name == 'current_feature_start') config.current_feature_start = Number(value);
    else if (name == 'available_rows') config.available_rows = Number(value);
    else if (name == 'available_cols') config.available_cols = Number(value);
    else if (name == 'scrollBy') config.scrollBy = Number(value);
    else if (name == 'columnOrderingScheme') config.columnOrderingScheme = Number(value);
    else if (name == 'sampleOrderingScheme') config.sampleOrderingScheme = Number(value);
    else if (name == 'isGeneFilteringOn') config.isGeneFilteringOn = Boolean(value);
    else if (name == 'isSampleFilteringOn') config.isSampleFilteringOn = Boolean(value);
  }
  */
}
