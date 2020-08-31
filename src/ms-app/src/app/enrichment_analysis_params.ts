export class EnrichmentAnalysisParams {

    phenotype: string;
    dataset: string;
    significance_level_d: number;
    testtype: string;
    use_pathways: boolean;
    use_ontologies: boolean;
    apply_fdr_d: boolean;
    fdr_threshold_d: number;
    significance_level_e: number;
    apply_fdr_e: boolean;
    fdr_threshold_e: number;


    constructor() {
        this.dataset = '';
        this.phenotype = '';
        this.testtype = '';
        this.use_pathways = true;
        this.use_ontologies = false;
        this.significance_level_d = 0.05;
        this.apply_fdr_d = false;
        this.fdr_threshold_d = 1;
        this.significance_level_e = 0.05;
        this.apply_fdr_e = false;
        this.fdr_threshold_e = 1;
    }

}