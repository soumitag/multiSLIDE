import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ListData } from '../list_data'
import { ListService } from '../list.service';
import { ServerResponseData } from '../server_response'
import { Observable } from 'rxjs/Observable';

@Component({
  selector: 'app-list-panel',
  templateUrl: './list-panel.component.html',
  styleUrls: ['./list-panel.component.css']
})
export class ListPanelComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() lists: ListData[];

  @Output() onListChange = new EventEmitter<string>();

  list_service_response: ServerResponseData;

  constructor(private listService: ListService) { }

  ngOnInit() { }

  showListDetails(i) {
    var x = document.getElementById("details_" + i);
    if (x.style.display === "none") {
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
    this.onListChange.emit();
		if(this.list_service_response.status == 1) {
			alert(this.list_service_response.message);
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
}
