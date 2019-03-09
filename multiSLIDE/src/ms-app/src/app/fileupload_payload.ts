export class FileuploadPayload {
    data_action : string;
    analysis_name: string;
    delimiter: string;
    upload_type: string;
    identifier_type: string;
    filename: string;

    constructor(data_action : string,
        analysis_name: string,
        delimiter: string,
        upload_type: string,
        identifier_type: string,
        filename: string) {
            this.data_action = data_action;
            this.analysis_name = analysis_name;
            this.delimiter = delimiter;
            this.upload_type = upload_type;
            this.identifier_type = identifier_type;
            this.filename = filename;
    }
}