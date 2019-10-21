import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SelectionPanelData } from './selection-panel_data';
import { SelectionPanelState } from './selection-state_data';
import { LegendData } from './legend_data';
import "rxjs/Rx"
import { forkJoin } from 'rxjs';
import { ServerResponseData } from './server_response';
import { LocalSettings } from './local-settings'

@Injectable()
export class AssortedService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetPhenotypes";
	private legendsUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetFigureLegends";
	private networkNeighborsUrl = LocalSettings.MSVIZ_ENGINE_URL + "/NetworkNeighborServices";

	constructor(private http: Http, private httpClient: HttpClient) { }

	getSelectionPanelDataAndState(analysis_name: string): Observable<any[]> {
		let data = this.getSelectionPanelData(analysis_name);
		let state = this.getSelectionPanelState(analysis_name);
		return forkJoin([data, state]);
	}

	getSelectionPanelData(analysis_name: string): Observable<SelectionPanelData> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'panel_data'
			},
			withCredentials: true
		})
			.map(res => <SelectionPanelData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	getSelectionPanelState(analysis_name: string): Observable<SelectionPanelState> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'panel_state'
			},
			withCredentials: true
		})
			.map(res => <SelectionPanelState>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	getLegendData(analysis_name: string): Observable<LegendData> {

		return this.httpClient.get(this.legendsUrl, {
			params: {
				'analysis_name': analysis_name
			},
			withCredentials: true
		})
			.map(res => <LegendData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	addNetworkNeighbors_small_payload (
		analysis_name: string,
		dataset_name: string,
		query_entrez: string,
		neighbor_entrez: string[],
		network_type: string
	): Observable<ServerResponseData> {

		return this.httpClient.get(this.networkNeighborsUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'add',
				'dataset_name': dataset_name,
				'query_entrez': query_entrez,
				'neighbor_entrez_list': neighbor_entrez.toString(),
				'network_type': network_type
			},
			withCredentials: true
		})
			.map(res => <ServerResponseData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

	}

	addNetworkNeighbors_large_payload (
		analysis_name: string,
		dataset_name: string,
		query_entrez: string,
		neighbor_entrez: string[],
		network_type: string
	): Observable<ServerResponseData> {

		let formData: FormData = new FormData(); 
		formData.append('analysis_name', analysis_name);
		formData.append('action', 'add');
		formData.append('dataset_name', dataset_name);
		formData.append('query_entrez', query_entrez);
		formData.append('neighbor_entrez_list', neighbor_entrez.toString());
		formData.append('network_type', network_type);

		return this.httpClient.post(this.networkNeighborsUrl, formData, { withCredentials:true })
			.map(res => <ServerResponseData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

	}

	removeNetworkNeighbors (
		analysis_name: string,
		id: string
	): Observable<ServerResponseData> {

		return this.httpClient.get(this.networkNeighborsUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'remove',
				'id': id
			},
			withCredentials: true
		})
			.map(res => <ServerResponseData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

	}

}
