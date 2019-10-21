export class SignificanceTestingParams {

    phenotype: string;
    dataset: string;
    significance_level: number;

    constructor() {
        this.dataset = '';
        this.phenotype = '';
        this.significance_level = 0.05;
    }

}