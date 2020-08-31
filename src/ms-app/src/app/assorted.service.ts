import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SelectionPanelData } from './selection-panel_data';
import { SelectionPanelState } from './selection-state_data';
import { LegendData } from './legend_data';
import "rxjs/Rx"
import { forkJoin } from 'rxjs';
import { ServerResponseData } from './server_response';
import { LocalSettings } from './local-settings'
import { EnrichmentAnalysisParams } from './enrichment_analysis_params'
import { MappedData } from './mapped_data';

@Injectable()
export class AssortedService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetPhenotypes";
	private legendsUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetFigureLegends";
	private networkNeighborsUrl = LocalSettings.MSVIZ_ENGINE_URL + "/NetworkNeighborServices";

	constructor(private httpClient: HttpClient) { }

	getEnrichmentAnalysisFile(analysis_name:string, filename: string) {
		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'filename': filename,
				'action': 'download_enrichment_analysis_results'
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

	getFunctionalGroupsData(analysis_name: string): Observable<MappedData> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'get_functional_grp_names'
			},
			withCredentials: true
		})
			.map(res => <MappedData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	getUserSpecifiedConnections(analysis_name: string): Observable<string[][]> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'get_user_specified_connections'
			},
			withCredentials: true
		})
			.map(res => <string[][]>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	removeUserSpecifiedConnections(analysis_name: string, filename: string): Observable<ServerResponseData> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'action': 'delete_user_specified_connections',
				'analysis_name': analysis_name,
				'filename': filename
			},
			withCredentials: true
		})
			.map(res => <ServerResponseData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

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

	setEnrichmentAnalysisParams (
		analysis_name: string,
		enrichment_analysis_params: EnrichmentAnalysisParams
		): Observable<MappedData> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'set_enrichment_analysis_params',
				'dataset': enrichment_analysis_params.dataset,
				'phenotype': enrichment_analysis_params.phenotype,
				'testtype': enrichment_analysis_params.testtype,
				'use_pathways': enrichment_analysis_params.use_pathways.toString(),
				'use_ontologies': enrichment_analysis_params.use_ontologies.toString(),
				'significance_level_d': enrichment_analysis_params.significance_level_d.toString(),
				'apply_fdr_d': enrichment_analysis_params.apply_fdr_d.toString(),
				'fdr_threshold_d': enrichment_analysis_params.fdr_threshold_d.toString(),
				'significance_level_e': enrichment_analysis_params.significance_level_e.toString(),
				'apply_fdr_e': enrichment_analysis_params.apply_fdr_e.toString(),
				'fdr_threshold_e': enrichment_analysis_params.fdr_threshold_e.toString()
			},
			withCredentials: true
		})
			.map(res => <MappedData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

  }

}
