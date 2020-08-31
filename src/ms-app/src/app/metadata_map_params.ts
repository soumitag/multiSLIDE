export class MetadataMapParams {

    selected_metadatacols: string[];
    linker_col_present: string;
    map_metadatacol_std_id: string[];
    omics_col_present: string;
    selected_other_id: string;

    constructor() {
        this.selected_metadatacols = [];
        this.linker_col_present = 'Yes';
        this.map_metadatacol_std_id = [];
        this.omics_col_present = 'Yes';
        this.selected_other_id = '';
    }
}