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
import java.util.List;
import java.util.Map;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.data.ClinicalInformation;
import org.cssblab.multislide.structure.GeneGroup;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.structure.NetworkNeighbor;
import org.cssblab.multislide.utils.Utils;

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
        HashMap <String, HashMap <String, Integer>> phenotypeValueSortMap = analysis.data.clinical_info.getDistinctPhenotypeValues(
                analysis.data_selection_state.selected_phenotypes
        );
        
        ArrayList <LegendGroup> L = new ArrayList <> ();
        
        for (Map.Entry pair : phenotypeColorMap.entrySet()) {
            String phenotype = (String)pair.getKey();
            if (analysis.data_selection_state.hasPhenotype(phenotype)) {
                if (analysis.data.clinical_info.getPhenotypeDatatype(phenotype) == ClinicalInformation.PHENOTYPE_DATATYPE_CONTINUOUS) {
                    L.add(new LegendGroup(phenotype, analysis.data.clinical_info.getPhenotypeRange(phenotype), analysis.data.clinical_info.standard_continuous_colors));
                } else {
                    int sz = phenotypeValueSortMap.get(phenotype).size();
                    String[] names = new String[sz];
                    double[][] colors = new double[sz][3];
                    int position = 0;
                    for (Map.Entry pair_1 : ((HashMap <String, double[]>)pair.getValue()).entrySet()) {
                        String name = (String)pair_1.getKey();
                        //int position = phenotypeValueSortMap.get(phenotype).get(name);
                        if (name.equals("")){
                            name = "-";
                        }
                        names[position] = name;
                        colors[position] = (double[])pair_1.getValue();
                        position++;
                    }
                    ArrayList <String> names_list = new ArrayList <> (Arrays.asList(names));
                    ArrayList <double[]> colors_list = new ArrayList <> (Arrays.asList(colors));
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
        
        HashMap <String, GeneGroup> gene_groups = analysis.data.selected.getGeneGroups();
        
        LegendGroup pathways = new LegendGroup("Pathways", new ArrayList <String> (), new ArrayList <double[]> ());
        LegendGroup ontologies = new LegendGroup("Gene Ontologies", new ArrayList <String> (), new ArrayList <double[]> ());
        LegendGroup genes = new LegendGroup("Genes", new ArrayList <String> (), new ArrayList <double[]> ());
        LegendGroup user_defined = new LegendGroup("User-defined Functional Groups", new ArrayList <String> (), new ArrayList <double[]> ());
        
        boolean hasGenes = false;
        boolean hasPathways = false;
        boolean hasOntologies = false;
        boolean hasUserDefined = false;
        
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
            } else if (gene_group.type.equals("user_defined")) {
                hasUserDefined = true;
                user_defined.legend_names.add(gene_group.display_tag);
                user_defined.colors.add(gene_group.color);
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
        if (hasUserDefined) {
            L.add(user_defined);
        }
        
        return L;
    }
    
    public static ArrayList <LegendGroup> createNetworkNeighborhoodLegendGroup(
            AnalysisContainer analysis
    ) {
        ArrayList <LegendGroup> G = new ArrayList <LegendGroup> ();
        LegendGroup L = new LegendGroup();
        L.title = "Network and Neighbor Types";
        L.legend_names.add("Protein-Protein Interactions");
        L.legend_names.add("PPI Search Key");
        L.legend_names.add("miRNA Targets");
        L.legend_names.add("miRNA Targets Search Key");
        L.legend_names.add("Transcription Factor Targets");
        L.legend_names.add("TF Targets Search Key");
        // L.legend_names.add("Search Key");
        L.colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_COLOR);
        L.colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_COLOR);
        // L.colors.add(NetworkNeighbor.SEARCH_KEY_COLOR);
        L.stroke_colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.PPI_ENTREZ_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.MIRNA_ID_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_STROKE_COLOR);
        L.stroke_colors.add(NetworkNeighbor.TF_ENTREZ_NEIGHBOR_STROKE_COLOR);
        // L.stroke_colors.add(NetworkNeighbor.SEARCH_KEY_COLOR);
        G.add(L);
        return G;
    }
    
    public static ArrayList <LegendGroup> createInterOmicsConnectionsLegendGroup(
            AnalysisContainer analysis
    ) {
        
        ArrayList <LegendGroup> G = new ArrayList <LegendGroup> ();
        
        for (int i=0; i<analysis.data_selection_state.selected_datasets.length-1; i++) {
            
            String dataset_name_1 = analysis.data_selection_state.selected_datasets[i];
            String dataset_name_2 = analysis.data_selection_state.selected_datasets[i+1];
            
            BipartiteLinkageGraph linkage = null;
            if (analysis.data_selection_state.user_defined_between_omics_linkages.containsKey(dataset_name_1 + "_" + dataset_name_2)) {
                linkage = analysis.data_selection_state.user_defined_between_omics_linkages.get(dataset_name_1 + "_" + dataset_name_2);
            } else if (analysis.data_selection_state.user_defined_between_omics_linkages.containsKey(dataset_name_2 + "_" + dataset_name_1)) {
                linkage = analysis.data_selection_state.user_defined_between_omics_linkages.get(dataset_name_2 + "_" + dataset_name_1);
            }
            
            HashMap <String, String> mapping_names = new HashMap <> ();
            HashMap <String, int[]> mapping_colors = new HashMap <> ();
            
            if (linkage != null) {
                
                List <List<String>> feature_ids_1 = analysis.data.selected.getFeatureIDs(
                        linkage.dataset_name_1, analysis.global_map_config, new String[]{linkage.column_name_1});
            
                List <List<String>> feature_ids_2 = analysis.data.selected.getFeatureIDs(
                            linkage.dataset_name_2, analysis.global_map_config, new String[]{linkage.column_name_2});

                for (int j=0; j<feature_ids_1.size(); j++) {
                    for (int k=0; k<feature_ids_2.size(); k++) {
                        if (linkage.isConnected(feature_ids_1.get(j).get(0), feature_ids_2.get(k).get(0))) {
                            String name = linkage.getName(feature_ids_1.get(j).get(0), feature_ids_2.get(k).get(0));
                            int[] color = linkage.getColor(feature_ids_1.get(j).get(0), feature_ids_2.get(k).get(0));
                            if (!name.equals("") || color[0]!=175 || color[1]!=175 || color[2]!=175) {
                                String key = name + "_" + color[0] + "_" + color[1] + "_" + color[2];
                                mapping_names.put(key, name);
                                mapping_colors.put(key, color);
                            }
                        }
                    }
                }
                
                LegendGroup L = new LegendGroup();
                L.title = "Inter-omics Connections (" + linkage.display_name + ")";
                G.add(L);
                
                for (String key : mapping_names.keySet()) {
                    String name = mapping_names.get(key);
                    L.legend_names.add(name);
                    int[] colors = mapping_colors.get(key);
                    double[] colors_d = new double[3];
                    int index = 0;
                    for (int c: colors) {
                        colors_d[index++] = c;
                    }
                    L.colors.add(colors_d);
                    L.stroke_colors.add(colors_d);
                }
                
            }
        }
        
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
