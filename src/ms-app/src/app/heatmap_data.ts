export class HeatmapData {
    title: string;
    description: string
    nSamples: number;
    nEntrez: number;
    bin_colors: number[][];
    cell_bin_indices: number[][];
    /*
  column_headers: string[];
  row_names: string[];
  gene_tags: number[];
  gene_tag_colors: number[][];
  phenotypes: string[][][];
  phenotype_labels: string[];
  gene_group_keys: string[];
  entrez: string[];
  */
    hist_x_values: string[];
    hist_frequencies: number[];
  /*
  search_tag_colors: number[][];
  search_tag_stroke_colors: number[][];
  search_tag_color_indices: number[];
  */
    search_tag_origin_map_ind: boolean[];
  /*
  search_tag_ids: string[];
  search_tag_positions: number[][];
  is_search_query: number[][];
  */
  entrez: string[];
  column_headers: string[];
  gene_tags: number[][];
  search_tag_positions: number[][];
  //is_search_query: number[][];
}