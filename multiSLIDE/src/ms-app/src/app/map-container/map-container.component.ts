import { Component, OnInit, Input, SimpleChange } from '@angular/core';
import { HeatmapService } from '../heatmap.service'
import { AnalysisService } from '../analysis.service'
import { MapContainerLayout } from '../map-container_layout'
import { ListData } from '../list_data'
import { ServerResponseData } from '../server_response'
import { ListService } from '../list.service';
import { GlobalMapConfig } from '../global-map-congif_data';
import { config } from 'rxjs';

@Component({
  selector: 'app-map-container',
  templateUrl: './map-container.component.html',
  styleUrls: ['./map-container.component.css']
})
export class MapContainerComponent implements OnInit {

  @Input() load_count: number;
  @Input() analysis_name: string;
  @Input() containerDisplayOn: boolean;

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
  
  /*
  nTotalSamples = 133;
  nTotalFeatures = 562;
  clinicalFilters: string[] = ['LYMPH_NODES_EXAMINED','SEX','ETHNICITY'];
  geneTags: string[] = [];
  datasetNames: string[] = ['CNA', 'DNA Methylation', 'mRNA', 'Protein'];
  */

  server_response: ServerResponseData;
  layout: MapContainerLayout;
  
  lists: ListData[];
  feature_list_names: string[] = [];
  sample_list_names: string[] = [];

  N: number[] = [];

  isRowsPerPageEditable: boolean = false;
  isColsPerPageEditable: boolean = false;
  isRowLabelWidthEditable: boolean = false;
  isColHeaderHeightEditable: boolean = false;

  tempPrevRowsPerPageValue: number;
  tempPrevColsPerPageValue: number;
  tempPrevRowLabelWidthValue: number;
  tempPrevColHeaderHeightValue: number;

  constructor(private heatmapService: HeatmapService, 
              private analysisService: AnalysisService,
              private listService: ListService) { }

  ngOnInit() {
    if (this.containerDisplayOn) {
      this.getConfig();
      this.getUserLists();
      this.load_count++;
    }
  }

  initContainer(server_config: GlobalMapConfig) {
    this.config = server_config;
    this.getMapContainerLayout();
  }

  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		for (let propName in changes) {
			if (propName == "load_count") {
        this.layout = null;
				this.ngOnInit()
			}
		}
	}

  getMapContainerLayout(): void {
    this.heatmapService.getMapContainerLayout (this.analysis_name,
                                               this.config)
      .subscribe(data => this.layout = data, 
                () => console.log("observable complete"),
                () => this.initializeN());
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
    this.heatmapService.getGlobalMapConfig (this.analysis_name)
      .subscribe(data => this.initContainer(data), () => console.log("observable complete"));
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

  load_row_view(direction: number): void {
    if (direction == 1) {
      this.config.current_sample_start = this.config.current_sample_start + this.config.rowsPerPage;
    } else if (direction == -1) {
      this.config.current_sample_start = this.config.current_sample_start - this.config.rowsPerPage;
    }
  }

  load_column_view(direction: number): void {
    if (direction == 1) {
      this.config.current_feature_start = this.config.current_feature_start + this.config.colsPerPage;
    } else if (direction == -1) {
      this.config.current_feature_start = this.config.current_feature_start - this.config.colsPerPage;
    }
  }

  setMapResolution(sz) {
    this.config.mapResolution = sz;
    this.getMapContainerLayout();
  }

  setGridLayout(n_maps_per_row) {
    this.config.gridLayout = n_maps_per_row;
    this.getMapContainerLayout();
  }

  childSortOrderChanged(sortBy: string) {
    console.log(sortBy);
    this.analysisService.reInitializeAnalysis (this.analysis_name, sortBy)
      .subscribe(
        data => this.server_response = data, 
        () => console.log("error"),
        () => this.notifyAnalysisReloaded(sortBy));
  }

  childListChanged() {
    this.getUserLists();
  }

  notifyAnalysisReloaded(sortBy: string) {
    if (this.server_response.status == 1) {
      this.config.sortBy = sortBy;
    } else {
      alert(this.server_response.message + '. ' + this.server_response.detailed_reason);
    }
  }

  toggleRowsPerPage(n:number) {
    if (n==1) {
      this.isRowsPerPageEditable=true;
      this.tempPrevRowsPerPageValue = this.config.rowsPerPage;
    } else if (n==0) {
      this.isRowsPerPageEditable=false;
      if (this.tempPrevRowsPerPageValue != this.config.rowsPerPage) {
        this.layout = null;
        this.getMapContainerLayout();
      }
    }
  }

  toggleColsPerPage(n:number) {
    if (n==1) {
      this.isColsPerPageEditable=true;
      this.tempPrevColsPerPageValue = this.config.colsPerPage;
    } else if (n==0) {
      this.isColsPerPageEditable=false;
      if (this.tempPrevColsPerPageValue != this.config.colsPerPage) {
        this.layout = null;
        this.getMapContainerLayout();
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
        this.layout = null;
        this.getMapContainerLayout();
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
        this.layout = null;
        this.getMapContainerLayout();
      }
    }
  }

}
