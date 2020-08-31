import { Component, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { SelectionPanelData } from '../selection-panel_data';
import { SelectionPanelState } from '../selection-state_data';
import { AssortedService } from '../assorted.service';
import { SearchResultSummary } from '../search_result_summary';
import { ServerResponseData } from '../server_response';
import { AnalysisService } from '../analysis.service'
import { DialogBoxComponent } from '../dialog-box/dialog-box.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material/dialog";
import { AddGenesComponent } from '../add-genes/add-genes.component';

@Component({
  selector: 'app-selection-panel',
  templateUrl: './selection-panel.component.html',
  styleUrls: ['./selection-panel.component.css']
})
export class SelectionPanelComponent implements OnInit {

  @Input() analysis_name: string;
  @Output() notifyChanges = new EventEmitter();

  addGenesDialogRef: MatDialogRef<AddGenesComponent>;

  showingFeatureList: boolean = false;

  constructor(private dialog: MatDialog) { }

  ngOnInit() {}

  openAddGenesPanel() {
    const dialogConfig = new MatDialogConfig();
	
    dialogConfig.data = {
      analysis_name: this.analysis_name,
    };
    
    this.addGenesDialogRef = this.dialog.open(AddGenesComponent, dialogConfig);
    this.addGenesDialogRef.afterClosed()
    		.subscribe( data=>this.handleAddGenesParamsUpdate(data) );
  }

  handleAddGenesParamsUpdate(status: number) {
    if (status == 1) {
      this.notifyChanges.emit();
    }
  }

}
