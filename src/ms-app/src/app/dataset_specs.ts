export class DatasetSpecs {
    unique_name: string;
    display_name: string;
    analysis_name: string;
    filename: string;
    delimiter: string;
    omics_type: string;
    expanded_filename: string;
    filename_within_analysis_folder: string;
    metadata_columns: string[];
    has_linker: boolean;
    linker_colname: string;
    linker_identifier_type: string;
    has_additional_identifiers: boolean;
    identifier_metadata_columns: string[];
}