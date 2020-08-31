import { Component, OnInit, Input, SimpleChange } from '@angular/core';
import { HeatmapService } from '../heatmap.service'
import { MapLinkLayout } from '../map_link_layout';

@Component({
  selector: 'app-map-links',
  templateUrl: './map-links.component.html',
  styleUrls: ['./map-links.component.css']
})
export class MapLinksComponent implements OnInit {

  @Input() analysis_name: string;
  @Input() datase_name_1: string;
  @Input() datase_name_2: string;
  @Input() height: number;
  @Input() width: number;
  @Input() load_count: number;
  @Input() load_layout_count: number;
  @Input() isTransposed: number;

  layout: MapLinkLayout;
  frill_paths_starts: string[];
  frill_paths_ends: string[];
  mapping_colors_cache: number[][];
  frill_colors = new Map<string, number[]>();
  frill_colors_cache = new Map<string, number[]>();
  is_ready: boolean = false;

  //highlight_color: number[] = [22, 145, 0];
  highlight_color: number[] = [173, 23, 12];
  default_color: number[] = [175,175,175];

  constructor(private heatmapService: HeatmapService) { }

  ngOnInit() {
    this.getMapLinkLayout();
  }

  getMapLinkLayout(): void {
    
    this.is_ready = false;

    this.heatmapService.getMapLinkLayout(	
          this.analysis_name,
          this.datase_name_1,
          this.datase_name_2)
			.subscribe(
				data => this.prepMapLinkLayout(data), 
				() => console.log("observable complete"));
  }

  prepMapLinkLayout(data: MapLinkLayout) {

    this.layout = data;
    var cw = this.layout.cell_width_height;
    var half_cw = cw/2.0

    if (this.isTransposed == 0) {

      this.frill_paths_starts = [];
      for (var i=0; i<this.layout.mappings.length; i++) {
        var x = this.layout.gene_centres_1[this.layout.mappings[i][0]];
        var y = this.layout.start+this.layout.cell_width_height
        var d = "M" + (x-half_cw) + "," + (y-cw) + "L" + (x+half_cw) + "," + (y-cw) + "L" + x + "," + y + " z";
        this.frill_paths_starts.push(d);
      }

      this.frill_paths_ends = [];
      for (var i=0; i<this.layout.mappings.length; i++) {
        var x = this.layout.gene_centres_2[this.layout.mappings[i][1]];
        var y = this.layout.end - cw
        var d = "M" + (x-half_cw) + "," + (y+cw) + "L" + (x+half_cw) + "," + (y+cw) + "L" + x + "," + y + " z";
        this.frill_paths_ends.push(d);
      }

    } else if (this.isTransposed == 1) {

      this.frill_paths_starts = [];
      for (var i=0; i<this.layout.mappings.length; i++) {
        var x = this.layout.start+this.layout.cell_width_height;
        var y = this.layout.gene_centres_1[this.layout.mappings[i][0]];
        var d = "M" + (x-cw) + "," + (y-half_cw) + "L" + (x-cw) + "," + (y+half_cw) + "L" + x + "," + y + " z";
        this.frill_paths_starts.push(d);
      }

      this.frill_paths_ends = [];
      for (var i=0; i<this.layout.mappings.length; i++) {
        var x = this.layout.end - cw;
        var y = this.layout.gene_centres_2[this.layout.mappings[i][1]];
        var d = "M" + (x+cw) + "," + (y+half_cw) + "L" + (x+cw) + "," + (y-half_cw) + "L" + x + "," + y + " z";
        this.frill_paths_ends.push(d);
      }
    }

    this.mapping_colors_cache = JSON.parse(JSON.stringify(this.layout.mapping_colors));

    /*
        there can be multiple instances of each start and end tag
        therefore changing the color of one instance does not guarantee that the colored
        tag will be displayed. To overcome this a dictionary, mapping the unique x,y coordinates 
        to color is maintained
    */
    this.frill_colors = new Map<string, number[]>();
    for (var i=0; i<this.layout.mappings.length; i++) {
      this.setFrillColor(this.frill_colors, this.frill_paths_starts[i], this.layout.mapping_colors[i]);
      this.setFrillColor(this.frill_colors, this.frill_paths_ends[i], this.layout.mapping_colors[i]);
    }

    this.frill_colors_cache = new Map<string, number[]>();
    this.frill_colors.forEach((value: number[], key: string) => {
      let c = JSON.parse(JSON.stringify(value));
      this.frill_colors_cache.set(key, c);
    });

    this.is_ready = true;
  }

  setFrillColor(frills_colors_map: Map<string, number[]>, frill_key: string, new_color: number[]) {
    
    if (frills_colors_map.has(frill_key)) {
      let current_color = frills_colors_map.get(frill_key);
      if (current_color == this.default_color) {
        //set new color
        frills_colors_map.set(frill_key, new_color);
      }
    } else {
      frills_colors_map.set(frill_key, new_color);
    }
  }
  
  ngOnChanges(changes: {[propKey: string]: SimpleChange}) {
		
		for (let propName in changes) {
			if (propName == "load_count") {
				if(!changes['load_count'].isFirstChange()) {
					this.layout = null;			// nullify it so that "Loading..." message is displayed
					this.getMapLinkLayout();
				}
			} else if (propName == "load_layout_count") {
				if(!changes['load_layout_count'].isFirstChange()) {
					this.layout = null;			// nullify it so that "Loading..." message is displayed
					this.getMapLinkLayout();
				}
			}
		}
		
  }

  toggle_highlight_starts(start_gene_index: number) {

    let selected_frills = new Map<string, boolean>();

    for (let i=0; i<this.layout.mappings.length; i++) {
      if (this.layout.mappings[i][0] == start_gene_index) {
        this.toggle_highlight_line(i);
        //this.toggle_highlight_frill(this.frill_paths_starts[i]);
        //this.toggle_highlight_frill(this.frill_paths_ends[i]);
        selected_frills.set(this.frill_paths_starts[i], true);
        selected_frills.set(this.frill_paths_ends[i], true);
      }
    }

    selected_frills.forEach((value: boolean, key: string) => {
      this.toggle_highlight_frill(key);
    });
  }

  toggle_highlight_ends(end_gene_index: number) {

    let selected_frills = new Map<string, boolean>();

    for (let i=0; i<this.layout.mappings.length; i++) {
      if (this.layout.mappings[i][1] == end_gene_index) {
        this.toggle_highlight_line(i);
        //this.toggle_highlight_frill(this.frill_paths_starts[i]);
        //this.toggle_highlight_frill(this.frill_paths_ends[i]);
        selected_frills.set(this.frill_paths_starts[i], true);
        selected_frills.set(this.frill_paths_ends[i], true);
      }
    }

    selected_frills.forEach((value: boolean, key: string) => {
      this.toggle_highlight_frill(key);
    });
  }
  
  toggle_highlight_line(index: number) {
    if (this.layout.mapping_colors[index][0] == this.highlight_color[0] 
          && this.layout.mapping_colors[index][1] == this.highlight_color[1]
            && this.layout.mapping_colors[index][2] == this.highlight_color[2]) {
      this.layout.mapping_colors[index] = this.mapping_colors_cache[index];
    } else {
      this.layout.mapping_colors[index] = this.highlight_color;
    }
  }

  toggle_highlight_frill(frill_key: string) {
    let current_color = this.frill_colors.get(frill_key);
    if (current_color[0] == this.highlight_color[0] && current_color[1] == this.highlight_color[1] && current_color[2] == this.highlight_color[2]) {
      this.frill_colors.set(frill_key, this.frill_colors_cache.get(frill_key));
    } else {
      this.frill_colors.set(frill_key, this.highlight_color);
    }
  }

}
