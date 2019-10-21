export class MapConfig {
    custom_gene_identifiers: string[];
    data_min: number;
    data_max: number;
    columnLabel: string;
    nColors: number;
    colorScheme: string;
    binningRange: string;
    binningRangeStart: number;
    binningRangeEnd: number;

    constructor() {
        this.custom_gene_identifiers = [];
        this.data_min = 0;
        this.data_max = 0;
        this.columnLabel = "refseq_2021158607524066";
        this.nColors = 51;
        this.colorScheme = "COOLWARM";
        this.binningRange = "user_specified";
        this.binningRangeStart = 0;
        this.binningRangeEnd = 0;
    }
}