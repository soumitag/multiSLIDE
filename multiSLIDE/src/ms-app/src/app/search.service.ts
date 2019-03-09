import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { SearchResults } from './search_results';
import { NeighborhoodSearchResults } from './neighborhood_search_results';
import "rxjs/Rx"

@Injectable()
export class SearchService {

  private baseUrl = "http://localhost:8080/msviz-engine/DoSearch";

  private neighborhoodSearchUrl = "http://localhost:8080/msviz-engine/DoNeighborhoodSearch";

  constructor(private http: Http, private httpClient: HttpClient) { }

  doSearch(analysis_name, query): Observable<SearchResults[]> {
		
	return this.httpClient.get ( this.baseUrl, {
			params: { 
				'analysis_name': analysis_name,
				'query': query
			},
			withCredentials: true
		  })
		.map(res => <SearchResults[]> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
}

  doAdvancedSearch(analysis_name, searchText, queryType, searchType): Observable<SearchResults[]> {
		
		return this.httpClient.get ( this.baseUrl, {
				params: { 
					'analysis_name': analysis_name,
					'searchText': searchText,
					'queryType': queryType,
					'searchType': searchType,
				},
				withCredentials: true
		  	})
			.map(res => <SearchResults[]> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

  doNeighborhoodSearch(analysis_name, searchText, queryType, searchType): Observable<NeighborhoodSearchResults> {
		
		return this.httpClient.get ( this.neighborhoodSearchUrl, {
				params: { 
					'analysis_name': analysis_name,
					'searchText': searchText,
					'queryType': queryType,
					'searchType': searchType,
				},
				withCredentials: true
		  	})
			.map(res => <NeighborhoodSearchResults> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

}
