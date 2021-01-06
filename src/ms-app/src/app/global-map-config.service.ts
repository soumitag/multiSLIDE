import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GlobalMapConfig } from './global-map-config_data';
import { ServerResponseData } from './server_response';
import { LocalSettings } from './local-settings'
import { Observable } from 'rxjs/Observable';
import { SignificanceTestingParams } from './significance_testing_params';
import { ClusteringParams } from './clustering_params';
import { config } from 'rxjs';
import { MappedData } from './mapped_data';
import { PhenotypeSortingParams } from './phenotype_sorting_params'

@Injectable({
  providedIn: 'root'
})

export class GlobalMapConfigService {

  private globalMapConfigUrl = LocalSettings.MSVIZ_ENGINE_URL + "/GlobalMapConfigServices";

  constructor(private httpClient: HttpClient) { }

  /*
  setMapResolution (
    analysis_name: string, config: GlobalMapConfig
  ): Observable<ServerResponseData> {
		return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_map_resolution',
        'mapResolution': config.mapResolution
      },
		withCredentials: true
		})
		.map(res => <ServerResponseData>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
  }
  */

  getGlobalMapConfig(analysis_name: string): Observable<GlobalMapConfig> {

    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'get_global_map_config',
      },
      withCredentials: true
    })
      .map(res => <GlobalMapConfig>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  /*
  setGlobalMapConfig(analysis_name: string, config: GlobalMapConfig): Observable<GlobalMapConfig> {

    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_global_map_config',
        'nSamples': config.userSpecifiedRowsPerPage.toString(),
        'nEntrez': config.userSpecifiedColsPerPage.toString(),
        'colHeaderHeight': config.colHeaderHeight.toString(),
        'rowLabelWidth': config.rowLabelWidth.toString(),
        'mapResolution': config.mapResolution,
        'gridLayout': config.gridLayout.toString()
      },
      withCredentials: true
    })
      .map(res => <GlobalMapConfig>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }
  */

  setGlobalMapConfigParam (
     analysis_name: string, 
     action: string,
     param_value: string
  ): Observable<ServerResponseData> {

    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': action,
        'param_value': param_value
      },
		withCredentials: true
		})
		.map(res => <ServerResponseData>res)
		.catch(error => {
			console.log(error);
			return Observable.throw(error);
		});
  }

  updateView (
    analysis_name: string, 
    axis: string,
    start: number,
    scrollBy: number
  ): Observable<MappedData> {

    var action = '';
    if (axis == 'sample') {
      action = 'set_current_sample_start'
    } else if (axis == 'feature') {
      action = 'set_current_feature_start'
    }

    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': action,
        'param_value': start.toString(),
        'scrollBy': scrollBy.toString()
      },
    withCredentials: true
    })
    .map(res => <MappedData>res)
    .catch(error => {
      console.log(error);
      return Observable.throw(error);
    });
  }

  updateGlobalMapConfigParam (
    analysis_name: string, 
    action: string,
    param_value: string
  ): Observable<ServerResponseData> {

   return this.httpClient.get(this.globalMapConfigUrl, {
     params: {
       'analysis_name': analysis_name,
       'action': action,
       'param_value': param_value
     },
   withCredentials: true
   })
   .map(res => <ServerResponseData>res)
   .catch(error => {
     console.log(error);
     return Observable.throw(error);
   });
  }
   
  updateGeneFilteringParams (
    analysis_name: string, 
    params: SignificanceTestingParams
  ): Observable<ServerResponseData> {
    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_significance_testing_params',
        'dataset': params.dataset,
        'phenotype': params.phenotype,
        'significance_level': params.significance_level.toString(),
        'testtype': params.testtype,
        'apply_fdr': params.apply_fdr.toString(),
        'fdr_threshold': params.fdr_threshold.toString()
      },
      withCredentials: true
    })
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  setClusteringParams(
    analysis_name: string,
    params: ClusteringParams
  ): Observable<ServerResponseData> {
    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_clustering_params',
        'type': params.type.toString(),
        'dataset': params.dataset,
        'use_defaults': params.use_defaults.toString(),
        'linkage_function': params.linkage_function.toString(),
        'distance_function': params.distance_function.toString(),
        'leaf_ordering': params.leaf_ordering.toString(),
        'numClusterLabels': params.numClusterLabels.toString()
      },
      withCredentials: true
    })
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  setPhenotypeSortingParams (
    analysis_name: string, 
    params: PhenotypeSortingParams
  ): Observable<ServerResponseData> {
    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_phenotype_sorting_params', 
        'phenotypes': params.phenotypes.toString(),
        'sort_orders': params.sort_orders.toString()
      },
      withCredentials: true
    })
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  updateSelectedDatasets (
    analysis_name: string, 
    selected_datasets: string[]
  ): Observable<ServerResponseData> {
    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_selected_datasets', 
        'selected_datasets': selected_datasets.toString()
      },
      withCredentials: true
    })
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
  }

  updateSelectedPhenotypes (
    analysis_name: string,
    selected_phenotypes: string[]
  ): Observable<ServerResponseData> {
    return this.httpClient.get(this.globalMapConfigUrl, {
      params: {
        'analysis_name': analysis_name,
        'action': 'set_selected_phenotypes', 
        'selected_phenotypes': selected_phenotypes.toString()
      },
      withCredentials: true
    })
      .map(res => <ServerResponseData>res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
    }

    updateDatasetLinkings (analysis_name: string, dataset_names: string[],
      is_dataset_linked_arr: boolean[]
    ): Observable<ServerResponseData> {
      return this.httpClient.get(this.globalMapConfigUrl, {
        params: {
          'analysis_name': analysis_name,
          'action': 'set_database_linkings', 
          'dataset_names': dataset_names.toString(),
          'is_dataset_linked_arr': is_dataset_linked_arr.toString()
        },
        withCredentials: true
      })
        .map(res => <ServerResponseData>res)
        .catch(error => {
          console.log(error);
          return Observable.throw(error);
        });

    }

    resetDefaults (analysis_name: string) {
      return this.httpClient.get ( this.globalMapConfigUrl, {
        params: { 
          'analysis_name': analysis_name,
          'action': 'reset_config'
        },
        withCredentials: true
      })
      .map(res => <ServerResponseData> res)
      .catch(error => {
        console.log(error);
        return Observable.throw(error);
      });
    }

}
