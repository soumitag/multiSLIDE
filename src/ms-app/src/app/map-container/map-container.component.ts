import { Component, OnInit, Input, Output, SimpleChange, EventEmitter } from '@angular/core';
import { HeatmapService } from '../heatmap.service'
import { AnalysisService } from '../analysis.service'
import { MapContainerLayout } from '../map-container_layout'
import { ListData } from '../list_data'
import { ServerResponseData } from '../server_response'
import { ListService } from '../list.service';
import { GlobalMapConfig } from '../global-map-config_data';
import { SignificanceTestingComponent } from '../significance-testing/significance-testing.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";
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

  config: GlobalMapConfig;
  common_heatmap_data: GlobalHeatmapData;
  server_response: ServerResponseData;
  mapped_data_response: MappedData;
  layout: MapContainerLayout;
  heatmap_layout: HeatmapLayout;

  lists: ListData[];
  feature_list_names: string[] = [];
  sample_list_names: string[] = [];

  selected_phenotypes: string[];
  selected_datasets: string[];

  N: number[] = [];
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
  panel_left: number;

  sigtestDialogRef: MatDialogRef<SignificanceTestingComponent>;
  clusteringDialogRef: MatDialogRef<HierarchicalClusteringComponent>;
  phensortDialogRef: MatDialogRef<PhenotypeSortingComponent>;

  constructor(private dialog: MatDialog, 
              private heatmapService: HeatmapService, 
              private analysisService: AnalysisService,
              private globalMapConfigService: GlobalMapConfigService,
              private listService: ListService,
              private assortedService: AssortedService) { }

  ngOnInit() {
    if (this.containerDisplayOn) {
      this.getConfig();
      this.getHeatmapLayout();
      this.getMapContainerLayout();
      this.getUserLists();
      this.getSelectionData();
      this.load_count++;
    }
    console.log(this.isForPrint);
  }

  initContainer(server_config: GlobalMapConfig) {
    this.config = server_config;
    this.getGlobalHeatmapData();
  }

  offsetSelectionPanel(){
    if(this.showSelectionPanel){
      this.panel_left = 300;
      console.log(this.panel_left);
    } else {
      this.panel_left = 10;
      console.log(this.panel_left);
    }
  }

  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		for (let propName in changes) {
			if (propName == "load_count") {
        this.layout = null;
				this.ngOnInit()
      }
      if(propName == "showSelectionPanel"){
        this.offsetSelectionPanel();
      }
      if(propName == "list_change_count"){
        this.childListChanged();
      }
		}
  }
  
  getGlobalHeatmapData(): void {
    this.heatmapService.getGlobalHeatmapData (
      this.analysis_name,
      this.config.current_sample_start,
      this.config.current_feature_start
    ).subscribe(data => this.common_heatmap_data = data, 
                () => console.log("observable complete"));
    //this.load_count++;
  }

  getMapContainerLayout(): void {
    this.heatmapService.getMapContainerLayout (
      this.analysis_name
    ).subscribe(data => this.layout = data, 
                () => console.log("observable complete"),
                () => this.initializeN());
    //this.load_count++;
  }

  initializeN() {
    for (var i=0; i<this.layout.nMaps; i++) {
      this.N[i] = i;
    }
    console.log("N: " + this.N);
  }

  getUserLists(): void {
    this.listService.getUserLists (this.analysis_name)
      .subscribe(data => this.parseListData(data), () => console.log("observable complete"));
  }

  getConfig(): void {
    this.globalMapConfigService.getGlobalMapConfig (this.analysis_name)
      .subscribe(data => this.initContainer(data), () => console.log("observable complete"));
  }

  getSelectionData(): void {
    this.assortedService.getSelectionPanelState (this.analysis_name)
      .subscribe(data => this.parseSelectionData(data), () => console.log("observable complete"));
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

  parseSelectionData(data: SelectionPanelState) {
    if (data) {
      this.selected_datasets = data.datasets
      this.selected_phenotypes = data.selectedPhenotypes
    } else {
      alert('Could not get selected datasets and phenotypes from server.')
    }
  }
  
  getHeatmapLayout(): void {
		this.heatmapService.getHeatmapLayout(this.analysis_name)
			.subscribe(data => this.heatmap_layout = data, () => console.log("observable complete"));
	}

  load_view(axis:number, direction:number) {
    if (this.config.mapOrientation == 0) {
      if (axis==0) {
        this.load_row_view(direction);
      } else if (axis==1) {
        this.load_column_view(direction);
      }
    } else if (this.config.mapOrientation == 1) {
      if (axis==0) {
        this.load_column_view(direction);
      } else if (axis==1) {
        this.load_row_view(direction);
      }
    }
  }

  load_row_view(direction: number): void {
    console.log("Start: " + this.config.current_sample_start);
    console.log("Scroll By: " + this.config.scrollBy);
    console.log("Available Rows: " + this.config.available_rows);

    if (!this.config.scrollBy || this.config.scrollBy < 1) {
      alert("'Scroll By' must be a value between 1 and 999.");
      return;
    }

    if (direction == 1) {
      this.setSampleStart(this.config.current_sample_start + this.config.scrollBy, this.config.scrollBy);
    } else if (direction == -1) {
      this.setSampleStart(this.config.current_sample_start - this.config.scrollBy, this.config.scrollBy);
    }

    /*
    if (direction == 1) {
      if ((this.config.current_sample_start + this.config.rowsPerPageDisplayed + this.config.scrollBy) <= this.config.available_rows) {
        console.log("Scrolling");
        this.setSampleStart(this.config.current_sample_start + this.config.scrollBy, this.config.scrollBy);
        //this.config.current_sample_start = this.config.current_sample_start + this.config.scrollBy;
        //this.getGlobalHeatmapData();
      }
    } else if (direction == -1) {
      if (this.config.current_sample_start > 0) {
        this.setSampleStart(this.config.current_sample_start + this.config.scrollBy, this.config.scrollBy);
        //this.config.current_sample_start = this.config.current_sample_start - this.config.scrollBy;
        //this.getGlobalHeatmapData();
      }
    }
    */

  }

  load_column_view(direction: number): void {

    console.log("Direction: " + direction);

    if (!this.config.scrollBy || this.config.scrollBy < 1) {
      alert("'Scroll By' must be a value between 1 and 999.");
      return;
    }

    if (direction == 1) {
      this.setFeatureStart(this.config.current_feature_start + this.config.scrollBy, this.config.scrollBy);
    } else if (direction == -1) {
      this.setFeatureStart(this.config.current_feature_start - this.config.scrollBy, this.config.scrollBy);
    }

    /*
    if (direction == 1) {
      if ((this.config.current_feature_start + this.config.colsPerPageDisplayed + this.config.scrollBy) <= this.config.available_cols) {
        console.log("Start: " + this.config.current_feature_start);
        console.log("Scroll By: " + this.config.scrollBy);
        console.log("Available Cols: " + this.config.available_cols);
        this.setFeatureStart(this.config.current_feature_start + this.config.scrollBy, this.config.scrollBy);
        //this.config.current_feature_start = this.config.current_feature_start + this.config.scrollBy;
        //this.getGlobalHeatmapData();
      }
    } else if (direction == -1) {
      if (this.config.current_feature_start > 0) {
        this.setFeatureStart(this.config.current_feature_start - this.config.scrollBy, this.config.scrollBy);
        //this.config.current_feature_start = this.config.current_feature_start - this.config.scrollBy;
        //this.getGlobalHeatmapData();
      }
    }
    */
    
  }
  
  childSortOrderChanged(sortBy: string) {
    
    let phens: string[] = [sortBy];
    let soords: boolean[] = [true];
    this.setPhenotypeSortingParams(new PhenotypeSortingParams(phens,soords));

    //this.setSampleSortBy(sortBy);
    /*
    console.log(sortBy);
    this.analysisService.reInitializeAnalysis (this.analysis_name, sortBy)
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => this.notifyAnalysisReloaded(sortBy));
    */
  }

  /*
  notifyAnalysisReloaded(sortBy: string) {
    if (this.server_response.status == 1) {
      this.config.sortBy = sortBy;
      //this.config_heatmap_params.sortBy = sortBy;
    } else {
      alert(this.server_response.message + '. ' + this.server_response.detailed_reason);
    }
  }
  */

  childListChanged() {
    this.getUserLists();
  }

  neighborsChanged() {
    this.analysisService.reInitializeAnalysis (this.analysis_name, 'reinit')
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => this.reloadOnNeighborChange());
  }

  visualizeList(feature_list: string) {
    this.notifyListVisualization.emit(feature_list);
    this.getConfig();
    this.getHeatmapLayout();
    this.getMapContainerLayout();
    this.load_count++;
  }

  reloadOnNeighborChange() {
    if (this.server_response.status == 1) {
      this.getConfig();
      this.getHeatmapLayout();
      this.getMapContainerLayout();
      this.load_count++;
    } else {
      alert(this.server_response.message + '. ' + this.server_response.detailed_reason);
    }
  }

  toggleRowsPerPage(n:number) {
    if (n==1) {
      this.isRowsPerPageEditable=true;
      this.tempPrevRowsPerPageValue = this.config.userSpecifiedRowsPerPage;
    } else if (n==0) {
      this.isRowsPerPageEditable=false;
      if (this.tempPrevRowsPerPageValue != this.config.userSpecifiedRowsPerPage) {
        this.setRowsPerPage(this.tempPrevRowsPerPageValue);
      }
    }
  }

  toggleColsPerPage(n:number) {
    if (n==1) {
      this.isColsPerPageEditable=true;
      this.tempPrevColsPerPageValue = this.config.userSpecifiedColsPerPage;
    } else if (n==0) {
      this.isColsPerPageEditable=false;
      if (this.tempPrevColsPerPageValue != this.config.userSpecifiedColsPerPage) {
        this.setColsPerPage(this.tempPrevColsPerPageValue);
      }
    }
  }

  toggleRowLabelWidth(n:number) {
    if (n==1) {
      this.isRowLabelWidthEditable=true;
      this.tempPrevRowLabelWidthValue = this.config.rowLabelWidth;
    } else if (n==0) {
      this.isRowLabelWidthEditable=false;
      if (this.tempPrevRowLabelWidthValue != this.config.rowLabelWidth) {
        this.setRowLabelWidth(this.tempPrevRowLabelWidthValue);
      }
    }
  }

  toggleColHeaderHeight(n:number) {
    if (n==1) {
      this.isColHeaderHeightEditable=true;
      this.tempPrevColHeaderHeightValue = this.config.colHeaderHeight;
    } else if (n==0) {
      this.isColHeaderHeightEditable=false;
      if(this.tempPrevColHeaderHeightValue != this.config.colHeaderHeight) {
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
      // significance
      this.openSignificanceTestingPanel();
    }
  }

  openClusteringDialog(rc: number){
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
    } else if (rc == 1) {
      params = this.config.col_clustering_params;
    }

    var prev_data = new ClusteringParams();
    prev_data.type = params.type;
    prev_data.dataset = params.dataset;
    prev_data.use_defaults = params.use_defaults;
    prev_data.linkage_function = params.linkage_function;
    prev_data.distance_function = params.distance_function;
    prev_data.leaf_ordering = params.leaf_ordering;

    dialogConfig.data = {
      clustering_params: params,
      analysis_name: this.analysis_name,
      dataset_names: this.selected_datasets
		};
    this.clusteringDialogRef = this.dialog.open(HierarchicalClusteringComponent, dialogConfig);
    this.clusteringDialogRef.afterClosed()
    		.subscribe( data=>this.handleClusteringParamsUpdate(rc,data,prev_data) );
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

  openPhenotypeSortingPanel(){
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
      phenotypes: this.selected_phenotypes
		};
    this.phensortDialogRef = this.dialog.open(PhenotypeSortingComponent, dialogConfig);
    this.phensortDialogRef.afterClosed()
    		.subscribe( data=>this.handlephenotypeSortUpdate(data) );
  }

  handlephenotypeSortUpdate(params: PhenotypeSortingParams) {
    if (params) {
      if (params != this.config.phenotype_sorting_params) {
        this.setPhenotypeSortingParams(params);
      }
    }
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
      significance_testing_params: this.config.significance_testing_params
    };
    
    var prev_data = new SignificanceTestingParams();
    prev_data.dataset = this.config.significance_testing_params.dataset;
    prev_data.phenotype = this.config.significance_testing_params.phenotype;
    prev_data.significance_level = this.config.significance_testing_params.significance_level;

    this.sigtestDialogRef = this.dialog.open(SignificanceTestingComponent, dialogConfig);
    this.sigtestDialogRef.afterClosed()
    		.subscribe( data=>this.handleSignificanceTestingParamsUpdate(prev_data, data) );
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

  setSampleStart(sample_start: number, scrollBy: number) {
    this.globalMapConfigService.updateView (
      this.analysis_name, 'sample', sample_start, scrollBy
    )
      .subscribe(
        data => this.mapped_data_response = data, 
        () => console.log("error"),
        () => {
          if (this.mapped_data_response.status == 1) {
            this.updateParams(this.config, this.mapped_data_response);
            this.getGlobalHeatmapData();
            this.load_count++;
          } else if (this.mapped_data_response.status == 0){
            alert(this.mapped_data_response.message + ' ' + this.mapped_data_response.detailed_reason);
          }
        });
  }

  setFeatureStart(feature_start: number, scrollBy: number) {
    this.globalMapConfigService.updateView (
      this.analysis_name, 'feature', feature_start, scrollBy
    )
      .subscribe(
        data => this.mapped_data_response = data, 
        () => console.log("error"),
        () => {
          if (this.mapped_data_response.status == 1) {
            this.updateParams(this.config, this.mapped_data_response);
            this.getGlobalHeatmapData();
            this.load_count++;
          } else if (this.mapped_data_response.status == 0) {
            alert(this.mapped_data_response.message + ' ' + this.mapped_data_response.detailed_reason);
          }
        });
  }

  setMapResolution(sz: string) {
    //this.setMapContainerConfig();
    //this.globalMapConfigService.setMapResolution(this.analysis_name, this.config)
    this.globalMapConfigService.setGlobalMapConfigParam (
      this.analysis_name, 'set_map_resolution', sz
    )
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.mapResolution = sz;
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setMapOrientation(orientation: number) {
    //this.setMapContainerConfig();
    //this.globalMapConfigService.setMapResolution(this.analysis_name, this.config)
    this.globalMapConfigService.setGlobalMapConfigParam (
      this.analysis_name, 'set_map_orientation', orientation.toString()
    )
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.mapOrientation = orientation;
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setGridLayout(n_maps_per_row: number) {
    //this.config.gridLayout = n_maps_per_row;
    //this.setMapContainerConfig();
    this.globalMapConfigService.setGlobalMapConfigParam (
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
    this.globalMapConfigService.setGlobalMapConfigParam (
      this.analysis_name, 'set_col_header_height', this.config.colHeaderHeight.toString()
    )
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
          } else {
            this.config.colHeaderHeight = fallback_value;
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setRowLabelWidth(fallback_value: number) {
    this.globalMapConfigService.setGlobalMapConfigParam (
      this.analysis_name, 'set_row_label_width', this.config.rowLabelWidth.toString()
    )
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
          } else {
            this.config.rowLabelWidth = fallback_value;
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setRowsPerPage(fallback_value: number) {
    this.globalMapConfigService.updateGlobalMapConfigParam (
      this.analysis_name, 'set_rows_per_page', this.config.userSpecifiedRowsPerPage.toString()
    )
      .subscribe(
        data => this.mapped_data_response = data, 
        () => console.log("error"),
        () => {
          if (this.mapped_data_response.status == 1) {
            this.updateParams(this.config, this.mapped_data_response);
            this.getGlobalHeatmapData();
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
            this.load_count++;
          } else {
            this.config.userSpecifiedRowsPerPage = fallback_value;
            alert(this.mapped_data_response.message + ' ' + this.mapped_data_response.detailed_reason);
          }
        });
  }

  setColsPerPage(fallback_value: number) {
    this.globalMapConfigService.updateGlobalMapConfigParam (
      this.analysis_name, 'set_cols_per_page', this.config.userSpecifiedColsPerPage.toString()
    )
      .subscribe(
        data => this.mapped_data_response = data, 
        () => console.log("error"),
        () => {
          if (this.mapped_data_response.status == 1) {
            this.updateParams(this.config, this.mapped_data_response);
            this.getGlobalHeatmapData();
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
            this.load_count++;
          } else {
            this.config.userSpecifiedColsPerPage = fallback_value;
            alert(this.mapped_data_response.message + ' ' + this.mapped_data_response.detailed_reason);
          }
        });
  }

  setRowClusteringParams(params: ClusteringParams) {
    this.globalMapConfigService.setClusteringParams (
      this.analysis_name, params
    ).subscribe (
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.row_clustering_params = params;
            if (this.config.sampleOrderingScheme == 1) {
              this.config.current_sample_start = 0;
              this.getGlobalHeatmapData();
              this.load_count++;
            }
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setColClusteringParams(params: ClusteringParams) {
    this.globalMapConfigService.setClusteringParams (
      this.analysis_name, params
    ).subscribe (
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.col_clustering_params = params;
            if (this.config.columnOrderingScheme == 1) {
              this.config.current_feature_start = 0;
              this.getGlobalHeatmapData();
              this.load_count++;
            }
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setColumnOrderingScheme(scheme: string) {
    this.globalMapConfigService.setGlobalMapConfigParam (
      this.analysis_name, 'set_col_ordering', scheme
    ).subscribe (
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.columnOrderingScheme = parseInt(scheme);
            this.config.current_feature_start = 0;
            this.getGlobalHeatmapData();
            this.load_count++;
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  setSampleOrderingScheme(scheme: string) {
    this.globalMapConfigService.setGlobalMapConfigParam (
      this.analysis_name, 'set_row_ordering', scheme
    ).subscribe (
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.sampleOrderingScheme = parseInt(scheme);
            this.config.current_sample_start = 0;
            this.getGlobalHeatmapData();
            this.load_count++;
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  toggleGeneFiltering() {
    var new_param_value = !this.config.isGeneFilteringOn;
    this.globalMapConfigService.updateGlobalMapConfigParam (
      this.analysis_name, 'set_gene_filtering', new_param_value.toString()
    ).subscribe (
        data => this.mapped_data_response = data, 
        () => console.log("error"),
        () => {
          if (this.mapped_data_response.status == 1) {
            this.config.isGeneFilteringOn = new_param_value;
            this.updateParams(this.config, this.mapped_data_response);
            this.config.current_feature_start = 0;
            this.getHeatmapLayout(); 
            this.getMapContainerLayout();
            this.getGlobalHeatmapData();
            this.load_count++;
          } else {
            alert(this.mapped_data_response.message + ' ' + this.mapped_data_response.detailed_reason);
          }
        });
  }

  setGeneFilteringParams(params: SignificanceTestingParams) {
    console.log('in setGeneFilteringParams()');
    this.globalMapConfigService.updateGeneFilteringParams (
      this.analysis_name, params
    ).subscribe (
        data => this.mapped_data_response = data, 
        () => console.log("error"),
        () => {
          if (this.mapped_data_response.status == 1) {
            this.config.significance_testing_params = params;
            if (this.config.isGeneFilteringOn || this.config.columnOrderingScheme == 2) {
              this.updateParams(this.config, this.mapped_data_response);
              this.config.current_feature_start = 0;
              this.getHeatmapLayout(); 
              this.getMapContainerLayout();
              this.getGlobalHeatmapData();
              this.load_count++;
            }
          } else {
            alert(this.mapped_data_response.message + ' ' + this.mapped_data_response.detailed_reason);
          }
        });
  }

  setPhenotypeSortingParams(params: PhenotypeSortingParams) {
    this.globalMapConfigService.setPhenotypeSortingParams (
      this.analysis_name, params
    ).subscribe (
        data => this.server_response = data, 
        () => console.log("error"),
        () => {
          if (this.server_response.status == 1) {
            this.config.phenotype_sorting_params = params;
            if (this.config.sampleOrderingScheme == 0) {
              this.config.current_sample_start = 0;
              this.getGlobalHeatmapData();
              this.load_count++;
            }
          } else {
            alert(this.server_response.message + ' ' + this.server_response.detailed_reason);
          }
        });
  }

  updateParams(config: GlobalMapConfig, mapped_data: MappedData) {
    for (var i=0; i<mapped_data.names.length; i++) {
        this.updateField(config, mapped_data.names[i], mapped_data.values[i]);
    }
  }

  updateField(config: GlobalMapConfig, name: string, value: string) {
    if (name == 'mapResolution')    config.mapResolution = value;
    else if (name == 'gridLayout')  config.gridLayout = Number(value);
    else if (name == 'rowsPerPageDisplayed')    config.rowsPerPageDisplayed = Number(value);
    else if (name == 'colsPerPageDisplayed')    config.colsPerPageDisplayed = Number(value);
    else if (name == 'userSpecifiedRowsPerPage')	config.userSpecifiedRowsPerPage = Number(value);
    else if (name == 'userSpecifiedColsPerPage')	config.userSpecifiedColsPerPage = Number(value);
    else if (name == 'colHeaderHeight')	config.colHeaderHeight = Number(value);
    else if (name == 'rowLabelWidth')	config.rowLabelWidth = Number(value);
    else if (name == 'current_sample_start')	config.current_sample_start = Number(value);
    else if (name == 'current_feature_start')	config.current_feature_start = Number(value);
    else if (name == 'available_rows')	config.available_rows = Number(value);
    else if (name == 'available_cols')	config.available_cols = Number(value);
    else if (name == 'scrollBy')	config.scrollBy = Number(value);
    else if (name == 'columnOrderingScheme')	config.columnOrderingScheme = Number(value);
    else if (name == 'sampleOrderingScheme')	config.sampleOrderingScheme = Number(value);
    else if (name == 'isGeneFilteringOn')	config.isGeneFilteringOn = Boolean(value);
    else if (name == 'isSampleFilteringOn')	config.isSampleFilteringOn = Boolean(value);
  }

}
