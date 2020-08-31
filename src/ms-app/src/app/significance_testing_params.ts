export class SignificanceTestingParams {

    phenotype: string;
    dataset: string;
    significance_level: number;
    testtype: string;
    apply_fdr: boolean;
    fdr_threshold: number;

    constructor() {
        this.dataset = '';
        this.phenotype = '';
        this.testtype = '';
        this.significance_level = 0.05;
        this.apply_fdr = false;
        this.fdr_threshold = 1;
    }

}