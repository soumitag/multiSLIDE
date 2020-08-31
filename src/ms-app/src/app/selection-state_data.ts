import { SearchResultSummary } from "./search_result_summary";
import { EnrichmentAnalysisParams } from './enrichment_analysis_params';
import { SearchResults } from "./search_results";
import { EnrichmentAnalysisResults} from "./enrichment_analysis_results";
import { FunctionalGroupContainer } from './functional_group_container';

export class SelectionPanelState {
  add_genes_source_type: number;
  selected_phenotypes: string[];
  selected_datasets: string[];
  selected_searches: SearchResultSummary[];
  selected_enriched_groups: EnrichmentAnalysisResults[];
  selected_functional_groups: FunctionalGroupContainer[];
  current_search_results: SearchResults[];
  enrichment_analysis_params: EnrichmentAnalysisParams;
  uploaded_filename: string;
  functional_groups_in_file: FunctionalGroupContainer[];
}