import { Observable } from 'rxjs/Observable';
import { FormControl, FormGroup, Validators, NgForm, FormBuilder } from '@angular/forms';
import { of } from 'rxjs/observable/of';
import { Injectable } from '@angular/core';
import { Http } from "@angular/http";
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { ServerResponseData } from './server_response';
import "rxjs/Rx"
import { FileuploadPayload } from './fileupload_payload'
import {PreviewData} from './preview_data'
import { LocalSettings } from './local-settings'


@Injectable()
export class DatauploadService {

  private baseUrl = LocalSettings.MSVIZ_ENGINE_URL + "/DataUploader";  // web api URL
  private createUrl = LocalSettings.MSVIZ_ENGINE_URL + "/CreateAnalysis";  // web api URL
  private removeUrl = LocalSettings.MSVIZ_ENGINE_URL + "/DataRemover";
  private previewUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GetPreview";
  private metadataInfoUrl = LocalSettings.MSVIZ_ENGINE_URL + "/SetMetadataColumns";
  private listUploadUrl = LocalSettings.MSVIZ_ENGINE_URL + "/ListUploader";  // web api URL
  private loadAnalysisUrl = LocalSettings.MSVIZ_ENGINE_URL + "/LoadAnalysis";  // web api URL
  
  private current_url: string = '';

  constructor(private httpClient: HttpClient) { }

  postMultipartData(fileItem: File, extraData?: FileuploadPayload): Observable<ServerResponseData> {

    const formData: FormData = new FormData();
    formData.append('fileItem', fileItem, fileItem.name);

    if (extraData) {
      for (let key in extraData) {
        // iterate and set other form data
        formData.append(key, extraData[key])
        console.log(key, extraData[key])
      }
    }

    console.log("processing request...")
    return this.httpClient.post(this.baseUrl, formData, {withCredentials:true})
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  postMultipartListData(fileItem: File, extraData?: FileuploadPayload): Observable<ServerResponseData> {

    const formData: FormData = new FormData();
    formData.append('fileItem', fileItem, fileItem.name);

    if (extraData) {
      for (let key in extraData) {
        // iterate and set other form data
        formData.append(key, extraData[key])
        console.log(key, extraData[key])
      }
    }

    console.log("processing request...")
    return this.httpClient.post(this.listUploadUrl, formData, {withCredentials:true})
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  postMultipartAnalysisData(fileItem: File, extraData?: FileuploadPayload): Observable<ServerResponseData> {

    const formData: FormData = new FormData();
    formData.append('fileItem', fileItem, fileItem.name);

    if (extraData) {
      for (let key in extraData) {
        // iterate and set other form data
        formData.append(key, extraData[key])
        console.log(key, extraData[key])
      }
    }

    console.log("processing request...")
    return this.httpClient.post(this.loadAnalysisUrl, formData, {withCredentials:true})
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }


  removeDataset(datasetToRemove: FileuploadPayload): Observable<ServerResponseData> {

    let httpParams = new HttpParams()

    for (let key in datasetToRemove) {
      // iterate and set other form data
      httpParams = httpParams.append(key, datasetToRemove[key])
      //console.log(key, datasetToRemove[key])
    }

    console.log(httpParams.keys());
    for (let key in httpParams) {
      console.log(httpParams[key])
    }

    let httpHeaders = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    console.log(httpHeaders.keys());

    console.log("processing remove request...")
    return this.httpClient.get(this.removeUrl, {
      headers: httpHeaders,
      params: httpParams,
      responseType: 'json',
      withCredentials: true
    })
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  previewDataset(datasetToPreview: FileuploadPayload): Observable<PreviewData> {

    let httpParams = new HttpParams()

    for (let key in datasetToPreview) {
      // iterate and set other form data
      httpParams = httpParams.append(key, datasetToPreview[key])
      //console.log(key, datasetToRemove[key])
    }

    console.log(httpParams.keys());
    for (let key in httpParams) {
      console.log(httpParams[key])
    }

    let httpHeaders = new HttpHeaders({
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    console.log(httpHeaders.keys());

    console.log("processing preview request...")
    return this.httpClient.get(this.previewUrl, {
      headers: httpHeaders,
      params: httpParams,
      responseType: 'json',
      withCredentials: true
    })
      .map(res => <PreviewData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  createAnalysis(analysis_name: string, species: string): Observable<ServerResponseData> {

    return this.httpClient.get(this.createUrl, {
      params: {
        'analysis_name': analysis_name,
        'species': species,					
      },
      withCredentials: true
      })
        .map(res => <ServerResponseData>res)
        .catch(error => {
          console.log(error);
          return Observable.throw(error);
        });
  }

  updateMetaDataInfo(
    analysis_name: string, 
    expanded_filename: string,
    metadata_columns: string[],
    metadata_columns_with_identifier_mappings: string[],
    identifiers: string[]
  ): Observable<ServerResponseData> {

    return this.httpClient.get(this.metadataInfoUrl, {
      params: {
        'action': 'set_metadata',
        'analysis_name': analysis_name,
        'expanded_filename': expanded_filename,	
        'metadata_columns': metadata_columns.toString(),	
        'metadata_columns_with_identifier_mappings': metadata_columns_with_identifier_mappings.toString(),	
        'identifiers': identifiers.toString()
      },
      withCredentials: true
      })
        .map(res => <ServerResponseData>res)
        .catch(error => {
          console.log(error);
          return Observable.throw(error);
        });
  }

  getColumnNames (
    analysis_name: string, 
    expanded_filename: string,
  ): Observable<PreviewData> {

    return this.httpClient.get(this.metadataInfoUrl, {
      params: {
        'action': 'get_column_headers',
        'analysis_name': analysis_name,
        'expanded_filename': expanded_filename,
      },
      withCredentials: true
      })
        .map(res => <PreviewData>res)
        .catch(error => {
          console.log(error);
          return Observable.throw(error);
        });
  }

}
