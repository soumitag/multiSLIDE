import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { Injectable } from '@angular/core';
/*import { HttpClient } from "@angular/common/http";*/
import { HttpClient } from '@angular/common/http';
import { HeatmapData } from './heatmap_data';
import { HeatmapLayout } from './heatmap_layout';
import { MapLinkLayout } from './map_link_layout';
import { MapContainerLayout } from './map-container_layout';
import { ServerResponseData } from './server_response'
import { MapConfig } from './map-config_data';
import { GlobalMapConfig } from './global-map-config_data';
import "rxjs/Rx"
import { LocalSettings } from './local-settings'
import { GlobalHeatmapData } from './global_heatmap_data';

@Injectable()
export class HeatmapService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetHeatmap";  // web api URL
	
	constructor(private httpClient: HttpClient) { }

	/*
	getHeatmapData(analysis_name: string, 
				   datasetName: string,
				   sampleStartIndex: number, 
				   entrezStartIndex: number, 
				   nSamples: number, 
				   nEntrez: number): Observable<HeatmapData> {
					   
		console.log("getting data...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'action': 'get_data',
						  'dataset_name': datasetName,
						  'sample_start': sampleStartIndex.toString(),
						  'feature_start': entrezStartIndex.toString(),
						  'nSamples': nSamples.toString(),
						  'nEntrez': nEntrez.toString()
						},
				withCredentials: true
		  	})
			.map(res => <HeatmapData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	getHeatmapLayout(analysis_name: string, 
					 datasetName: string,
					 nSamples: number, 
					 nEntrez: number, 
					 mapResolution: string): Observable<HeatmapLayout> {
		
		return this.httpClient.get ( this.baseUrl, {
				params: { 
					'analysis_name': analysis_name,
					'action': 'get_layout',
					'dataset_name': datasetName,
					'nSamples': nSamples.toString(),  
					'nEntrez': nEntrez.toString(), 
					'colHeaderHeight': '-1',
					'rowLabelWidth': '-1', 
					'mapResolution': mapResolution
				},
				withCredentials: true
		  	})
			.map(res => <HeatmapLayout> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}
	*/

	getGlobalHeatmapData (analysis_name: string): Observable<GlobalHeatmapData> {
		
		console.log("getting data...")
		return this.httpClient.get ( this.baseUrl, {
		params: { 
				'analysis_name': analysis_name,
				'action': 'get_global_data'
				},
		withCredentials: true
		})
		.map(res => <GlobalHeatmapData> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}

	getHeatmapData(	analysis_name: string, 
					datasetName: string): Observable<HeatmapData> {
					
		console.log("getting data...")
		return this.httpClient.get ( this.baseUrl, {
			params: { 
					'analysis_name': analysis_name,
					'action': 'get_data',
					'dataset_name': datasetName
					},
			withCredentials: true
		})
		.map(res => <HeatmapData> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}

	getHeatmapLayout(analysis_name: string, dataset_name: string): Observable<HeatmapLayout> {

		return this.httpClient.get ( this.baseUrl, {
			params: { 
				'analysis_name': analysis_name,
				'dataset_name': dataset_name,
				'action': 'get_layout'
			},
			withCredentials: true
		})
		.map(res => <HeatmapLayout> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}

	getMapContainerLayout(analysis_name: string): Observable<MapContainerLayout> {
		
		return this.httpClient.get ( this.baseUrl, {
				params: { 
					'analysis_name': analysis_name,
					'action': 'get_container_layout',
				},
				withCredentials: true
		  	})
			.map(res => <MapContainerLayout> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	getMapLinkLayout(analysis_name: string,
		dataset_name_1: string, dataset_name_2: string): Observable<MapLinkLayout> {

		console.log("getting data...")
		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'get_link_layout',
				'dataset_name_1': dataset_name_1,
				'dataset_name_2': dataset_name_2
			},
			withCredentials: true
		})
			.map(res => <MapLinkLayout>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	resetMap(analysis_name: string,
			 dataset_name: string,
			 mapConfig: MapConfig): Observable<ServerResponseData> {

		return this.httpClient.get(this.baseUrl, {
				params: {
					'analysis_name': analysis_name,
					'action': 'reset_heatmap',
					'dataset_name': dataset_name,
					'numColors': mapConfig.nColors.toString(),
					'binning_range': mapConfig.binningRange,
					'color_scheme': mapConfig.colorScheme,
					'binning_range_start': mapConfig.binningRangeStart.toString(),
					'binning_range_end': mapConfig.binningRangeEnd.toString(),
					'selected_feature_identifiers': mapConfig.selected_feature_identifiers.toString(),
					'show_aggregated': mapConfig.show_aggregated.toString(),
					'aggregate_function': mapConfig.aggregate_function
				},
				withCredentials: true
			})
			.map(res => <ServerResponseData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	getMapConfig(analysis_name: string, dataset_name: string): Observable<MapConfig> {

   		return this.httpClient.get(this.baseUrl, {
		   params: {
			   'analysis_name': analysis_name,
			   'action': 'get_map_config',
			   'dataset_name': dataset_name
		   },
		   withCredentials: true
	   })
	   .map(res => <MapConfig>res)
	   .catch(error => {
		   console.log(error);
		   return Observable.throw(error);
	   });
	}

	
}
