import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient } from '@angular/common/http';
import { SelectionPanelData } from './selection-panel_data';
import { SelectionPanelState } from './selection-state_data';
import { LegendData } from './legend_data';
import "rxjs/Rx"

@Injectable()
export class AssortedService {

  private baseUrl = "http://localhost:8080/msviz-engine/GetPhenotypes";
  private legendsUrl = "http://localhost:8080/msviz-engine/GetFigureLegends";

  constructor(private http: Http, private httpClient: HttpClient) { }

  getSelectionPanelData(analysis_name: string): Observable<SelectionPanelData> {
		
		return this.httpClient.get ( this.baseUrl, {
				params: { 
					'analysis_name': analysis_name,
					'action': 'panel_data'
				},
				withCredentials: true
		  	})
			.map(res => <SelectionPanelData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }

  getSelectionPanelState(analysis_name: string): Observable<SelectionPanelState> {
		
	return this.httpClient.get ( this.baseUrl, {
			params: { 
				'analysis_name': analysis_name,
				'action': 'panel_state'
			},
			withCredentials: true
		  })
		.map(res => <SelectionPanelState> res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
  }

  getLegendData(analysis_name: string): Observable<LegendData> {
		
		return this.httpClient.get ( this.legendsUrl, {
				params: { 
					'analysis_name': analysis_name
				},
				withCredentials: true
		  	})
			.map(res => <LegendData> res)
			.catch(error => {
				console.log(error);
				return Observable.throw(error);
			});
  }
  
}
