export class ClusteringParams {
    /*
    is_joint: boolean;
    use_aggregate: boolean;
    aggregate_func: string;
    */
    /*
    has_multiple_data_levels: boolean;
    */
    type: number; /* row or column clustering */
    dataset: string;
    use_defaults: boolean;
    linkage_function: number;
    distance_function: number;
    leaf_ordering: number;
    numClusterLabels: number;
  /*
  constructor() {
    this.type = 0;
    this.dataset = '';
    this.use_defaults = true;
    this.linkage_function = 0;
    this.distance_function = 0;
    this.leaf_ordering = 0;
  }
  */
}