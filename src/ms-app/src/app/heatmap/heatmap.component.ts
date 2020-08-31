import { Component, OnInit, Input, Output, OnChanges, SimpleChange, EventEmitter } from '@angular/core';
import { HeatmapData } from '../heatmap_data';
import { HeatmapService } from '../heatmap.service'
import { HeatmapLayout } from '../heatmap_layout';
import { NewListComponent } from '../new-list/new-list.component'
import { MapSettingsComponent } from '../map-settings/map-settings.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material/dialog";
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
	@Input() global_data: GlobalHeatmapData;
	@Input() datasetName: string;
	@Input() mapResolution: string;
	@Input() clinicalFilters: string[];
	@Input() geneTags: string[];
	@Input() sortBy: string;
	@Input() feature_list_names: string[];
	@Input() sample_list_names: string[];
	@Input() load_count: number;
	@Input() load_layout_count: number;
	@Input() isTransposed: number;
	@Input() isForPrint: number;

	@Output() onSortOrderChange = new EventEmitter<string>();
	@Output() onListChange = new EventEmitter<string>();
	@Output() onNeighborsChange = new EventEmitter();

	data: HeatmapData;	
	layout: HeatmapLayout;
	list_service_response: ServerResponseData;
	add_neighbor_service_response: ServerResponseData;
	mapconfig_server_response: ServerResponseData;
	mapSettingsDialogRef: MatDialogRef<MapSettingsComponent>;
	//custom_gene_identifiers: string[];
	col_order: number[];
	nSamples: number;
	nEntrez: number;
	//isTransposed: boolean = true;

	public hist = {
		data: [

			{
				type: "bar",
				name: "",
				showlegend: false,
				marker: {
					color: []
				}
			},

			{
				type: "bar",
				showlegend: false,
				name: "",
				marker: {
					color: [],
					line: {
						color: 'gray',
						width: 2
					},

				}
			},

		],
		layout: {
			barmode: 'overlay', width: 300, height: 300, title: '', bargap: 0.0,
			xaxis: {
				showgrid: true, mirror: true,
				ticks: 'outside',
				showline: true, nticks: 5
			}, yaxis: {
				showgrid: true, mirror: true,
				ticks: 'outside',
				showline: true, nticks: 5
			},
			margin: {
				l: 40,
				r: 2,
				b: 40,
				t: 2,
				pad: 0
			  }
		},
		config: {
			'displayModeBar': false,
			staticPlot: true
		},
	};

	constructor(private heatmapService: HeatmapService,
				private listService: ListService,
				private assortedService: AssortedService,
				private dialog: MatDialog) { 

					/*
					this.hist['layout']['width'] = 272
					this.hist['layout']['height'] = 152

					this.hist['data'][1]['x'] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
                	this.hist['data'][1]['y'] = [6, 10, 3, 6, 10, 3, 6, 10, 36, 10, 3]
                	this.hist['data'][1]['marker']['color'] = ['rgb(0,0,195)', 'rgb(142,124,195)', 'rgb(142,124,195)', 'rgb(142,124,195)',
                	'rgb(142,124,195)', 'rgb(142,124,195)', 'rgb(142,124,195)',
					'rgb(142,124,195)', 'rgb(142,124,195)', 'rgb(142,12,195)', 'rgb(133,124,195)']
				
					this.hist['data'][0]['x'] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
					this.hist['data'][0]['y'] = [36, 36, 36, 36, 36, 36, 36, 36, 36, 36, 36]
					this.hist['data'][0]['marker']['color'] = ['rgb(0,0,195)', 'rgb(142,124,195)', 'rgb(142,124,195)', 'rgb(142,124,195)',
					'rgb(142,124,195)', 'rgb(142,124,195)', 'rgb(142,124,195)',
					'rgb(142,124,195)', 'rgb(142,124,195)', 'rgb(142,12,195)', 'rgb(133,124,195)']
					*/
				}

	ngOnInit() { 
		this.getHeatmapData();
		this.getHeatmapLayout();
		console.log("Started.");
		//console.log(this.data);
	}
	
	ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		
		for (let propName in changes) {
			if (propName == "gene_mask") {
				if(!changes['gene_mask'].isFirstChange()) {
					this.data = null;			// nullify it so that "Loading..." message is displayed
					this.layout = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					this.getHeatmapLayout();
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
					this.layout = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapData();
					this.getHeatmapLayout();
					console.log('ngOnChnages: ' + propName);
				}
			} else if (propName == "load_layout_count") {
				if(!changes['load_layout_count'].isFirstChange()) {
					this.layout = null;			// nullify it so that "Loading..." message is displayed
					this.getHeatmapLayout();
					console.log('ngOnChnages: ' + propName);
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
											this.datasetName)
			.subscribe(
				data => this.setData(data), 
				() => console.log("observable complete"));
	}

	setData(data: HeatmapData) {
		this.data = data;
		this.nSamples = data.nSamples;
		this.nEntrez = data.nEntrez;

		var max_freq = Math.max.apply(null, data.hist_frequencies);
		console.log(max_freq)

		this.hist['data'][0]['marker']['color'] = [];
		this.hist['data'][0]['x'] = [];
		this.hist['data'][0]['y'] = [];
		this.hist['data'][1]['marker']['color'] = [];
		this.hist['data'][1]['x'] = [];
		this.hist['data'][1]['y'] = [];
		for (var i=0; i<data.bin_colors.length-1; i++) {
			var cs = 'rgb(' + data.bin_colors[i][0] + ',' + data.bin_colors[i][1] + ',' + data.bin_colors[i][2] + ')';
			this.hist['data'][0]['marker']['color'].push(cs);
			this.hist['data'][0]['x'].push(data.hist_x_values[i]);
			this.hist['data'][0]['y'].push(max_freq);
			this.hist['data'][1]['marker']['color'].push(cs);
			this.hist['data'][1]['x'].push(data.hist_x_values[i]);
			this.hist['data'][1]['y'].push(data.hist_frequencies[i]);
		}

		console.log(this.isForPrint);
	}
	
	getHeatmapLayout(): void {
		this.heatmapService.getHeatmapLayout(this.analysis_name, 
											 this.datasetName)
			.subscribe(data => this.setLayout(data), () => console.log("observable complete"));
	}

	setLayout(data: HeatmapLayout) {
		this.layout = data;
		this.hist['layout']['width'] = data.hist_width;
		this.hist['layout']['height'] = data.hist_height;
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
			list_data = this.global_data.gene_group_keys[this.data.gene_tags[col_index][0]];
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
			list_data = this.data.entrez[col_index];
		} else if (add_type_ind == 1) {
			add_type = "feature_group";
			list_data = this.global_data.gene_group_keys[this.data.gene_tags[col_index][0]];
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
			datasetName: this.datasetName
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
			.subscribe(data => this.handleServerResponse_MapConfigUpdate(data), () => console.log("observable complete"));
		}
	}
	
	handleServerResponse_MapConfigUpdate(response: ServerResponseData) {
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

	searchNeighbors(col_index: number, search_type_code: number) {
		var entrez = this.data.entrez[col_index];
		var display_tag = this.data.column_headers[col_index];
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
