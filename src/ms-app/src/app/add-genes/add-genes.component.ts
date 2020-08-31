import { Component, OnInit, Inject} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { SelectionPanelState } from '../selection-state_data';
import { AssortedService } from '../assorted.service';
import { SelectionPanelData } from '../selection-panel_data';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-add-genes',
  templateUrl: './add-genes.component.html',
  styleUrls: ['./add-genes.component.css']
})
export class AddGenesComponent implements OnInit {
  analysis_name: string;
  data: SelectionPanelData;
  state: SelectionPanelState;
  tabs = ['Search', 'Enrichment Analysis', 'Upload'];
  selected = new FormControl(0);

  constructor(
    private assortedService: AssortedService,
    private dialogRef: MatDialogRef<AddGenesComponent>,
    @Inject(MAT_DIALOG_DATA) data) {
    this.analysis_name = data.analysis_name;
    this.dialogRef.updatePosition({ top: '80px', left: '300px' });
    this.dialogRef.updateSize('550px', '700px');
  }

  ngOnInit() {
    this.getSelectionPanelData();
    this.getSelectionPanelState();
  }

  getSelectionPanelData(): void {
		this.assortedService.getSelectionPanelData(this.analysis_name)
      .subscribe(
        data => this.data = data, 
        () => console.log("observable complete"));
  }

  getSelectionPanelState(): void {
		this.assortedService.getSelectionPanelState(this.analysis_name)
      .subscribe(
        data => this.state = data, 
        () => console.log("observable complete"));
  }

  onChangesApplied(): void {
    this.dialogRef.close(1);
  }

  close() {
    this.dialogRef.close(0);
  }
}
