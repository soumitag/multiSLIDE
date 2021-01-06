import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
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
	private createSessionUrl = LocalSettings.MSVIZ_ENGINE_URL + "/CreateSession";
	
	constructor(private httpClient: HttpClient) { }

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
						source: string,
						search_ids: string[]
	): Observable<ServerResponseData> {
		console.log("initializing analysis...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'source': source,
						  'ids': search_ids.toString(),
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

	serializeView(analysis_name: string) {
		return this.httpClient.get ( this.serializeUrl, {
			params: { 
				'analysis_name': analysis_name,
				'action': 'generate_view'
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

	getActiveAnalyses() {
		return this.httpClient.get(this.createSessionUrl, {
		  params: {
			'action': 'get_active_analyses'
		  },
		  withCredentials: true
		  })
			.map(res => <string[][]>res)
			.catch(error => {
			  console.log(error);
			  return Observable.throw(error);
			});
	  }
	
}
