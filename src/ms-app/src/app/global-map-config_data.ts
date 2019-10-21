import { SignificanceTestingParams } from "./significance_testing_params";
import { ClusteringParams } from "./clustering_params";
import { MappedData } from './mapped_data'
import { PhenotypeSortingParams } from './phenotype_sorting_params'

export class GlobalMapConfig {
    mapOrientation: number;
    mapResolution: string;
    gridLayout: number;
    rowsPerPageDisplayed: number;
    colsPerPageDisplayed: number;
    userSpecifiedRowsPerPage: number;
    userSpecifiedColsPerPage: number;
    colHeaderHeight: number;
    rowLabelWidth: number;
    current_sample_start: number;
    current_feature_start: number;
    available_rows: number;
    available_cols: number;
    scrollBy: number;
    columnOrderingScheme: number;
    sampleOrderingScheme: number;
    isGeneFilteringOn: boolean;
    isSampleFilteringOn: boolean;
    row_clustering_params: ClusteringParams;
    col_clustering_params: ClusteringParams;
    significance_testing_params: SignificanceTestingParams;
    phenotype_sorting_params: PhenotypeSortingParams;

}