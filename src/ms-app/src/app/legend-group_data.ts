export class LegendGroupData {
    title: string;
    subtype: number;    // continuous=0; categorical=1; network=2; unknown=-1
    legend_names: string[];
    colors: number[][];
    stroke_colors: number[][];
}