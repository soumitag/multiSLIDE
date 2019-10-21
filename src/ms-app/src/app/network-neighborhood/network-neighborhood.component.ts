import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material";
import { NeighborhoodSearchResults } from '../neighborhood_search_results';
import { SearchService } from '../search.service';

@Component({
  selector: 'app-network-neighborhood',
  templateUrl: './network-neighborhood.component.html',
  styleUrls: ['./network-neighborhood.component.css']
})
export class NetworkNeighborhoodComponent implements OnInit {

  data: NeighborhoodSearchResults;
  
  analysis_name: string;
  query_entrez: string;
  query_display_tag: string;
  neighbor_search_type: string;
  neighbor_search_type_display_name: string;
  dataset_name: string;

  selected_entrez: string[] = [];
  results_selection_ind: boolean[] = [];
  all_selected: boolean = false;
  all_button_text: string = 'Select All';

  constructor(private searchService: SearchService, 
              private dialogRef: MatDialogRef<NetworkNeighborhoodComponent>,
              @Inject(MAT_DIALOG_DATA) data) { 

      this.analysis_name = data.analysis_name;
      this.query_entrez = data.entrez;
      this.query_display_tag = data.query_display_tag;
      this.neighbor_search_type = data.search_type;
      this.dataset_name = data.dataset_name;
      this.dialogRef.updatePosition({ top: '110px', left: '700px' });
      this.dialogRef.updateSize('690px','800px');
      if (this.neighbor_search_type == 'ppi_entrez') {
        this.neighbor_search_type_display_name = 'Protein-Protein Interactions';
      } else if (this.neighbor_search_type == 'mirna_id') {
        this.neighbor_search_type_display_name = 'miRNA Targets';
      } else if (this.neighbor_search_type == 'tf_entrez') {
        this.neighbor_search_type_display_name = 'Transcription Factor Targets';
      }
  }

  ngOnInit() {
    this.doSearch();
  }

  doSearch(): void {
		this.searchService.doNeighborhoodSearch(
            this.analysis_name, 
            this.dataset_name,
            this.query_entrez, 
            this.neighbor_search_type
    )
			.subscribe(
        data => this.data = data, 
        () => console.log("observable complete"),
        () => this.notifySearchStatus()
      );
  }
  
  notifySearchStatus() {
    if (this.data.status == 0) {
      alert(this.data.message);
    }
  }

  close() {
    this.dialogRef.close();
  }

  toggleResult(neighbor_index: number) {
    if (this.results_selection_ind[neighbor_index]) {
      this.removeResult(neighbor_index);
    } else {
      this.addResult(neighbor_index);
    }
  }

  selectAll() {
    if (this.all_selected) {
      for (var i=0; i<this.data.neighbor_entrez.length; i++) {
        if (this.data.neighbor_in_dataset_ind[i]) {
          this.removeResult(i);
        }
      }
      this.all_selected = false;
      this.all_button_text = 'Select All';
    } else {
      for (var i=0; i<this.data.neighbor_entrez.length; i++) {
        if (this.data.neighbor_in_dataset_ind[i]) {
          this.addResult(i);
        }
      }
      this.all_selected = true;
      this.all_button_text = 'Deselect All';
    }
  }

  addResult(neighbor_index: number) {
    let neighbor_entrez = this.data.neighbor_entrez[neighbor_index];
    let index = this.selected_entrez.indexOf(neighbor_entrez);
    if (index == -1) {
      this.selected_entrez.push(neighbor_entrez);
    }
    this.results_selection_ind[neighbor_index] = true;
  }

  removeResult(neighbor_index: number) {
    let neighbor_entrez = this.data.neighbor_entrez[neighbor_index];
    let index = this.selected_entrez.indexOf(neighbor_entrez);
    this.selected_entrez.splice(index, 1);
    this.results_selection_ind[neighbor_index] = false;
  }

  saveChanges() {
    this.dialogRef.close(this.selected_entrez);
  }
}
