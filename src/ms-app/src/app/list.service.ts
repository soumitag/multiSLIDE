import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ListData } from './list_data';
import { ServerResponseData } from './server_response'
import { LocalSettings } from './local-settings'

@Injectable({
  providedIn: 'root'
})
export class ListService {

  private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/FeatureListServices";  // web api URL

  constructor(private httpClient: HttpClient) { }

  getUserLists (analysis_name): Observable<ListData[]> {
		console.log("loading feature lists...")
		return this.httpClient.get ( this.baseUrl, {
			params: { 
				'analysis_name': analysis_name,
            	'action': 'get_metadata'
			},
			withCredentials: true
		  	})
			.map(res => <ListData[]> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

  createList(analysis_name: string, feature_list_name:string): Observable<ServerResponseData> {
	console.log("create feature list...")
	return this.httpClient.get(this.baseUrl, {
		params: {
			'analysis_name': analysis_name,
			'action': 'create_list',
			'list_name': feature_list_name,								
		},
		withCredentials: true
	})
		.map(res => <ServerResponseData>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
  }

  deleteList(analysis_name: string, feature_list_name:string): Observable<ServerResponseData> {
	console.log("create feature list...")
	return this.httpClient.get(this.baseUrl, {
		params: {
			'analysis_name': analysis_name,
			'action': 'delete_list',
			'list_name': feature_list_name,								
		},
		withCredentials: true
	})
		.map(res => <ServerResponseData>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
  }

  createListAndAdd(analysis_name: string, entrez: string, add_type: string, dataset_name: string): Observable<ServerResponseData> {
	console.log("create and add in feature list...")
	return this.httpClient.get(this.baseUrl, {
		params: {
			'analysis_name': analysis_name,
			'action': 'create_list_and_add',
			'add_type': add_type,
			'list_data': entrez,
			'dataset_name': dataset_name			
		},
		withCredentials: true
	})
		.map(res => <ServerResponseData>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
  }

  addToList (analysis_name: string, entrez: string, feature_list_name: string, add_type: string, dataset_name: string): Observable<ServerResponseData> {
		console.log("loading feature lists...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
						  'action': 'add_features',
						  'list_name': feature_list_name,
						  'add_type': add_type,
						  'list_data': entrez,
						  'dataset_name': dataset_name
						},
				withCredentials: true
		  	})
			.map(res => <ServerResponseData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

  removeFromList (analysis_name: string, entrez: string, features: string, feature_list_name: string, dataset_name: string): Observable<ServerResponseData> {
		console.log("loading feature lists...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  	'analysis_name': analysis_name,
							'action': 'remove_features',
							'list_name': feature_list_name,
							'remove_type': 'single_feature',
							'dataset_name': dataset_name,
							'entrez': entrez,
							'features': features
						},
				withCredentials: true
		  	})
			.map(res => <ServerResponseData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

  serializeList (analysis_name: string, feature_list_name: string): Observable<Blob> {
		console.log("loading feature lists...")
		return this.httpClient.get ( this.baseUrl, {
				params: { 
						  'analysis_name': analysis_name,
              'action': 'save',
              'list_name': feature_list_name,
              'filename': feature_list_name + '.txt',
              'identifier': 'entrez'
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
  
}
