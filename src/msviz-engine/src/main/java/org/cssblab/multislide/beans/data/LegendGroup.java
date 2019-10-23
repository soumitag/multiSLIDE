/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.ClinicalInformation;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;

/**
 *
 * @author Soumita
 */
public class LegendGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final int LEGEND_GROUP_SUBTYPE_CONTINUOUS_PHENOTYPE = 0;
    public static final int LEGEND_GROUP_SUBTYPE_CATEGORICAL_PHENOTYPE = 1;
    
    public String title;
    public int subtype;     // continuous=0; categorical=1; network=2; unknown=-1
    public ArrayList <String> legend_names;
    public ArrayList <double[]> colors;
    public ArrayList <double[]> stroke_colors;
    
    public LegendGroup () {
        this.title = "";
        this.subtype = -1;
        this.legend_names = new ArrayList <String> ();
        this.colors = new ArrayList <double[]> ();
        this.stroke_colors = new ArrayList <double[]> ();
    }
    
    public LegendGroup (String title, ArrayList <String> legend_names, ArrayList <double[]> colors) {
        this.title = title;
        this.subtype = LegendGroup.LEGEND_GROUP_SUBTYPE_CATEGORICAL_PHENOTYPE;
        this.legend_names = legend_names;
        this.colors = colors;
        this.stroke_colors = colors;
    }
    
    public LegendGroup (String title, double[] min_max, ArrayList <short[]> colors) {
        this.title = title;
        this.subtype = LegendGroup.LEGEND_GROUP_SUBTYPE_CONTINUOUS_PHENOTYPE;
        this.legend_names = new ArrayList();
        this.legend_names.add(String.format("%5.2e",min_max[0]));
        this.legend_names.add(String.format("%5.2e",min_max[1]));
        this.colors = new ArrayList <double[]> ();
        for (int i=0; i<colors.size(); i++) {
            double[] d = new double[3];
            d[0] = (double)colors.get(i)[0];
            d[1] = (double)colors.get(i)[1];
            d[2] = (double)colors.get(i)[2];
            this.colors.add(d);
        }
        this.stroke_colors = this.colors;
    }
    
    public LegendGroup (String title, ArrayList <String> legend_names, ArrayList <double[]> colors, ArrayList <double[]> stroke_colors) {
        this.title = title;
        this.subtype = 2;
        this.legend_names = legend_names;
        this.colors = colors;
        this.stroke_colors = stroke_colors;
    }
    
    public static ArrayList <LegendGroup> createPhenotypeLegendGroups(AnalysisContainer analysis) throws MultiSlideException {
        
        HashMap <String, HashMap <String, double[]>> phenotypeColorMap = analysis.data.clinical_info.phenotypeColorMap;
        HashMap <String, HashMap <String, Integer>> phenotypeValueSortMap = analysis.data.clinical_info.phenotypeValueSortMap;
        
        ArrayList <LegendGroup> L = new ArrayList <LegendGroup> ();
        
        for (Map.Entry pair : phenotypeColorMap.entrySet()) {
            String phenotype = (String)pair.getKey();
            if (analysis.data.fs_data.hasPhenotype(phenotype)) {
                if (analysis.data.clinical_info.getPhenotypeDatatype(phenotype) == ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS) {
                    L.add(new LegendGroup(phenotype, analysis.data.clinical_info.getPhenotypeRange(phenotype), analysis.data.clinical_info.standard_continuous_colors));
                } else {
                    int sz = phenotypeValueSortMap.get(phenotype).size();
                    String[] names = new String[sz];
                    double[][] colors = new double[sz][3];
                    for (Map.Entry pair_1 : ((HashMap <String, double[]>)pair.getValue()).entrySet()) {
                        String name = (String)pair_1.getKey();
                        int position = phenotypeValueSortMap.get(phenotype).get(name);
                        if (name.equals("")){
                            name = "-";
                        }
                        names[position] = name;
                        colors[position] = (double[])pair_1.getValue();
                    }
                    ArrayList <String> names_list = new ArrayList <String> (Arrays.asList(names));
                    ArrayList <double[]> colors_list = new ArrayList <double[]> (Arrays.asList(colors));
                    L.add(new LegendGroup(phenotype, names_list, colors_list));
                }
            }
        }
        
        return L;
    }
    
    public static ArrayList <LegendGroup> createGenetagLegendGroup(
            AnalysisContainer analysis
    ) throws MultiSlideException {
        
        //String[] group_ids = analysis.data_selection_state.getGeneGroupNames();
        //String[] queryTypes = analysis.data_selection_state.getGeneGroupTypes();
        
        HashMap <String, GeneGroup> gene_groups = analysis.data.fs_data.getGeneGroups();
        
        LegendGroup pathways = new LegendGroup("Pathways", new ArrayList <String> (), new ArrayList <double[]> ());
        LegendGroup ontologies = new LegendGroup("Gene Ontologies", new ArrayList <String> (), new ArrayList <double[]> ());
        LegendGroup genes = new LegendGroup("Genes", new ArrayList <String> (), new ArrayList <double[]> ());
        
        boolean hasGenes = false;
        boolean hasPathways = false;
        boolean hasOntologies = false;
        
        for (GeneGroup gene_group : gene_groups.values()) {
            if (gene_group.type.equals("entrez")) {
                if (!hasGenes) {
                    genes.legend_names.add("Gene");
                    genes.colors.add(GeneGroup.GROUP_TYPE_GENE_COLOR);
                }
                hasGenes = true;
            } else if (gene_group.type.equals("pathid")) {
                hasPathways = true;
                pathways.legend_names.add(gene_group.display_tag);
                pathways.colors.add(gene_group.color);
            } else if (gene_group.type.equals("goid")) {
                hasOntologies = true;
                ontologies.legend_names.add(gene_group.display_tag);
                ontologies.colors.add(gene_group.color);
            }
        }
        
        ArrayList <LegendGroup> L = new ArrayList <LegendGroup> ();
        if (hasGenes) {
            L.add(genes);
        }
        if (hasPathways) {
            L.add(pathways);
        }
        if (hasOntologies) {
            L.add(ontologies);
        }
        
        return L;
    }
    
    public static ArrayList <LegendGroup> createNetworkNeighborhoodLegendGroup(
            AnalysisContainer analysis
    ) {
        ArrayList <LegendGroup> G = new ArrayList <LegendGroup> ();
        LegendGroup L = new LegendGroup();
        L.title = "Network and Neighbor Types";
        L.legend_names.add("Protein-Protein Interactions Search Key");
        L.legend_names.add("Protein-Protein Interactions");
        L.legend_names.add("miRNA Targets Search Key");
        L.legend_names.add("miRNA Targets");
        L.legend_names.add("Transcription Factor Targets Search Key");
        L.legend_names.add("Transcription Factor Targets");
        L.colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_COLOR);
        L.stroke_colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_STROKE_COLOR);
        G.add(L);
        return G;
    }
    
    /*
    public void createGenetagLegendGroup_1(AnalysisContainer analysis) {
        HashMap <String, double[]> group_color = analysis.group_colors;
        this.legend_names = new ArrayList <String> ();
        this.colors = new ArrayList <double[]> ();
        for (Map.Entry pair : group_color.entrySet()) {
            String name = (String)pair.getKey();
            if (name.equals("")){
                name = "-";
            }
            this.legend_names.add(name);
            this.colors.add((double[])pair.getValue());
        }
        this.title = "Pathways";
    }
    */
}