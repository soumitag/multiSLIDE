import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { ClusteringParams } from './clustering_params';
import { ServerResponseData } from './server_response';
import { LocalSettings } from './local-settings'
import { Observable } from 'rxjs/Observable';

@Injectable({
  providedIn: 'root'
})
export class ClusteringService {

  private clusteringUrl = LocalSettings.MSVIZ_ENGINE_URL + "/ClusteringService";

  constructor(private http: Http, private httpClient: HttpClient) { }

  setClusteringParams (
		analysis_name: string,
    row_clustering_params: ClusteringParams,
    col_clustering_params: ClusteringParams
	): Observable<ServerResponseData> {

		return this.httpClient.get(this.clusteringUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'set_clustering_params',
        'row_dataset': row_clustering_params.dataset,
        'row_use_defaults': row_clustering_params.use_defaults.toString(),
        'row_linkage_function': row_clustering_params.linkage_function.toString(),
        'row_distance_function': row_clustering_params.distance_function.toString(),
        'row_leaf_ordering': row_clustering_params.leaf_ordering.toString(),
        'col_dataset': col_clustering_params.dataset,
        'col_use_defaults': col_clustering_params.use_defaults.toString(),
        'col_linkage_function': col_clustering_params.linkage_function.toString(),
        'col_distance_function': col_clustering_params.distance_function.toString(),
        'col_leaf_ordering': col_clustering_params.leaf_ordering.toString()
			},
			withCredentials: true
		})
			.map(res => <ServerResponseData>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

  }
  
  getClusteringParams (analysis_name: string, row_or_col: string): Observable<ClusteringParams> {

		return this.httpClient.get(this.clusteringUrl, {
			params: {
        'analysis_name': analysis_name,
        'action': 'get_clustering_params',
				'type': row_or_col
			},
			withCredentials: true
		})
			.map(res => <ClusteringParams>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

  }
  
  getDatasets (analysis_name: string): Observable<string[]> {

		return this.httpClient.get(this.clusteringUrl, {
			params: {
        'analysis_name': analysis_name,
        'action': 'get_dataset_names'
			},
			withCredentials: true
		})
			.map(res => <string[]>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});

	}

}
