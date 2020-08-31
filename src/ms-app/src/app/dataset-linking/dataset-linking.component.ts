import { Component, OnInit, Input, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-dataset-linking',
  templateUrl: './dataset-linking.component.html',
  styleUrls: ['./dataset-linking.component.css']
})
export class DatasetLinkingComponent implements OnInit {

  analysis_name: string;
  /*is_dataset_linked = {}; */

  is_dataset_linked = new Map();
   
  constructor(private datasetLinkingDialogRef: MatDialogRef<DatasetLinkingComponent>,
    @Inject(MAT_DIALOG_DATA) data) { 

      this.analysis_name = data.analysis_name;
      this.is_dataset_linked = data.is_dataset_linked;
      this.datasetLinkingDialogRef.updatePosition({ top: '110px', left: '700px' });
      this.datasetLinkingDialogRef.updateSize('450px', '500px');
    }

  ngOnInit() {
  }

  onSlide(key: string, val: boolean) {
    this.is_dataset_linked[key] = val;
  }

  applyChanges() {
    this.datasetLinkingDialogRef.close(1);
  }

  close() {
    this.datasetLinkingDialogRef.close(0);
  }

}
