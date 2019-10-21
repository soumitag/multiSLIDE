import { Component, OnInit, Input, Output, OnChanges, SimpleChange, EventEmitter } from '@angular/core';
import { HeatmapData } from '../heatmap_data';
import { HeatmapService } from '../heatmap.service'
import { HeatmapLayout } from '../heatmap_layout';
import { NewListComponent } from '../new-list/new-list.component'
import { MapSettingsComponent } from '../map-settings/map-settings.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";
import { ListService } from '../list.service';
import { AssortedService } from '../assorted.service';
import { ServerResponseData } from '../server_response'
import { MapConfig } from '../map-config_data'
import { NetworkNeighborhoodComponent } from '../network-neighborhood/network-neighborhood.component'
import { GlobalHeatmapData } from '../global_heatmap_data';

@Component({
  selector: 'app-heatmap',
  templateUrl: './heatmap.component.html',
  styleUrls: ['./heatmap.component.css']
})

export class HeatmapComponent implements OnInit, OnChanges {

	@Input() analysis_name: string;
	@Input() layout: HeatmapLayout;
	@Input() global_data: GlobalHeatmapData;
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
	@Input() isTransposed: number;

	@Output() onSortOrderChange = new EventEmitter<string>();
	@Output() onListChange = new EventEmitter<string>();
	@Output() onNeighborsChange = new EventEmitter();

	data: HeatmapData;	
	//layout: HeatmapLayout;
	list_service_response: ServerResponseData;
	add_neighbor_service_response: ServerResponseData;
	mapconfig_server_response: ServerResponseData;
	mapSettingsDialogRef: MatDialogRef<MapSettingsComponent>;
	custom_gene_identifiers: string[];
	col_order: number[];
	//isTransposed: boolean = true;

	constructor(private heatmapService: HeatmapService,
				private listService: ListService,
				private assortedService: AssortedService,
				private dialog: MatDialog) { }

	ngOnInit() { 
		this.getHeatmapData();
		//this.getHeatmapLayout();
		console.log("Started.");
		//console.log(this.data);
	}
	
	ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		
		for (let propName in changes) {
			if (propName == "gene_mask") {
				if(!changes['gene_mask'].isFirstChange()) {
					this.data = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					console.log('ngOnChnages: ' + propName);
				}
			} else if (propName == "sampleStartIndex") {
				if(!changes['sampleStartIndex'].isFirstChange()) {
					this.data = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					console.log('ngOnChnages: ' + propName);
				}
			} else if (propName == "entrezStartIndex") {
				if(!changes['entrezStartIndex'].isFirstChange()) {
					this.data = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					console.log('ngOnChnages: ' + propName);
				}
			} else if (propName == "mapResolution") {
				//this.layout = null;			// nullify it so that "Loading..." message is displayed
				//this.getHeatmapLayout();
			} else if (propName == "sortBy") {
				if(!changes['sortBy'].isFirstChange()) {
					this.data = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					console.log('ngOnChnages: ' + propName);
				}
			}  else if (propName == "load_count") {
				if(!changes['load_count'].isFirstChange()) {
					this.data = null;			// nullify it so that "Loading..." message is displayed
					//this.layout = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					console.log('ngOnChnages: ' + propName);
					//this.getHeatmapLayout();
				}
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

	/*
	getHeatmapLayout(): void {
		this.heatmapService.getHeatmapLayout(this.analysis_name, 
											 this.datasetName)
			.subscribe(data => this.layout = data, () => console.log("observable complete"));
	}
	*/

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
			list_data = this.global_data.entrez[col_index];
		} else if (add_type_ind == 1) {
			add_type = "feature_group";
			list_data = this.global_data.gene_group_keys[this.global_data.gene_tags[col_index][0]];
		} else {
			alert('Bad param: add_type');
			return;
		}
		this.listService.createListAndAdd(this.analysis_name, list_data, add_type)
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
			list_data = this.global_data.entrez[col_index];
		} else if (add_type_ind == 1) {
			add_type = "feature_group";
			list_data = this.global_data.gene_group_keys[this.global_data.gene_tags[col_index][0]];
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
			//this.layout = null;			// nullify it so that "Loading..." message is displayed
			//this.getHeatmapLayout();
			this.data = null;			// nullify it so that "Loading..." message is displayed
			this.getHeatmapData();
		} else {
		  alert('Heatmap Settings Update Failed: ' + this.mapconfig_server_response.message + ' ' + this.mapconfig_server_response.detailed_reason + ' Please try again.');
		}
	}

	searchNeighbors(col_index: number, search_type_code: number) {
		var entrez = this.global_data.entrez[col_index];
		var display_tag = this.global_data.column_headers[col_index];
		var search_type = '';
		if (search_type_code == 0) {
			search_type = 'ppi_entrez';
		} else if (search_type_code == 1) {
			search_type = 'mirna_id';
		} else if (search_type_code == 2) {
			search_type = 'tf_entrez';
		}

		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '600';
		dialogConfig.width = '900';
		dialogConfig.position = {
			top: '200',
			left: '100'
		};
		dialogConfig.data = {
			analysis_name: this.analysis_name,
			dataset_name: this.datasetName,
			entrez: entrez,
			query_display_tag: display_tag,
			search_type: search_type
		};
		var neighborhoodSearchDialogRef = this.dialog.open(NetworkNeighborhoodComponent, dialogConfig);

		neighborhoodSearchDialogRef.afterClosed()
    		.subscribe( data=>this.addNetworkNeighbors(entrez, data, search_type) );

	}

	addNetworkNeighbors(
		query_entrez: string, 
		neighbor_entrez_list: string[],
		network_type: string
	) {
		if (neighbor_entrez_list && neighbor_entrez_list.length > 0) {
			if (neighbor_entrez_list.length < 20) {
				this.assortedService.addNetworkNeighbors_small_payload(
					this.analysis_name, 
					this.datasetName, 
					query_entrez,
					neighbor_entrez_list, 
					network_type
				)
				.subscribe(
					data => this.add_neighbor_service_response = data, 
					() => console.log("observable complete"),
					() => this.notifySearchStatus()
				);
			} else {
				this.assortedService.addNetworkNeighbors_large_payload(
					this.analysis_name, 
					this.datasetName, 
					query_entrez,
					neighbor_entrez_list, 
					network_type
				)
				.subscribe(
					data => this.add_neighbor_service_response = data, 
					() => console.log("observable complete"),
					() => this.notifySearchStatus()
				);
			}
		}
	}

	notifySearchStatus() {
		if(this.add_neighbor_service_response.status == 1) {
			this.onNeighborsChange.emit();
		} else {
			alert(this.add_neighbor_service_response.message + '. ' + this.add_neighbor_service_response.detailed_reason);
		}
	}

	removeNetworkNeighbors (search_tag_index: number) {
		let neighbor_id = this.global_data.search_tag_ids[search_tag_index];
		this.assortedService.removeNetworkNeighbors(
			this.analysis_name, 
			neighbor_id 
		)
		.subscribe(
			data => this.add_neighbor_service_response = data, 
			() => console.log("observable complete"),
			() => this.notifySearchStatus()
		);
	}

	/*
	downloadPdf() {
		var data = document.getElementById('print_trial');  
		html2canvas(data).then(canvas => {  
		  // Few necessary setting options  
		  var imgWidth = 2000;   
		  var pageHeight = 295;    
		  var imgHeight = canvas.height * imgWidth / canvas.width;  
		  var heightLeft = imgHeight;  
	  
		  const contentDataURL = canvas.toDataURL('image/png')  
		  let pdf = new jsPDF('p', 'mm', 'a4'); // A4 size page of PDF  
		  var position = 0;  
		  pdf.addImage(contentDataURL, 'PNG', 0, position, imgWidth, imgHeight)  
		  pdf.save('MYPdf.pdf'); // Generated PDF   
		});  
	}
	*/
}
