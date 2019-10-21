import { Component, OnInit, Input, Output, OnChanges, SimpleChange, EventEmitter } from '@angular/core';
import { CreateListComponent } from '../create-list/create-list.component'
import { HierarchicalClusteringComponent } from '../hierarchical-clustering/hierarchical-clustering.component'
import { UploadListComponent } from '../upload-list/upload-list.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";
import { ListService } from '../list.service'
import { AnalysisService } from '../analysis.service'
import { ServerResponseData } from '../server_response'
import { Router } from '@angular/router';
import { SaveWorkspaceComponent } from '../save-workspace/save-workspace.component'
import { CloseAnalysisComponent } from '../close-analysis/close-analysis.component'
import { LocalSettings } from '../local-settings'

@Component({
  selector: 'app-menu-panel',
  templateUrl: './menu-panel.component.html',
  styleUrls: ['./menu-panel.component.css']
})
export class MenuPanelComponent implements OnInit {

  @Input() analysis_name: string;
  @Output() notifySelectionPanelToggle = new EventEmitter();
  @Output() onListChange = new EventEmitter<string>();
  /*
  @Output() clusteringParamsChange = new EventEmitter<string>();
  */
  
  server_response: ServerResponseData;
  createListDialogRef: MatDialogRef<CreateListComponent>;
  uploadListDialogRef: MatDialogRef<UploadListComponent>;
  //clusteringDialogRef: MatDialogRef<ClusteringDialogComponent>;
  clusteringDialogRef: MatDialogRef<HierarchicalClusteringComponent>;

  constructor(
    private dialog: MatDialog, 
    private listService: ListService, 
    private analysisService: AnalysisService, 
    private router: Router) { }

  ngOnInit() {
  }

  openSaveViz() {
    window.open(LocalSettings.HOME_URL + '/#/save_viz?analysis_name='+this.analysis_name);
  }

  toggleSelectionPanel(){
    this.notifySelectionPanelToggle.emit();
  }

  showHomePage() {
    window.open(LocalSettings.HOME_URL, '_blank');
  }

  openCreateListDialog() {
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '500';
		dialogConfig.width = '200';
		dialogConfig.position = {
			top: '300',
			left: '300'
    };
    dialogConfig.data = {
			analysis_name: this.analysis_name
		};
    this.createListDialogRef = this.dialog.open(CreateListComponent, dialogConfig);
    
    this.createListDialogRef.afterClosed()
    		.subscribe( data=>this.createList(data) );
  }
  
  openUploadListDialog() {
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '500';
		dialogConfig.width = '200';
		dialogConfig.position = {
			top: '300',
			left: '300'
    };
    dialogConfig.data = {
			analysis_name: this.analysis_name
		};
    this.uploadListDialogRef = this.dialog.open(UploadListComponent, dialogConfig);
    
    this.uploadListDialogRef.afterClosed()
    		.subscribe( data=>this.uploadList(data) );
  }
  
  uploadList(status: number) {
    if (status == 1) {
      this.onListChange.emit();
    }
  }

  createList(list_name: string) {
    if (list_name) {
      this.listService.createList(this.analysis_name, list_name)
        .subscribe(
            data => this.server_response = data, 
            () => console.log("observable complete"), 
            () => this.notifyResponse()
        );
    }
  }

  notifyResponse() {
		if(this.server_response.status == 1) {
      alert(this.server_response.message);
      this.onListChange.emit();
		} else {
			alert(this.server_response.message + '. ' + this.server_response.detailed_reason);
		}
  }

  saveWorkspace() {
    const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '600';
		dialogConfig.width = '900';
		dialogConfig.position = {
			top: '200',
			left: '100'
		};
		dialogConfig.data = {
			analysis_name: this.analysis_name,
		};
		var saveWorkspaceDialog = this.dialog.open(SaveWorkspaceComponent, dialogConfig);

		saveWorkspaceDialog.afterClosed()
    		.subscribe( data=>this.downloadWorkspace(data) );
  }
  
  downloadWorkspace(filename: string) {
    if (filename) {
      this.analysisService.downloadWorkspace(this.analysis_name, filename)
        .subscribe(
            data => this.downloadFile(data, filename), 
            () => console.log("observable complete"));
    }
  }

  downloadFile(data, filename: string) {
    const blob = new Blob([data], { type: 'application/download' });
    const url= window.URL.createObjectURL(blob);
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style.display = "none";
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  closeAnalysis() {
    const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '600';
		dialogConfig.width = '900';
		dialogConfig.position = {
			top: '200',
			left: '100'
		};
		dialogConfig.data = {
			analysis_name: this.analysis_name,
		};
		var closeAnalysisDialog = this.dialog.open(CloseAnalysisComponent, dialogConfig);

		closeAnalysisDialog.afterClosed()
    		.subscribe( data=>this.downloadWorkspace(data) );
  }

}
