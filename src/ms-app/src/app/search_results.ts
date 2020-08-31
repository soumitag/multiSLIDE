import { SearchResultSummary } from "./search_result_summary";

export class SearchResults {
    query: string;
    dataset_names: string[];
    search_result_summaries: SearchResultSummary[];
}