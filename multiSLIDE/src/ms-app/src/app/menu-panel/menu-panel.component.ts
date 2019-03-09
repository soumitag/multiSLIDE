import { Component, OnInit, Input, Output, OnChanges, SimpleChange, EventEmitter } from '@angular/core';
import { CreateListComponent } from '../create-list/create-list.component'
import { HierarchicalClusteringComponent } from '../hierarchical-clustering/hierarchical-clustering.component'
import { MatDialog, MatDialogConfig, MatDialogRef } from "@angular/material";
import { ListService } from '../list.service'
import { ServerResponseData } from '../server_response'

@Component({
  selector: 'app-menu-panel',
  templateUrl: './menu-panel.component.html',
  styleUrls: ['./menu-panel.component.css']
})
export class MenuPanelComponent implements OnInit {

  @Input() analysis_name: string;
  server_response: ServerResponseData;
  createListDialogRef: MatDialogRef<CreateListComponent>;
  //clusteringDialogRef: MatDialogRef<ClusteringDialogComponent>;
  clusteringDialogRef: MatDialogRef<HierarchicalClusteringComponent>;

  constructor(private dialog: MatDialog, private listService: ListService) { }

  ngOnInit() {
  }

  openClusteringDialog(){
    const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '500';
		dialogConfig.width = '200';
		dialogConfig.position = {
			top: '300',
			left: '300'
		};
    this.clusteringDialogRef = this.dialog.open(HierarchicalClusteringComponent, dialogConfig);
  }

  openCreateListDialog() {
		const dialogConfig = new MatDialogConfig();
		dialogConfig.height = '500';
		dialogConfig.width = '200';
		dialogConfig.position = {
			top: '300',
			left: '300'
		};
    this.createListDialogRef = this.dialog.open(CreateListComponent, dialogConfig);
    
    this.createListDialogRef.afterClosed()
    		.subscribe( data=>this.createList(data) );

	}

  createList(list_name: string) {
    this.listService.createList(this.analysis_name, list_name)
			.subscribe(
					data => this.server_response = data, 
					() => console.log("observable complete"), 
          () => this.notifyResponse()
      );
  }

  notifyResponse() {
		if(this.server_response.status == 1) {
      alert(this.server_response.message);
		} else {
			alert(this.server_response.message + '. ' + this.server_response.detailed_reason);
		}
	}

}
