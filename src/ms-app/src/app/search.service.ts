import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from './search_results';
import { NeighborhoodSearchResults } from './neighborhood_search_results';
import "rxjs/Rx"
import { LocalSettings } from './local-settings'

@Injectable()
export class SearchService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/DoSearch";

	private neighborhoodSearchUrl = LocalSettings.MSVIZ_ENGINE_URL + "/DoNeighborhoodSearch";

	constructor(private httpClient: HttpClient) { }

	doSearch(analysis_name, query): Observable<SearchResults[]> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'query': query
			},
			withCredentials: true
		})
			.map(res => <SearchResults[]>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	doAdvancedSearch(analysis_name, searchText, queryType, searchType): Observable<SearchResults[]> {

		return this.httpClient.get(this.baseUrl, {
			params: {
				'analysis_name': analysis_name,
				'searchText': searchText,
				'queryType': queryType,
				'searchType': searchType,
			},
			withCredentials: true
		})
			.map(res => <SearchResults[]>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

	doNeighborhoodSearch (
		analysis_name: string,
		dataset_name: string,
		search_entrez: string,
		search_type: string
	): Observable<NeighborhoodSearchResults> {

		return this.httpClient.get(this.neighborhoodSearchUrl, {
			params: {
				'analysis_name': analysis_name,
				'dataset_name': dataset_name,
				'query': search_type + "=" + search_entrez
			},
			withCredentials: true
		})
			.map(res => <NeighborhoodSearchResults>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}

}
