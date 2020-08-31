import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SignificanceTestingParams } from './significance_testing_params';
import { GlobalMapConfig } from './global-map-config_data';
import { ServerResponseData } from './server_response';
import { LocalSettings } from './local-settings'
import { Observable } from 'rxjs/Observable';

@Injectable({
  providedIn: 'root'
})

export class SignificanceTestingService {

	//This class is to be deleted

	/*
  private sigTestUrl = LocalSettings.MSVIZ_ENGINE_URL + "/SignificanceTestingService";

  constructor(private http: Http, private httpClient: HttpClient) { }

  getDatasetsAndPhenotypes(analysis_name: string): Observable<string[][]> {
    return this.httpClient.get(this.sigTestUrl, {
			params: {
				'analysis_name': analysis_name,
				'action': 'get_dataset_names_and_phenotypes'
			},
			withCredentials: true
		})
			.map(res => <string[][]>res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

  getGeneMask (
    analysis_name: string, 
    config: GlobalMapConfig
  ): Observable<string[]> {

		return this.httpClient.get(this.sigTestUrl, {
		params: {
			'analysis_name': analysis_name,
			'action': 'get_significance_testing_mask',
			'isGeneFilteringOn': config.isGeneFilteringOn.toString(),
			'sigtest_dataset': config.significance_testing_params.dataset,
			'sigtest_phenotype': config.significance_testing_params.phenotype,
			'sigtest_significance_level': config.significance_testing_params.significance_level.toString()
		},
		withCredentials: true
		})
		.map(res => <string[]>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
	 }
	 */

}
