import { Observable } from 'rxjs/Observable';
import { of } from 'rxjs/observable/of';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import "rxjs/Rx"
import { ServerResponseData } from './server_response';
import { LocalSettings } from './local-settings'


@Injectable()
export class CreateSessionService {

	private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/CreateSession";  // web api URL
	
	constructor(private http: Http, private httpClient: HttpClient) { }

	createSessionData(analysis_name): Observable<ServerResponseData> {
		console.log("getting response...");
		
		return this.httpClient.get ( this.baseUrl, {
				params: { 'analysis_name': analysis_name },
				withCredentials:true
		  	})
			.map(res =>  {
				console.log(res);
				return <ServerResponseData> res;
			})
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
	}	
}
