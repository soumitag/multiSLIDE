import { Component, OnInit, Input, Output, OnChanges, SimpleChange, EventEmitter } from '@angular/core';
import { HeatmapData } from '../heatmap_data';
import { HeatmapService } from '../heatmap.service'
import { HeatmapLayout } from '../heatmap_layout';
import { NewListComponent } from '../new-list/new-list.component'
import { MapSettingsComponent } from '../map-settings/map-settings.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";
import { ListService } from '../list.service';
import { ServerResponseData } from '../server_response'
import { MapConfig } from '../map-config_data'

@Component({
  selector: 'app-heatmap',
  templateUrl: './heatmap.component.html',
  styleUrls: ['./heatmap.component.css']
})

export class HeatmapComponent implements OnInit, OnChanges {

	@Input() analysis_name: string;
	@Input() nSamples: number;
	@Input() nEntrez: number;
	@Input() datasetName: string;
	@Input() sampleStartIndex: number;
	@Input() entrezStartIndex: number;
	@Input() mapResolution: string;
	@Input() clinicalFilters: string[];
	@Input() geneTags: string[];
	@Input() sortBy: string;
	@Input() feature_list_names: string[];
	@Input() sample_list_names: string[];
	@Input() load_count: number;

	@Output() onSortOrderChange = new EventEmitter<string>();
	@Output() onListChange = new EventEmitter<string>();

	data: HeatmapData;	
	layout: HeatmapLayout;
	list_service_response: ServerResponseData;
	mapconfig_server_response: ServerResponseData;
	custom_gene_identifiers: string[];

	constructor(private heatmapService: HeatmapService,
				private listService: ListService,
				private dialog: MatDialog) { }

	ngOnInit() { 
		this.getHeatmapData();
		this.getHeatmapLayout();
		console.log("Started.");
		//console.log(this.data);
	}
	
	ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		for (let propName in changes) {
			if (propName == "sampleStartIndex" || propName == "entrezStartIndex") {
				this.data = null;			// nullify it so that "Loading..." message is displayed
				this.getHeatmapData();
			} else if (propName == "mapResolution") {
				this.layout = null;			// nullify it so that "Loading..." message is displayed
				this.getHeatmapLayout();
			} else if (propName == "sortBy") {
				this.data = null;			// nullify it so that "Loading..." message is displayed
				this.getHeatmapData();
			}  else if (propName == "load_count") {
				this.data = null;			// nullify it so that "Loading..." message is displayed
				this.layout = null;			// nullify it so that "Loading..." message is displayed
				this.getHeatmapData();
				this.getHeatmapLayout();
			}
		}
	}

	changeSortOrder(sortBy: string) {
		this.onSortOrderChange.emit(sortBy);
	}

	getHeatmapData(): void {
		/*
		this.heatmapService.getHeatmapData(this.analysis_name,
										   this.datasetName,
										   this.sampleStartIndex,
										   this.entrezStartIndex,
										   this.nSamples, 
										   this.nEntrez)
			.subscribe(data => this.data = data, () => console.log("observable complete"));
		*/
		this.heatmapService.getHeatmapData(	this.analysis_name,
											this.datasetName,
											this.sampleStartIndex,
											this.entrezStartIndex)
			.subscribe(data => this.data = data, () => console.log("observable complete"));
	}

	getHeatmapLayout(): void {
		/*
		this.heatmapService.getHeatmapLayout(this.analysis_name, 
											 this.datasetName, 
											 this.nSamples, 
											 this.nEntrez,
											 this.mapResolution)
			.subscribe(data => this.layout = data, () => console.log("observable complete"));
		*/
		this.heatmapService.getHeatmapLayout(this.analysis_name, 
											 this.datasetName)
			.subscribe(data => this.layout = data, () => console.log("observable complete"));
	}

	onContextMenuAction1(item: string) {
		alert('Click on Action 1 for entrez = ' + item);
	}

	onContextMenuAction2(item: string) {
		alert('Click on Action 2 for entrez = ' + item);
	}

	createListAndAdd (col_index: number, add_type_ind: number) {
		var add_type = "";
		var list_data = "";
		if (add_type_ind == 0) {
			add_type = "single_feature";
			list_data = this.data.entrez[col_index];
		} else if (add_type_ind == 1) {
			add_type = "feature_group";
			list_data = this.data.gene_group_keys[this.data.gene_tags[col_index]];
		} else {
			alert('Bad param: add_type');
			return;
		}
		let entrez = this.data.entrez[col_index]
		this.listService.createListAndAdd(this.analysis_name, entrez, add_type)
			.subscribe(
				data => this.list_service_response = data, 
				() => console.log("observable complete"), 
          		() => this.notifyResponse()
      );
	}

	addToList (col_index: number, feature_list_name: string, add_type_ind: number) {
		
		var add_type = "";
		var list_data = "";
		if (add_type_ind == 0) {
			add_type = "single_feature";
			list_data = this.data.entrez[col_index];
		} else if (add_type_ind == 1) {
			add_type = "feature_group";
			list_data = this.data.gene_group_keys[this.data.gene_tags[col_index]];
		} else {
			alert('Bad param: add_type');
			return;
		}
		console.log('Entrez / Group Name: ' + list_data + ', List: ' + feature_list_name);
		this.listService.addToList(this.analysis_name, list_data, feature_list_name, add_type)
			.subscribe(
					data => this.list_service_response = data, 
					() => console.log("observable complete"), 
					() => this.notifyResponse());
	}

	notifyResponse() {
		if(this.list_service_response.status == 1) {
			alert(this.list_service_response.message);
		} else {
			alert(this.list_service_response.message + '. ' + this.list_service_response.detailed_reason);
		}
		this.onListChange.emit();
	}

	openNewListDialog() {
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '300';
		dialogConfig.width = '600';
		dialogConfig.position = {
			top: '200',
			left: '200'
		};
		this.dialog.open(NewListComponent, dialogConfig);
	}

	mapSettingsDialogRef: MatDialogRef<MapSettingsComponent>;

	openSettingsDialog() {
		
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '700';
		dialogConfig.width = '600';
		dialogConfig.position = {
			top: '200',
			left: '200'
		};
		dialogConfig.data = {
			analysis_name: this.analysis_name,
			datasetName: this.datasetName,
			custom_gene_identifiers: this.custom_gene_identifiers
		};
		this.mapSettingsDialogRef = this.dialog.open(MapSettingsComponent, dialogConfig);

		this.mapSettingsDialogRef.afterClosed()
    		.subscribe( data=>this.applyMapConfigChanges(data) );
	}

	applyMapConfigChanges(new_map_config: MapConfig) {
		if (new_map_config != null) {
			this.heatmapService.resetMap(this.analysis_name,
						this.datasetName,
						new_map_config)
			.subscribe(data => this.handleServerResponse_MapConfigUpdate(data, new_map_config), () => console.log("observable complete"));
		}
	}
	
	handleServerResponse_MapConfigUpdate(response: ServerResponseData, new_map_config: MapConfig) {
		this.mapconfig_server_response = response;
		if (this.mapconfig_server_response.status == 1) {
			this.layout = null;			// nullify it so that "Loading..." message is displayed
			this.getHeatmapLayout();
			this.data = null;			// nullify it so that "Loading..." message is displayed
			this.getHeatmapData();
		} else {
		  alert('Heatmap Settings Update Failed: ' + this.mapconfig_server_response.message + ' ' + this.mapconfig_server_response.detailed_reason + ' Please try again.');
		}
	}

}
