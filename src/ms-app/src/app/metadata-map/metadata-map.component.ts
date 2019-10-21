import { Component, OnInit, Inject, Input, Output, EventEmitter} from '@angular/core';
import { PreviewData } from '../preview_data'
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import { DatauploadService } from '../dataupload.service'
import { Subscription } from 'rxjs/Subscription'
import { ServerResponseData } from '../server_response'

@Component({
  selector: 'app-metadata-map',
  templateUrl: './metadata-map.component.html',
  styleUrls: ['./metadata-map.component.css']
})
export class MetadataMapComponent implements OnInit {

  identifierCols = [
    { value: 'entrez_2021158607524066', text: 'Entrez' },
    { value: 'genesymbol_2021158607524066', text: 'Gene Symbol' },
    { value: 'refseq_2021158607524066', text: 'RefSeq ID' },
    { value: 'ensembl_gene_id_2021158607524066', text: 'Ensembl gene ID' },
    { value: 'ensembl_transcript_id_2021158607524066', text: 'Ensembl transcript ID' },
    { value: 'ensembl_protein_id_2021158607524066', text: 'Ensembl protein ID' },
    { value: 'uniprot_id_2021158607524066', text: 'UniProt ID' },
    { value: 'mirna_id_2021158607524066', text: 'miRNA ID' }
  ];

  selectedMetaDataCols: string = '';
  selectedMetaDataCols2: string = '';
  //identifierCols: string = '';
  metaDataCols:string = '';
  allowedSpecies: string;
  column_names: string[];

  selected_metadata_cols_applied: string[] = [];
  selected_metadata_cols_applied2: string[] = [];
  identifier_mapping_applied: string[] = [];

  identifier_col_applied: string = '';
  selected_metadata_cols_new: string[] = [];
  selected_metadata_cols_new2: string[] = [];
  identifier_mapping_new: string[] = [];

  selected_identifier_value: string;
  selected_identifier_text: string;
  identifier_col_new: string = '';

  hasChanged_MetaDataCols: boolean = false;
  hasChanged_MetaDataCols2: boolean = false;
  hasChanged_IdentifierCol: boolean = false;
  hasChanged_IdentifierMapping: boolean = false;

  analysis_name: string;
  expanded_filename: string;
  private uploadSubscription: Subscription;
  server_response: ServerResponseData;
  preview: PreviewData;
  return_val: string[] = [];

  constructor(private uploadService: DatauploadService, 
              private dialogRef: MatDialogRef<MetadataMapComponent>,
              @Inject(MAT_DIALOG_DATA) data) {
    //this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '110px', left: '700px' });
    this.dialogRef.updateSize('600px','750px');
    this.analysis_name = data.analysis_name;
    this.expanded_filename = data.expanded_filename;
  }

  ngOnInit() {
    this.getColumnNames();
  }

  metalog(){
    console.log(this.selectedMetaDataCols);
  }

  metaDataCols_log(){
    console.log(this.selectedMetaDataCols2);
  }

  identifierCols_log(){
    console.log(this.identifierCols)
  }

  addMetaDataCols() {
    this.hasChanged_MetaDataCols = this.addItem(this.selected_metadata_cols_new, 
      this.selected_metadata_cols_applied, this.selectedMetaDataCols);
      console.log(this.hasChanged_MetaDataCols)
  }

  addMetaDataIdentifierCols(){
    /*this.hasChanged_MetaDataCols2 = this.addItem(this.selected_metadata_cols_new2, 
      this.selected_metadata_cols_applied2, this.selectedMetaDataCols2);
      console.log(this.hasChanged_MetaDataCols2)

      this.hasChanged_IdentifierCol = this.addItem(this.identifier_col_new, 
        this.identifier_col_applied, this.identifierCols);
        console.log(this.hasChanged_IdentifierCol)
    */
    
    this.hasChanged_MetaDataCols2 = this.addSingleItem(this.selectedMetaDataCols2)
    this.hasChanged_IdentifierCol = this.addSingleItem(this.identifierCols)
    if(this.hasChanged_MetaDataCols2 && this.hasChanged_IdentifierCol){
      this.hasChanged_IdentifierMapping = this.addMappingItem(this.identifier_mapping_new, 
      this.identifier_mapping_applied, this.selectedMetaDataCols2, this.identifierCols)
    }

  }

  addMappingItem(selected_list_new, selected_list_applied, item_name1, item_name2){
    let i1 = selected_list_new.indexOf(item_name1);
    let i2 = selected_list_new.indexOf(item_name2);
    if (i1 > -1 || item_name1 == "" || i2 > -1 || item_name2 == "") {
      return;
    }
    for (var i=0; i<this.identifierCols.length; i++) {
      if (this.identifierCols[i].value == this.selected_identifier_value) {
        this.selected_identifier_text = this.identifierCols[i].text;
      }
    }
    selected_list_new.push(item_name1 +  '\u00A0 \u2192 \u00A0' + this.selected_identifier_text);
    console.log(selected_list_new);
    if (this.isEqual(selected_list_new, selected_list_applied)) {
      return false;
    } else {
      return true;
    }
  }

  removeMetaDataCols(cols) {
    this.hasChanged_MetaDataCols = this.removeItem(this.selected_metadata_cols_new, 
      this.selected_metadata_cols_applied, cols);
  }

  addSingleItem(item_name){
    let option_val = item_name;
    console.log(option_val) 
    return true;
  }

  addItem(selected_list_new, selected_list_applied, item_name) {
    let i = selected_list_new.indexOf(item_name);
    if (i > -1 || item_name == "") {
      return;
    }
    selected_list_new.push(item_name);
    console.log(selected_list_new);
    if (this.isEqual(selected_list_new, selected_list_applied)) {
      return false;
    } else {
      return true;
    }
  }

  removeItem(selected_list_new, selected_list_applied, item_name) {
    selected_list_new.splice(selected_list_new.indexOf(item_name), 1 );
    console.log(selected_list_new);
    if (this.isEqual(selected_list_new, selected_list_applied)) {
      return false;
    } else {
      return true;
    }
  }

  isEqual(arr1, arr2) {
    if(arr1.length !== arr2.length)
        return false;
    for(var i = arr1.length; i--;) {
        if(arr1[i] !== arr2[i])
            return false;
    }
    return true;
  }

  updateMetaDataInfo () {
    var t: string[][];
    t = [];
    for(var i: number = 0; i < 2; i++) {
      t[i] = [];
      for(var j: number = 0; j < this.identifier_mapping_new.length; j++) {
        t[i][j] = "";
      }
    }
    for (var i=0; i<this.identifier_mapping_new.length; i++) {
      console.log(this.identifier_mapping_new[i]);
      var str = this.identifier_mapping_new[i].replace("\u00A0 \u2192 \u00A0", ":");
      console.log(str);
      var parts = str.split(":");
      console.log(parts);
      t[0][i] = parts[0];
      t[1][i] = parts[1];
    }
    var cols_with_identifiers = t[0];
    var mapped_identifiers = t[1];
    for (var i=0; i<mapped_identifiers.length; i++) {
      for (var j=0; j<this.identifierCols.length; j++) {
        var a = this.identifierCols[j].text;
        if (a == mapped_identifiers[i]) {
          mapped_identifiers[i] = this.identifierCols[j].value;
        }
      }
    }
    console.log(t);
    this.return_val[0] = this.selected_metadata_cols_new.toString();
    this.return_val[1] = this.identifier_mapping_new.toString();
    this.uploadSubscription = this.uploadService.updateMetaDataInfo(
      this.analysis_name, 
      this.expanded_filename,
      this.selected_metadata_cols_new,
      cols_with_identifiers,
      mapped_identifiers
    ).subscribe(
          data => this.server_response = data, 
          () => console.log("create error"), 
          () => this.handleServerResponseOnUpdate()
      );
  }

  getColumnNames () {
    this.uploadSubscription = this.uploadService.getColumnNames(
      this.analysis_name, 
      this.expanded_filename
    ).subscribe(
          data => this.preview = data, 
          () => console.log("error getting headers"),
          () => this.handleServerResponseOnFetch()
      );
  }

  handleServerResponseOnUpdate() {
    if (this.server_response.status == 1) {
      alert(this.server_response.message);
      this.close();
    } else if (this.server_response.status == 0) {
      alert("Metadata update failed. " + this.server_response.message + " " + this.server_response.detailed_reason);
    }
  }

  handleServerResponseOnFetch() {
    this.column_names = this.preview.data[0];
  }

  close() {
    if (this.server_response.status == 1) {
      this.dialogRef.close(this.return_val);
    } else {
      this.dialogRef.close("");
    }
  }
}
