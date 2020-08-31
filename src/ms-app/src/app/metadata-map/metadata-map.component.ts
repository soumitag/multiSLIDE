import { Component, OnInit, Inject, Input, Output, EventEmitter } from '@angular/core';
import { PreviewData } from '../preview_data'
import { MAT_DIALOG_DATA, MatDialogRef, MatDialog } from "@angular/material/dialog";
import { MatRadioChange } from "@angular/material/radio";
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription'
import { ServerResponseData } from '../server_response'
import { HelpDialogComponent } from '../help-dialog/help-dialog.component';
import { MetadataMapParams } from '../metadata_map_params';
import { DatasetSpecs } from '../dataset_specs';

@Component({
  selector: 'app-metadata-map',
  templateUrl: './metadata-map.component.html',
  styleUrls: ['./metadata-map.component.css']
})
export class MetadataMapComponent implements OnInit {

  column_names: string[];
  analysis_name: string;
  dataset_spec: DatasetSpecs;
  /*
  expanded_filename: string;
  metadata_map_params: MetadataMapParams;
  */

 /* identifierCols = [
    { value: 'entrez_2021158607524066', text: 'Entrez' },
    { value: 'genesymbol_2021158607524066', text: 'Gene Symbol' },
    { value: 'refseq_2021158607524066', text: 'RefSeq ID' },
    { value: 'ensembl_gene_id_2021158607524066', text: 'Ensembl gene ID' },
    { value: 'ensembl_transcript_id_2021158607524066', text: 'Ensembl transcript ID' },
    { value: 'ensembl_protein_id_2021158607524066', text: 'Ensembl protein ID' },
    { value: 'uniprot_id_2021158607524066', text: 'UniProt ID' },
    { value: 'mirna_id_2021158607524066', text: 'miRNA ID' },
    { value: 'other_id_2021158607524066', text: 'Others' }
  ];*/

  identifierCols = new Map([['entrez_2021158607524066', 'Entrez'],
                            ['genesymbol_2021158607524066', 'Gene Symbol'],
                            ['refseq_2021158607524066', 'RefSeq ID'],
                            ['ensembl_gene_id_2021158607524066', 'Ensembl gene ID'],
                            ['uniprot_id_2021158607524066', 'UniProt ID'],
                            ['mirna_id_2021158607524066', 'miRNA ID'],
                            ['other_id_2021158607524066', 'Others']]);

  linker_col_present: string;
  linker_var_present: string[] = ['Yes', 'No'];
  has_additional_identifiers_local: string;

  selectedMetaDataCols: string = '';
  selected_meta_col_std: string = '';
  selected_identifier_value: string;
  selected_second_meta_col: string = '';

  remaining_cols_second_col: string[] = [];

  private uploadSubscription: Subscription;
  server_response: ServerResponseData;
  preview: PreviewData;
  return_val: string[] = [];

  //identifier_mapping_new: string[] = [];
  //second_metadata_mapping_new: string[] = [];

  constructor(private uploadService: DatauploadService,
    private dialogRef: MatDialogRef<MetadataMapComponent>,
    @Inject(MAT_DIALOG_DATA) data,
    public dialogHelp: MatDialog) {

    this.dialogRef.updatePosition({ top: '20px', left: '700px' });
    this.dialogRef.updateSize('600px', '1150px');
    this.analysis_name = data.analysis_name;
    this.dataset_spec = data.spec;
    /*
    this.expanded_filename = data.expanded_filename;
    this.metadata_map_params = new MetadataMapParams();
    */
  }

  ngOnInit() { 
    this.getColumnNames();
    var ds = this.dataset_spec;

    if (ds.has_linker) {
      this.linker_col_present = 'Yes';
    } else {
      this.linker_col_present = 'No';
    }
    if (ds.has_additional_identifiers) {
      this.has_additional_identifiers_local = 'Yes';
    } else {
      this.has_additional_identifiers_local = 'No';
    }
    for (var i=0; i<ds.metadata_columns.length; i++) {
      console.log(ds.has_linker);
      console.log(ds.metadata_columns[i]);
      console.log(ds.linker_colname);
      console.log(ds.identifier_metadata_columns);
      this.remaining_cols_second_col = [];
      if (!((ds.has_linker && ds.metadata_columns[i] == ds.linker_colname) || (ds.identifier_metadata_columns.includes(ds.metadata_columns[i])))) {
        this.remaining_cols_second_col.push(ds.metadata_columns[i]);
      }
    }
  }

  getColumnNames() {
    this.uploadSubscription = this.uploadService.getColumnNames(
      this.analysis_name,
      this.dataset_spec.expanded_filename
    ).subscribe(
      data => this.preview = data,
      () => console.log("error getting headers"),
      () => this.handleServerResponseOnFetch()
    );
  }

  handleServerResponseOnFetch() {
    this.column_names = this.preview.data[0];
  }

  addMetaDataCols() {
    let i = this.dataset_spec.metadata_columns.indexOf(this.selectedMetaDataCols)
    if (i<0) {
      this.dataset_spec.metadata_columns.push(this.selectedMetaDataCols);
      this.remaining_cols_second_col.push(this.selectedMetaDataCols);
    }
    console.log(this.selectedMetaDataCols);
    console.log(this.remaining_cols_second_col);
  }

  updateHasLinker() {
    this.dataset_spec.has_linker = this.linker_col_present=='Yes';
  }

  updateHasAdditionalIdentifiers() {
    this.dataset_spec.has_additional_identifiers = this.has_additional_identifiers_local=='Yes';
  }

  removeMetaDataCols(cols) {
    let i = this.dataset_spec.metadata_columns.indexOf(cols);
    if (i >= 0) {
      this.dataset_spec.metadata_columns.splice(i,1);
      this.remaining_cols_second_col.splice(i,1);
      if (this.dataset_spec.linker_colname == cols) {
        this.clearMetaDataIdentifierCols()
      }
      let j = this.dataset_spec.identifier_metadata_columns.indexOf(cols);
      if (j >= 0) {
        this.dataset_spec.identifier_metadata_columns.splice(j, 1);
      }

      j = this.remaining_cols_second_col.indexOf(cols);
      if (j >= 0) {
        this.remaining_cols_second_col.splice(j, 1);
      }
    }
  }

  addLinkerCol() {
    this.dataset_spec.linker_colname = this.selected_meta_col_std;
    this.dataset_spec.linker_identifier_type = this.selected_identifier_value;
    this.dataset_spec.has_linker = true;

    let j = this.remaining_cols_second_col.indexOf(this.dataset_spec.linker_colname);
    if(j >= 0) {
      this.remaining_cols_second_col.splice(j, 1);
    }
  }

  removeLinkerCol() {
    this.dataset_spec.has_linker = false;
    this.remaining_cols_second_col.push(this.dataset_spec.linker_colname);
    this.dataset_spec.linker_colname = '';
  }

  clearMetaDataIdentifierCols() {
    this.dataset_spec.has_linker = false;
  }

  addSecondMetaDataCols() {
    //console.log(this.selected_second_meta_col);
    let i = this.dataset_spec.identifier_metadata_columns.indexOf(this.selected_second_meta_col);
    if(i < 0) {
      this.dataset_spec.identifier_metadata_columns.push(this.selected_second_meta_col);
    }
    if (this.dataset_spec.identifier_metadata_columns.length > 0) {
      this.dataset_spec.has_additional_identifiers = true;
    }
  }

  removeSecondMetaDataCols(cols) {
    let i = this.dataset_spec.identifier_metadata_columns.indexOf(cols);
    if(i >= 0) {
      this.dataset_spec.identifier_metadata_columns.splice(i, 1);
    }
    if (this.dataset_spec.identifier_metadata_columns.length == 0) {
      this.dataset_spec.has_additional_identifiers = false;
    }
  }

  saveChanges() {
    let is_ready = true;
    if (this.dataset_spec.has_linker && (this.dataset_spec.linker_colname == '' || this.dataset_spec.linker_colname == null)) {
      alert("Please select the linker column and it's identifier type");
      is_ready = false;
    }
    if (this.dataset_spec.has_additional_identifiers && this.dataset_spec.identifier_metadata_columns.length == 0) {
      alert("Please select the molecular-level specific identifiers");
      is_ready = false;
    }
    if (is_ready) {
      this.dialogRef.close(this.dataset_spec);
    }
  }

  close() {
    this.dialogRef.close();
  }
  
  openHelpDialog(): void {
    const dialogHelpRef = this.dialogHelp.open(HelpDialogComponent, {
      width: '550px', height: '400px'
    });

    dialogHelpRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });

  }

}
