import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { ServerResponseData } from './server_response';
import "rxjs/Rx"
import { SelectionPanelData } from './selection-panel_data';

@Injectable()
export class AnalysisService {

	private baseUrl = "http://localhost:8080/msviz-engine/AnalysisReInitializer";  // web api URL
	private demoUrl = "http://localhost:8080/msviz-engine/LoadDemo"; 
	
	constructor(private http: Http, private httpClient: HttpClient) { }

	reInitializeAnalysis (analysis_name: string, sortBy: string): Observable<ServerResponseData> {
		console.log("re-initializing analysis...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'source': 'reinit',
						  'sortByForRows': sortBy
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
	): Observable<ServerResponseData> {
		console.log("initializing analysis...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'source': 'init',
						  'dataset_names': datasets.toString(),
						  'clinicalFilters': phenotypes.toString(),
						  'groupNames': group_ids.toString(),
						  'groupTypes': group_types.toString(),
						  'row_clustering': 'False',
						  'col_clustering': 'False'
						},
				withCredentials: true
		  	})
			.map(res => <ServerResponseData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	loadDemo (): Observable<ServerResponseData> {
		console.log("loading demo...")
		return this.httpClient.get ( this.demoUrl, {
			withCredentials: true
		})
		.map(res => <ServerResponseData> res)
		.catch(error => {
		console.log(error);
		return Observable.throw(error);
		});
	}
}
