export class NeighborhoodSearchResults {
  query_type: string;
  query_string: string;
  neighbor_count: number;
  neighbors_in_dataset: number;
  neighbor_entrez: string[];
  neighbor_display_tag: string[];
  identifier_type: string;
  neighbor_in_dataset_ind: boolean[];
  message: string;
  status: number;
}