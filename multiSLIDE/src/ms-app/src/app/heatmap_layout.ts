export class HeatmapLayout {
    cell_width_height: number;
    map_height: number;
    map_width: number;
    column_left_x: number[];
    row_top_y: number[];
    row_name_width: number;
    header_height: number; 
    column_header_text_anchor_x: number[];
    column_header_text_anchor_y: number;
    row_name_text_anchor_x: number;
    row_name_text_anchor_y: number[];
    phenotype_tag_anchor_x: number[];
    phenotype_tag_anchor_y: number[];
    phenotype_label_anchor_x: number[];
    phenotype_label_anchor_y: number;
    genetag_anchor_y: number[];
    svg_height: number;
    svg_width: number;
    plot_title_x: number;
    plot_title_y: number;
    row_label_font_size: number;
    column_label_font_size: number;
    phenotype_label_font_size: number;
    settings_icon_left: number;
    settings_icon_size: number;
    search_tag_radius: number;
    search_tags_xy: number[][][];
    search_tag_lines_xxy: number[][];
    colorbar_cell_x: number;
    colorbar_cell_y: number[];
    colorbar_cell_width: number;
    colorbar_cell_height: number;
    colorbar_tick_width: number;
    colorbar_tick_y: number[];
    colorbar_tick_x1_x2: number[];
    colorbar_tick_text_x: number;
    missing_value_colorbar_tick_y: number;
  }