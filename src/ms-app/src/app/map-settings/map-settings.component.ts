import { Component, OnInit, Inject } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import { HeatmapService } from '../heatmap.service'
import { MapConfig } from '../map-config_data'

@Component({
  selector: 'app-map-settings',
  templateUrl: './map-settings.component.html',
  styleUrls: ['./map-settings.component.css']
})
export class MapSettingsComponent implements OnInit {

  custom_gene_identifiers: string[];
  data_min: number = 0;
  data_max: number = 0;

  analysis_name: string;
  datasetName: string;

  new_map_config: MapConfig;
  numColorsStr: string;
  binningRangeStartStr: string;
  binningRangeEndStr: string;

  numColorsErrMsg: string = "";
  rangeErrMsg: string = "";
  numColorsErr: boolean = false;
  rangeErr: boolean = false;

  diverging_colormaps: string[] = ['BBKY', 'BLWR', 'BYR', 'GBR', 
                'SEISMIC','COOLWARM','BRBG','PRGN',
               'PIYG','RDYLGN','RDBU','RDYLBU','SPECTRAL'];
  uniform_colormaps: string[] = ['VIRIDIS','CIVIDIS','INFERNO','MAGMA','PLASMA'];

  standard_gene_identifiers: string[] = ['entrez','genesymbol','refseq',
                            'ensembl_gene_id','ensembl_transcript_id',
                            'ensembl_protein_id','uniprot_id']

  standard_gene_identifier_display_names: string[] = ['Entrez','Gene Symbol',
                            'RefSeq Gene',
                            'Ensembl Gene Id','Ensembl Transcript Id',
                            'Ensembl Protein Id','Uniprot Id']

  constructor( private heatmapService: HeatmapService, 
               private dialogRef: MatDialogRef<MapSettingsComponent>,
               @Inject(MAT_DIALOG_DATA) data) {

        this.analysis_name = data.analysis_name;
        this.datasetName = data.datasetName;
        this.custom_gene_identifiers = data.custom_gene_identifiers;
        this.dialogRef.updatePosition({ top: '90px', left: '700px' });
        this.dialogRef.updateSize('560px','820px');
  }

  ngOnInit() { 
    this.getMapConfig();
  }

  close() {
    this.dialogRef.close(this.new_map_config);
  }

  saveChanges() {

    if (this.numColorsStr == '' || this.numColorsStr == null) {
      this.numColorsErr = true;
      this.numColorsErrMsg = "Number of Colors is required";
      return;
    } else {
      if (isNaN(Number(this.numColorsStr))) {
        this.numColorsErr = true;
        this.numColorsErrMsg = "Number of Colors must be an integer";
        return;
      } else {
        let nC = Number(this.numColorsStr)
        if (nC < 3 || nC > 255) {
          this.numColorsErr = true;
          this.numColorsErrMsg = "Number of Colors must be between 3 and 255";
          return;
        } else {
          this.new_map_config.nColors = nC;
        }
      }
    }

    if (this.new_map_config.binningRange == 'user_specified') {
      if (this.binningRangeStartStr == '' || this.binningRangeStartStr == null) {
        this.rangeErr = true;
        this.rangeErrMsg = "Start and End values are required for 'Use Range'";
        return;
      } else {
        if (isNaN(Number(this.binningRangeStartStr))) {
          this.rangeErr = true;
          this.rangeErrMsg = "Start and End values must be numeric";
          return;
        } else {
          this.new_map_config.binningRangeStart = Number(this.binningRangeStartStr);
        }
      }

      if (this.binningRangeEndStr == '' || this.binningRangeEndStr == null) {
        this.rangeErr = true;
        this.rangeErrMsg = "Start and End values are required for 'Use Range'";
        return;
      } else {
        if (isNaN(Number(this.binningRangeEndStr))) {
          this.rangeErr = true;
          this.rangeErrMsg = "Start and End values must be numeric";
          return;
        } else {
          this.new_map_config.binningRangeEnd = Number(this.binningRangeEndStr);
        }
      }
    }

    this.new_map_config.columnLabel = 'genesymbol_2021158607524066';
    this.close();
  }

  getMapConfig(): void {
    this.heatmapService.getMapConfig(this.analysis_name, 
                                     this.datasetName)
      .subscribe(data => this.processMapConfigFromServer(data), 
                () => console.log("observable complete"));
  }

  processMapConfigFromServer(config: MapConfig) {
    if (config == null) {
      alert("Failed to get current Heatmap Settings from server. Falling back on default values.");
      this.new_map_config = new MapConfig();
    } else {
      this.new_map_config = config;
    }
    this.numColorsStr = this.new_map_config.nColors.toString();
    this.binningRangeStartStr = this.new_map_config.binningRangeStart.toString();
    this.binningRangeEndStr = this.new_map_config.binningRangeEnd.toString();
    this.custom_gene_identifiers = this.new_map_config.custom_gene_identifiers;
    this.data_min = this.new_map_config.data_min;
    this.data_max = this.new_map_config.data_max;
  }

}
