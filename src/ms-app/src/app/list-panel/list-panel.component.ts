import { Component, OnInit, Input, Output, EventEmitter, Inject } from '@angular/core';
import { ListData } from '../list_data'
import { ListService } from '../list.service';
import { AnalysisService } from '../analysis.service'
import { ServerResponseData } from '../server_response'
import { Observable } from 'rxjs/Observable';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: 'app-list-panel',
  templateUrl: './list-panel.component.html',
  styleUrls: ['./list-panel.component.css']
})
export class ListPanelComponent implements OnInit {

  /*
  @Input() analysis_name: string;
  @Output() onListChange = new EventEmitter<string>();
  @Output() visualizeListChange = new EventEmitter<string>();
  */

  analysis_name: string;
  lists: ListData[];
  error_getting_data: boolean = false;
  list_service_response: ServerResponseData;

  constructor(
    private dialogRef: MatDialogRef<ListPanelComponent>, 
    @Inject(MAT_DIALOG_DATA) data,
    private listService: ListService, 
    private analysisService: AnalysisService) { 
      this.analysis_name = data.analysis_name;
      this.dialogRef.updatePosition({ top: '110px', left: '500px' });
      //this.dialogRef.updateSize('550px', '500px');
  }

  ngOnInit() { 
    this.getData();
  }

  getData() {
    this.listService.getUserLists(this.analysis_name)
			.subscribe(
					data => this.lists = data, 
					() => console.log("observable complete"), 
					() => this.setErrorResponseonGet());
  }

  setErrorResponseonGet() {
    this.error_getting_data = true;
  }

  showListDetails(i) {
    var x = document.getElementById("details_" + i);
    if (x.style.display === "none" || x.style.display === "") {
      x.style.display = "block";
    } else {
      x.style.display = "none";
    }
  }

  removeFromList(list_index: number, entrez_index: number) {
    this.listService.removeFromList(this.analysis_name, 
      this.lists[list_index].entrez[entrez_index], this.lists[list_index].name)
			.subscribe(
					data => this.list_service_response = data, 
					() => console.log("observable complete"), 
					() => this.notifyResponse());
  }

  notifyResponse() {
    //this.onListChange.emit();
		if(this.list_service_response.status == 1) {
      alert(this.list_service_response.message);
      this.getData();
		} else {
			alert(this.list_service_response.message + '. ' + this.list_service_response.detailed_reason);
		}
  }
  
  serializeList(list_name: string) {
    this.listService.serializeList(this.analysis_name, list_name)
			.subscribe(
					data => this.downloadFile(data, list_name), 
					() => console.log("observable complete"));
  }

  removeList(list_name: string) {
    this.listService.deleteList(this.analysis_name, list_name)
			.subscribe(
					data => this.list_service_response = data, 
					() => console.log("observable complete"), 
					() => this.notifyListDeleteResponse());
  }

  notifyListDeleteResponse() {
    //this.onListChange.emit();
		if(this.list_service_response.status == 1) {
      alert(this.list_service_response.message);
      this.getData();
		} else {
			alert(this.list_service_response.message + '. ' + this.list_service_response.detailed_reason);
		}
  }

  /*
  visualizeList(list_name: string) {
    this.analysisService.initializeAnalysisFromList(this.analysis_name, list_name)
			.subscribe(
					data => this.list_service_response = data, 
          () => console.log("observable complete"),
          () => {
            if (this.list_service_response.status == 1) {
              this.visualizeListChange.emit(list_name);
            } else if (this.list_service_response.status == 0) {
              alert('Unable to visualize list. ' + this.list_service_response.message + ' ' + this.list_service_response.detailed_reason);
            }
          });
  }
  */

  downloadFile(data, list_name) {
    const blob = new Blob([data], { type: 'text/csv' });
    const url= window.URL.createObjectURL(blob);
    var a = document.createElement("a");
    document.body.appendChild(a);
    a.style.display = "none";
    a.href = url;
    a.download = list_name + '.txt';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  close() {
    this.dialogRef.close();
  }
}
