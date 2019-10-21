export class PhenotypeSortingParams {
    phenotypes: string[];
    sort_orders: boolean[];

    constructor(phenotypes: string[], sort_orders: boolean[]) {
        this.phenotypes = phenotypes;
        this.sort_orders = sort_orders;
    }

    setPhenotypes(phenotypes: string[]) {
        this.phenotypes = phenotypes;
    }

    setSortOrders(sort_orders: boolean[]) {
        this.sort_orders = sort_orders;
    }
}