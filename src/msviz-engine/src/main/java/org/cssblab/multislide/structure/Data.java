package org.cssblab.multislide.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.searcher.GoObject;
import org.cssblab.multislide.searcher.PathwayObject;
import org.cssblab.multislide.searcher.Searcher;
import org.cssblab.multislide.utils.FileHandler;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.cssblab.multislide.algorithms.clustering.BinaryTree;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.SearchResultSummary;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.searcher.GeneObject;
import org.cssblab.multislide.utils.FormElementMapper;
import org.cssblab.multislide.utils.IndexedEntrez;
import org.cssblab.multislide.utils.IndexedEntrezComparator;
import org.cssblab.multislide.utils.PhenotypeComparator;
import org.cssblab.multislide.utils.PhenotypeSet;

/**
 *
 * @author Soumita
 */

public class Data implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public int nDatasets;
    
    public DatasetSpecs[] dataset_specs;
    public ClinicalInformation clinical_info;
    public String[] dataset_names;
    public ArrayList <Float[][]> expressions;
    public MetaData metadata;
    public HashMap <String, Integer> datasetIndexMap;
    public String[] sample_ids;
    public ArrayList <boolean[]> missing_rows_indicator;
    //private final HashMap <String, int[]> entrez_counts_map;           // entrez -> {dataset_name -> counts, dataset_name -> counts, ...}
    
    public boolean has_miRNA_Data;
    public FilteredSortedData fs_data; 
    
    public Data (
        String analysis_basepath, 
        HashMap <String, DatasetSpecs> dataset_specs_map,
        ColorPalette categorical_palette, 
        ColorPalette continuous_palette,
        Searcher searcher,
        HashMap <String, Integer> identifier_index_map
    ) throws MultiSlideException {
        
        this.nDatasets = dataset_specs_map.size()-1;
        this.dataset_names = new String[dataset_specs_map.size()-1];
        clinical_info = new ClinicalInformation(categorical_palette, continuous_palette);

        int nSamplesInClinicalInfo = 0;
        Iterator it = dataset_specs_map.entrySet().iterator();
        has_miRNA_Data = false;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (spec.omics_type.equals("clinical-info")) {
                String filepath = analysis_basepath + File.separator + spec.filename_within_analysis_folder;
                nSamplesInClinicalInfo = clinical_info.loadClinicalInformation(filepath, FormElementMapper.parseDelimiter(spec.delimiter));
            }
            if (spec.omics_type.equals("mi_rna")) {
                has_miRNA_Data = true;
            }
        }
        
        HashMap <String, Integer> dataset_names_counts = new HashMap <String, Integer> ();
        it = dataset_specs_map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (!spec.omics_type.equals("clinical-info")) {
                if (dataset_names_counts.containsKey(spec.omics_type)) {
                    int c = dataset_names_counts.get(spec.omics_type);
                    dataset_names_counts.put(spec.omics_type, c+1);
                } else {
                    dataset_names_counts.put(spec.omics_type, 1);
                }
            }
        }
        
        datasetIndexMap = new HashMap <String, Integer> ();
        dataset_specs = new DatasetSpecs[dataset_specs_map.size()-1];
        ArrayList <String[][]> raw_datasets = new ArrayList <String[][]> ();
        it = dataset_specs_map.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (!spec.omics_type.equals("clinical-info")) {
                String filepath = analysis_basepath + File.separator + spec.filename_within_analysis_folder;
                raw_datasets.add(loadData(filepath, FormElementMapper.parseDelimiter(spec.delimiter)));
                if (dataset_names_counts.get(spec.omics_type) > 1) {
                    dataset_names[count] = spec.omics_type + spec.filename;
                } else {
                    dataset_names[count] = spec.omics_type;
                }
                datasetIndexMap.put(dataset_names[count], count);
                dataset_specs[count++] = spec;
            }
        }
        
        metadata = new MetaData();
        metadata.extract(dataset_names, raw_datasets, dataset_specs, searcher, identifier_index_map);
        ArrayList <String[][]> raw_data = metadata.getNonMetaDataColumns(dataset_names, raw_datasets);
        
        expressions = new ArrayList <Float[][]> ();
        
        ArrayList <String[][]> aligned_datasets = alignSamples(raw_data, clinical_info);
        
        if(sample_ids.length > nSamplesInClinicalInfo) {
            throw new MultiSlideException("Missing sample information in clinincal information file.");
        } else {
            for (int s = 0; s < sample_ids.length; s++) {
                if (!clinical_info.samplePhenotypeMap.containsKey(sample_ids[s])) {
                    throw new MultiSlideException("Missing sample information for sample id: " + sample_ids[s] + " in clinincal information file.");
                }
            }
        }
        
        //specs[i].getMappedColname("genesymbol_2021158607524066")
        for (int i=0; i<dataset_names.length; i++) {
            expressions.add(parseDataset (aligned_datasets.get(i)));
        }
        
    }
    
    public int getEntrezCounts(int dataset_index, String entrez) {
        HashMap <String, ArrayList<Integer>> entrezPosMap_i = metadata.entrezPosMaps.get(dataset_names[dataset_index]);
        ArrayList <Integer> temp = entrezPosMap_i.get(entrez);
        int sz = 0;
        if (temp != null) {
            sz = temp.size();
        }
        return sz;
    }
    
    private Float[][] parseDataset (String[][] raw_data) throws MultiSlideException {
        
        Float[][] data = new Float[raw_data[0].length][raw_data.length-1];

        try {

            for (int i = 1; i < raw_data.length; i++) {
                for (int j = 0; j < raw_data[0].length; j++) {
                    if (raw_data[i][j].equalsIgnoreCase("NA")) {
                        data[j][i - 1] = Float.NaN;
                    } else {
                        data[j][i - 1] = Float.parseFloat(raw_data[i][j]);
                    }
                }
            }

            return data;

        } catch (Exception e) {
            System.out.println(e);
            throw new MultiSlideException(e.getMessage());
        }
        
    }
    
    public void prepareDataForGroup(
            AnalysisContainer analysis,
            String identifier,
            ColorPalette gene_group_color_palette
    ) throws MultiSlideException {
        
        SearchResultSummary[] searches = analysis.data_selection_state.searches;
        
        /*
        Get all entrez ids for user provided gene groups (pathways, ontologies, genes, etc) from mongodb
        and add to entrez_list 
        */
        HashMap <String, ArrayList <GeneGroup>> entrez_group_map = new HashMap <String, ArrayList <GeneGroup>>();
        ArrayList<String> entrez_list = new ArrayList<String>();
        for (int i = 0; i < searches.length; i++) {
            ArrayList<String> temp = getGroupEntrez(analysis.searcher, searches[i]._id, searches[i].mapGeneGroupCodeToType());
            entrez_list.addAll(temp);
            GeneGroup gene_group = new GeneGroup(searches[i].mapGeneGroupCodeToType(), searches[i]._id, searches[i].display_tag);
            for (int j = 0; j < temp.size(); j++) {
                String entrez = temp.get(j);
                if (entrez_group_map.containsKey(entrez)) {
                    entrez_group_map.get(temp.get(j)).add(gene_group);
                } else {
                    ArrayList <GeneGroup> gg  = new ArrayList <GeneGroup> ();
                    gg.add(gene_group);
                    entrez_group_map.put(temp.get(j), gg);
                }
            }
        }
        
        if (has_miRNA_Data) {
            for (int i = 0; i < searches.length; i++) {
                ArrayList<String> temp = get_miRNA_GroupEntrez(analysis.searcher, searches[i]._id, searches[i].mapGeneGroupCodeToType());
                entrez_list.addAll(temp);
                GeneGroup gene_group = new GeneGroup(searches[i].mapGeneGroupCodeToType(), searches[i]._id, searches[i].display_tag);
                for (int j = 0; j < temp.size(); j++) {
                    String entrez = temp.get(j);
                    if (entrez_group_map.containsKey(entrez)) {
                        entrez_group_map.get(temp.get(j)).add(gene_group);
                    } else {
                        ArrayList <GeneGroup> gg  = new ArrayList <GeneGroup> ();
                        gg.add(gene_group);
                        entrez_group_map.put(temp.get(j), gg);
                    }
                }
            }
        }
        
        /*
        Add selected network neighbors to entrez_list and entrez_group_map
        */
        ArrayList <String> network_neighbor_ids = new ArrayList(analysis.data_selection_state.network_neighbors.keySet());
        for (int i = 0; i < network_neighbor_ids.size(); i++) {
            NetworkNeighbor NN = analysis.data_selection_state.network_neighbors.get(network_neighbor_ids.get(i));
            GeneGroup gene_group = new GeneGroup("entrez", NN.network_type, NN.query_entrez);
            if (!entrez_list.contains(NN.query_entrez)) {
                entrez_list.add(NN.query_entrez);
                if (entrez_group_map.containsKey(NN.query_entrez)) {
                    entrez_group_map.get(NN.query_entrez).add(gene_group);
                } else {
                    ArrayList <GeneGroup> gg  = new ArrayList <GeneGroup> ();
                    gg.add(gene_group);
                    entrez_group_map.put(NN.query_entrez, gg);
                }
            }
            for (int j=0; j<NN.neighbor_entrez_list.length; j++) {
                if (!entrez_list.contains(NN.neighbor_entrez_list[j])) {
                    entrez_list.add(NN.neighbor_entrez_list[j]);
                    if (entrez_group_map.containsKey(NN.neighbor_entrez_list[j])) {
                        entrez_group_map.get(NN.neighbor_entrez_list[j]).add(gene_group);
                    } else {
                        ArrayList <GeneGroup> gg  = new ArrayList <GeneGroup> ();
                        gg.add(gene_group);
                        entrez_group_map.put(NN.neighbor_entrez_list[j], gg);
                    }
                }
            }
        }
        
        /*
        Find intersection between entrez_list and user provided data files
        and add to filtered_entrez_list
        */
        HashMap <String, Boolean> filtered_entrezs = new HashMap <String, Boolean> ();
        for (int i = 0; i < entrez_list.size(); i++) {
            if (metadata.entrezMaster.containsKey(entrez_list.get(i))) {
                filtered_entrezs.put(entrez_list.get(i), Boolean.TRUE);
            }
        }
        ArrayList<String> filtered_entrez_list = new ArrayList<String>();
        for (String key : filtered_entrezs.keySet()) {
            filtered_entrez_list.add(key);
        }
        
        prepareFilteredSortedData(analysis, identifier, gene_group_color_palette, filtered_entrez_list, entrez_group_map);
        
    }
    
    public void prepareFeatureListDataForGroup(
            String feature_list_name,
            AnalysisContainer analysis,
            String identifier,
            ColorPalette gene_group_color_palette
    ) throws MultiSlideException {
        
        if (!analysis.lists.hasFeatureList(feature_list_name)) {
            throw new MultiSlideException("There is no such feature list with name: '" + feature_list_name + "'");
        }
        
        ArrayList <String[]> feat_list = analysis.lists.featureListMap.get(feature_list_name);
        ArrayList <String> filtered_entrez_list = new ArrayList <String> ();
        HashMap <String, ArrayList <GeneGroup>> entrez_group_map = new HashMap <String, ArrayList <GeneGroup>> ();
        HashMap <String, Boolean> filtered_entrezs = new HashMap <String, Boolean> ();
        
        for (String[] entry : feat_list) {
            String entrez = entry[0];
            String group = entry[1];
            GeneGroup gene_group = new GeneGroup("entrez", group, group);
            filtered_entrezs.put(entrez, Boolean.TRUE);
            if (entrez_group_map.containsKey(entrez)) {
                entrez_group_map.get(entrez).add(gene_group);
            } else {
                ArrayList <GeneGroup> gg  = new ArrayList <GeneGroup> ();
                gg.add(gene_group);
                entrez_group_map.put(entrez, gg);
            }
        }
        
        for (String key : filtered_entrezs.keySet()) {
            filtered_entrez_list.add(key);
        }
        
        prepareFilteredSortedData(analysis, identifier, gene_group_color_palette, filtered_entrez_list, entrez_group_map);
    }
    
    public void prepareFilteredSortedData(
            AnalysisContainer analysis,
            String identifier,
            ColorPalette gene_group_color_palette,
            ArrayList<String> filtered_entrez_list,
            HashMap <String, ArrayList <GeneGroup>> entrez_group_map
    ) throws MultiSlideException {
        
        String[] selected_phenotypes = analysis.data_selection_state.getSelectedPhenotypes();
        
        /*
        Compute size of filtered sorted data
        */
        int[] nRowsPerEntrez = new int[filtered_entrez_list.size()];
        int nSelectedDatasets = analysis.data_selection_state.datasets.length;
        for (int dataset_index = 0; dataset_index < nSelectedDatasets; dataset_index++) {
            for (int i = 0; i < filtered_entrez_list.size(); i++) {
                nRowsPerEntrez[i] = Math.max(nRowsPerEntrez[i], this.getEntrezCounts(dataset_index, filtered_entrez_list.get(i)));
            }
        }
        
        int nEntrez = 0;
        for (int i=0; i<nRowsPerEntrez.length; i++) {
            nEntrez += nRowsPerEntrez[i];
        }
        
        ArrayList <String> entrez_disp_list = new ArrayList <String> ();
        for (int i=0; i<nRowsPerEntrez.length; i++) {
            for (int j=0; j<nRowsPerEntrez[i]; j++) {
                entrez_disp_list.add(filtered_entrez_list.get(i));
            }
        }
        
        /*
        Populate filtered sorted data (fs_data)
        */
        fs_data = new FilteredSortedData(
                nEntrez, 
                sample_ids.length, 
                entrez_disp_list, 
                analysis.data_selection_state.datasets,
                entrez_group_map
        );
        for (int dataset_index=0; dataset_index<dataset_names.length; dataset_index++) {

            String dataset_name = dataset_names[dataset_index];
            if (analysis.data_selection_state.isDatasetSelected(dataset_name)) {
                
                Float[][] expression_data = expressions.get(dataset_index);
                Float[][] filtered_data = new Float[sample_ids.length][nEntrez];

                HashMap <String, ArrayList<Integer>> entrezPosMap_i = metadata.entrezPosMaps.get(dataset_name);
                
                String metadata_column_name;
                if (dataset_specs[dataset_index].omics_type.equals("mi_rna")) {
                    metadata_column_name = dataset_specs[dataset_index].getMappedColname("mirna_id_2021158607524066");
                } else {
                    metadata_column_name = dataset_specs[dataset_index].getMappedColname(identifier);
                }
                
                String[] col_values = metadata.values.get(dataset_name).get(metadata_column_name.toUpperCase());
                
                ArrayList <String> entrez = new ArrayList <String> ();
                String[] column_headers = new String[nEntrez];

                int entrez_source_index;
                int col_count = 0;
                String entrez_i;
                
                for (int i=0; i<filtered_entrez_list.size(); i++) {
                    System.out.println(filtered_entrez_list.get(i));
                }
                
                for (int i = 0; i < filtered_entrez_list.size(); i++) {
                    int N = 0;
                    entrez_i = filtered_entrez_list.get(i);
                    if (entrezPosMap_i.containsKey(entrez_i)) {
                        ArrayList <Integer> positions = entrezPosMap_i.get(entrez_i);
                        if (positions != null) {
                            N = positions.size();
                        }
                        for (int j = 0; j < N; j++) {
                            entrez.add(entrez_i);
                            entrez_source_index = positions.get(j);
                            column_headers[col_count] = col_values[entrez_source_index];
                            for (int s = 0; s < sample_ids.length; s++) {
                                filtered_data[s][col_count] = expression_data[s][entrez_source_index];
                            }
                            col_count++;
                        }
                        for (int j = N; j < nRowsPerEntrez[i]; j++) {
                            entrez.add(entrez_i);
                            for (int s = 0; s < sample_ids.length; s++) {
                                filtered_data[s][col_count] = Float.NaN;
                            }
                            col_count++;
                        }
                    } else {
                        for (int j = 0; j < nRowsPerEntrez[i]; j++) {
                            entrez.add(entrez_i);
                            for (int s = 0; s < sample_ids.length; s++) {
                                filtered_data[s][col_count] = Float.NaN;
                            }
                            col_count++;
                        }
                    }
                }
                
                if (dataset_specs[dataset_index].omics_type.equals("mi_rna")) {
                    for (int i=0; i<column_headers.length; i++) {
                        if (column_headers[i] != null) {
                            column_headers[i] = column_headers[i].toLowerCase();
                        }
                    }
                }

                fs_data.addExpressions(filtered_data, dataset_name);
                fs_data.addColumnHeaders(column_headers, dataset_name);
                fs_data.addRowNames(sample_ids, dataset_name);
            }
        }
        
        fs_data.consolidateColumnHeaders();
        
        for (int p = 0; p < selected_phenotypes.length; p++) {
            String[] phenotypes = new String[sample_ids.length];
            for (int s = 0; s < sample_ids.length; s++) {
                phenotypes[s] = this.clinical_info.getPhenotypeValue(sample_ids[s], selected_phenotypes[p]);
            }
            fs_data.addPhenotypes(phenotypes, selected_phenotypes[p]);
        }

        fs_data.computeDataRanges();
        fs_data.generateGeneGroupColors(gene_group_color_palette);
        
        fs_data.recomputeColOrdering(analysis);
        fs_data.recomputeRowOrdering(analysis);
    }
    
    private ArrayList <String> getGroupEntrez(Searcher searcher, String group_name, String queryType) {
        
        ArrayList <String> entrez_list = new ArrayList <String> ();
        
        if (queryType.equals("entrez")) {
            ArrayList<GeneObject> genes = searcher.processGeneQuery(group_name, "exact", queryType);
            for (int i = 0; i < genes.size(); i++) {
                GeneObject gene = genes.get(i);
                entrez_list.add(gene.entrez_id);
            }
        } else if (queryType.equals("pathid")) {
            ArrayList<PathwayObject> part_paths = searcher.processPathQuery(group_name, "exact", queryType);
            for (int i = 0; i < part_paths.size(); i++) {
                PathwayObject path = part_paths.get(i);
                entrez_list.addAll(path.entrez_ids);
            }
        } else if (queryType.equals("goid")) {
            ArrayList<GoObject> part_go_terms = searcher.processGOQuery(group_name, "exact", queryType);
            for (int i = 0; i < part_go_terms.size(); i++) {
                GoObject go = part_go_terms.get(i);
                entrez_list.addAll(go.entrez_ids);
            }
        }
        return entrez_list;
    }
    
    private ArrayList <String> get_miRNA_GroupEntrez(Searcher searcher, String group_name, String queryType) {
        
        ArrayList <String> entrez_list = new ArrayList <String> ();
        
        if (queryType.equals("pathid")) {
            ArrayList<PathwayObject> part_paths = searcher.processmiRNAPathQuery(group_name, "exact", queryType);
            for (int i = 0; i < part_paths.size(); i++) {
                PathwayObject path = part_paths.get(i);
                entrez_list.addAll(path.entrez_ids);
            }
        } else if (queryType.equals("goid")) {
            ArrayList<GoObject> part_go_terms = searcher.processmiRNAGOQuery(group_name, "exact", queryType);
            for (int i = 0; i < part_go_terms.size(); i++) {
                GoObject go = part_go_terms.get(i);
                entrez_list.addAll(go.entrez_ids);
            }
        }
        return entrez_list;
    }
    
    private ArrayList <String[][]> alignSamples(ArrayList <String[][]> raw_datasets, ClinicalInformation clinical_info) {
        
        HashMap <String, Integer> sampleIdMaster = new HashMap <String, Integer> ();
        for (int dataset_index=0; dataset_index<raw_datasets.size(); dataset_index++) {
            String[][] raw_data = raw_datasets.get(dataset_index);
            for (int col_index=0; col_index<raw_data[0].length; col_index++) {
                if (clinical_info.samplePhenotypeMap.containsKey(raw_data[0][col_index])) {
                    sampleIdMaster.put(raw_data[0][col_index], col_index);
                }
            }
        }
        
        missing_rows_indicator = new ArrayList <boolean[]> ();
        ArrayList <String[][]> aligned_datasets = new ArrayList <String[][]> ();
        
        for (int i=0; i<raw_datasets.size(); i++) {
            String[][] raw_data = raw_datasets.get(i);
            String[][] temp = new String[raw_data.length][sampleIdMaster.size()];
            
            HashMap <String, Integer> sample_ids_i = new HashMap <String, Integer> ();
            for (int j=0; j<raw_data[0].length; j++) {
                sample_ids_i.put(raw_data[0][j], j);
            }
            
            for (int j=0; j<0; j++) {
                for (int k=0; k<raw_data.length; k++) {
                    temp[k][j] = raw_data[k][j];
                }
            }
            
            sample_ids = new String[sampleIdMaster.size()];
            boolean[] missing_rows_ind_i = new boolean[sampleIdMaster.size()];
            int count = 0;
            for (Map.Entry pair : sampleIdMaster.entrySet()) {
                if (sample_ids_i.containsKey((String)pair.getKey())) {
                    int col_id = sample_ids_i.get((String)pair.getKey());
                    for (int k=0; k<raw_data.length; k++) {
                        temp[k][count] = raw_data[k][col_id];
                    }
                    missing_rows_ind_i[count] = false;
                } else {
                    for (int k=0; k<raw_data.length; k++) {
                        temp[k][count] = "NA";
                    }
                    missing_rows_ind_i[count] = true;
                }
                sample_ids[count] = (String)pair.getKey();
                count++;
            }
            missing_rows_indicator.add(missing_rows_ind_i);
            aligned_datasets.add(temp);
        }
        
        return aligned_datasets;
    }
    
    private String[][] loadData(String filename, String delimiter) {
        
        String[][] input_data = null;
        
        try {
            input_data = FileHandler.loadDelimData(filename, delimiter, false);
            
        } catch(Exception e){
            System.out.println("Error reading input data:");
            System.out.println(e);
            //throw new DataParsingException("Unknown error while reading data file. If the problem persists please report an issue.");
        }
        
        return input_data;
    }
    
    private static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {
        @Override
        public int compare(String str1, String str2) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0) {
                res = str1.compareTo(str2);
            }
            return res;
        }
    };
    
    
}
