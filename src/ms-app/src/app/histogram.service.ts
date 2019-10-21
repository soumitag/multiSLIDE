import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { HistogramData } from './histogram_data';
import { LocalSettings } from './local-settings'

import "rxjs/Rx"

@Injectable()
export class HistogramService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetHistogram";  // web api URL
	
	constructor(private http: Http, private httpClient: HttpClient) { }

	getHistogramData(analysis_name, data_type, img_width, img_height): Observable<HistogramData> {
		console.log("getting data...");
		console.log(data_type);
		return this.httpClient.get ( this.baseUrl, {
				params: { 'analysis_name': analysis_name,
						  'data_type': data_type,  
						  'img_width': img_width,
						  'img_height': img_height						  
						}
		  	})
			.map(res =>  {
				console.log(res);
				return <HistogramData> res;
			})
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}	
}
