import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { HeatmapData } from './heatmap_data';
import { HeatmapLayout } from './heatmap_layout';
import { MapContainerLayout } from './map-container_layout';
import { ServerResponseData } from './server_response'
import { MapConfig } from './map-config_data';
import { GlobalMapConfig } from './global-map-congif_data';
import "rxjs/Rx"

@Injectable()
export class HeatmapService {

	private baseUrl = "http://localhost:8080/msviz-engine/GetHeatmap";  // web api URL
	
	constructor(private http: Http, private httpClient: HttpClient) { }

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

	getHeatmapData(	analysis_name: string, 
					datasetName: string,
					sampleStartIndex: number, 
					entrezStartIndex: number): Observable<HeatmapData> {
					
		console.log("getting data...")
		return this.httpClient.get ( this.baseUrl, {
			params: { 
					'analysis_name': analysis_name,
					'action': 'get_data',
					'dataset_name': datasetName,
					'sample_start': sampleStartIndex.toString(),
					'feature_start': entrezStartIndex.toString()
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
					 datasetName: string): Observable<HeatmapLayout> {

		return this.httpClient.get ( this.baseUrl, {
			params: { 
				'analysis_name': analysis_name,
				'action': 'get_layout',
				'dataset_name': datasetName
			},
			withCredentials: true
		})
		.map(res => <HeatmapLayout> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}

	getMapContainerLayout(analysis_name: string,
						  config: GlobalMapConfig): Observable<MapContainerLayout> {
		
		return this.httpClient.get ( this.baseUrl, {
				params: { 
					'analysis_name': analysis_name,
					'action': 'get_container_layout',
					'nSamples': config.rowsPerPage.toString(),  
					'nEntrez': config.colsPerPage.toString(), 
					'colHeaderHeight': config.colHeaderHeight.toString(),
					'rowLabelWidth': config.rowLabelWidth.toString(), 
					'mapResolution': config.mapResolution,
					'gridLayout': config.gridLayout.toString()
				},
				withCredentials: true
		  	})
			.map(res => <MapContainerLayout> res)
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
					'column_label': mapConfig.columnLabel,
					'numColors': mapConfig.nColors.toString(),
					'binning_range': mapConfig.binningRange,
					'color_scheme': mapConfig.colorScheme,
					'binning_range_start': mapConfig.binningRangeStart.toString(),
					'binning_range_end': mapConfig.binningRangeEnd.toString()
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

	getGlobalMapConfig(analysis_name: string): Observable<GlobalMapConfig> {

		return this.httpClient.get(this.baseUrl, {
		params: {
			'analysis_name': analysis_name,
			'action': 'get_global_map_config',
		},
		withCredentials: true
		})
		.map(res => <GlobalMapConfig>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
 	}
}
