import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { ServerResponseData } from './server_response';
import "rxjs/Rx"
import { SelectionPanelData } from './selection-panel_data';
import { LocalSettings } from './local-settings'

@Injectable()
export class AnalysisService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/AnalysisReInitializer";  // web api URL
	private demoUrl = LocalSettings.MSVIZ_ENGINE_URL + "/LoadDemo"; 
	private serializeUrl = LocalSettings.MSVIZ_ENGINE_URL + "/SerializeAnalysis"; 
	
	constructor(private http: Http, private httpClient: HttpClient) { }

	reInitializeAnalysis (analysis_name: string, source: string): Observable<ServerResponseData> {
		console.log("re-initializing analysis...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'source': source
						},
				withCredentials: true
		  	})
			.map(res => <ServerResponseData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	initializeAnalysis (analysis_name: string, 
						datasets: string[],
						phenotypes: string[],
						group_ids: string[],
						group_types: number[],
						group_display_tags: string[],
						intersection_counts: number[],
						intersection_counts_per_dataset: string,
						background_counts: number[]
	): Observable<ServerResponseData> {
		console.log("initializing analysis...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'source': 'init',
						  'dataset_names': datasets.toString(),
						  'clinicalFilters': phenotypes.toString(),
						  'groupIDs': group_ids.toString(),
						  'groupTypes': group_types.toString(),
						  'groupNames': group_display_tags.toString(),
						  'intersection_counts': intersection_counts.toString(),
						  'intersection_counts_per_dataset': intersection_counts_per_dataset,
						  'background_counts': background_counts.toString()
						},
				withCredentials: true
		  	})
			.map(res => <ServerResponseData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	initializeAnalysisFromList (
		analysis_name: string, 
		feature_list: string
	): Observable<ServerResponseData> {
		console.log("initializing analysis...")
		return this.httpClient.get ( this.baseUrl, {
			params: { 
					'analysis_name': analysis_name,
					'source': 'list',
					'feature_list': feature_list
					},
			withCredentials: true
		})
		.map(res => <ServerResponseData> res)
		.catch(error => {
		console.log(error);
		return Observable.throw(error);
		});
	}

	loadDemo (generated_number : number, demo_number: number): Observable<ServerResponseData> {
		console.log("loading demo...")
		return this.httpClient.get ( this.demoUrl, { 
			params: {
				'demo_id' : generated_number.toString(),
				'demo_number' : demo_number.toString(),
			},
			withCredentials: true
		})
		.map(res => <ServerResponseData> res)
		.catch(error => {
		console.log(error);
		return Observable.throw(error);
		});
	}

	serializeWorkspace(analysis_name: string) {
		return this.httpClient.get ( this.serializeUrl, {
			params: { 
				'analysis_name': analysis_name,
				'action': 'generate'
			},
			withCredentials: true,
		})
		.map(res => <ServerResponseData> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}

	downloadWorkspace(analysis_name: string, filename: string) {
		return this.httpClient.get ( this.serializeUrl, {
			params: { 
				'analysis_name': analysis_name,
				'filename': filename,
				'action': 'download'
			},
			withCredentials: true,
			responseType: 'blob'
		})
		.map(res => <Blob> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}

	deleteAnalysis (analysis_name: string) {
		return this.httpClient.get ( this.serializeUrl, {
			params: { 
				'analysis_name': analysis_name,
				'action': 'delete'
			},
			withCredentials: true
		})
		.map(res => <ServerResponseData> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	}
}
