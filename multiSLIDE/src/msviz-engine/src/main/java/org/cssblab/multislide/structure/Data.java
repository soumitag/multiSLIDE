package org.cssblab.multislide.structure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import org.cssblab.multislide.algorithms.clustering.BinaryTree;
import org.cssblab.multislide.beans.data.DatasetSpecs;
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

public class Data {
    
    public int nDatasets;
    public int nGenes_1;
    public int nSamples_1;
    
    public DatasetSpecs[] dataset_specs;
    public ClinicalInformation clinical_info;
    public String[] dataset_names;
    public ArrayList <Float[][]> expressions;
    public MetaData metadata;
    public HashMap <String, Integer> samplePosMap;
    public HashMap <String, Integer> entrezSortPositionMap;
    public HashMap <String, Integer> datasetIndexMap;
    public String[] sample_ids;
    
    public FilteredSortedData fs_data;
    
    public Data(
            String analysis_basepath, 
            HashMap <String, DatasetSpecs> dataset_specs_map
    ) throws MultiSlideException {
        
        this.nDatasets = dataset_specs_map.size()-1;
        this.dataset_names = new String[dataset_specs_map.size()-1];
        clinical_info = new ClinicalInformation();

        int nSamplesInClinicalInfo = 0;
        Iterator it = dataset_specs_map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (spec.omics_type.equals("clinical-info")) {
                String filepath = analysis_basepath + File.separator + spec.filename_within_analysis_folder;
                nSamplesInClinicalInfo = clinical_info.loadClinicalInformation(filepath, FormElementMapper.parseDelimiter(spec.delimiter));
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
        metadata.extract(dataset_names, raw_datasets, dataset_specs);
        ArrayList <String[][]> raw_data = metadata.getNonMetaDataColumns(dataset_names, raw_datasets);
        
        expressions = new ArrayList <Float[][]> ();
        //entrezSymbolMap = new HashMap <String, HashMap <String, String>> ();
        //entrezPosMap = new HashMap <String, Integer> ();
        samplePosMap = new HashMap <String, Integer> ();
        ArrayList <String[][]> aligned_datasets = alignSamples(raw_data);
        
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
        
        this.fs_data = new FilteredSortedData();
        
    }
    
    /*
    public Data(String clinal_info_filename, String[] dataset_names, String[] data_filenames, HashMap <String, DatasetSpecs> dataset_specs_map) {
        
        this.dataset_names = dataset_names;
        
        clinical_info = new ClinicalInformation();
        nSamples = clinical_info.loadClinicalInformation(clinal_info_filename, "\t");
        
        nDatasets = dataset_names.length;
        ArrayList <String[][]> raw_datasets = new ArrayList <String[][]> ();
        for (int i=0; i<data_filenames.length; i++) {
            raw_datasets.add(loadData(data_filenames[i],"\t"));
        }
        
        DatasetSpecs[] specs = new DatasetSpecs[dataset_specs_map.size()-1];
        Iterator it = dataset_specs_map.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (!spec.omics_type.equals("clinical-info")) {
                specs[count++] = spec;
            }
        }
        
        expressions = new HashMap <String, Float[][]> ();
        entrezSymbolMap = new HashMap <String, HashMap <String, String>> ();
        entrezPosMap = new HashMap <String, Integer> ();
        samplePosMap = new HashMap <String, Integer> ();
        ArrayList <String[][]> aligned_datasets = alignSamples(raw_datasets, 3);
        for (int i=0; i<data_filenames.length; i++) {
            expressions.put(
                    dataset_names[i], 
                    loadDataset(
                            aligned_datasets.get(i), 
                            dataset_names[i],
                            specs[i].metadata_columns, 
                            specs[i].getMappedColname("entrez_2021158607524066"),
                            specs[i].getMappedColname("genesymbol_2021158607524066")
                    )
            );
        }
        
        this.fs_data = new FilteredSortedData();
    }
    */
    
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
    
    public void makeSampleSortOrder (
            String sortBy, 
            String[] selected_phenotypes, 
            AnalysisContainer analysis, 
            ClusteringParams row_clustering_params, 
            Float[][] data
    ) throws MultiSlideException {
        
        if (sortBy.equalsIgnoreCase("No_Sort")) {
            if (row_clustering_params.isActive) {
                
                BinaryTree linkage_tree = analysis.clusterer.doClustering(
                        data,
                        analysis, 
                        row_clustering_params.dataset_name, 
                        row_clustering_params.type, 
                        row_clustering_params.linkage_function, 
                        row_clustering_params.distance_function, 
                        row_clustering_params.leaf_ordering
                );
                
                for (int i = 0 ; i < linkage_tree.leaf_ordering.size(); i++) {
                    int index = linkage_tree.leaf_ordering.get(i);
                    samplePosMap.put(sample_ids[i], index);
                }
                
            } else {
                List<PhenotypeSet> phensets = new ArrayList<PhenotypeSet>();

                for (int s=0; s<sample_ids.length; s++) {
                    String[] phenset = new String[selected_phenotypes.length];
                    for (int p=0; p<selected_phenotypes.length; p++) {
                        phenset[p] = this.clinical_info.getPhenotypeValue(sample_ids[s], selected_phenotypes[p]);
                    }
                    phensets.add(new PhenotypeSet(s, phenset));
                }
                Collections.sort(phensets, new PhenotypeComparator());
                for (int s=0; s<sample_ids.length; s++) {
                    samplePosMap.put(sample_ids[phensets.get(s).index], s);
                }
            }
        } else {
            HashMap <String, Boolean> phenotype_values = new HashMap <String, Boolean>();
            for (int s=0; s<sample_ids.length; s++) {
                String phenotype = this.clinical_info.getPhenotypeValue(sample_ids[s], sortBy);
                phenotype_values.put(phenotype, Boolean.TRUE);
            }
            ArrayList <String> phenotype_values_list = new ArrayList <String> (phenotype_values.keySet());
            Collections.sort(phenotype_values_list, ALPHABETICAL_ORDER);
            int index = 0;
            for (int i=0; i<phenotype_values_list.size(); i++) {
                for (int s=0; s<sample_ids.length; s++) {
                    String phenotype = this.clinical_info.getPhenotypeValue(sample_ids[s], sortBy);
                    if(phenotype.equalsIgnoreCase(phenotype_values_list.get(i))) {
                        samplePosMap.put(sample_ids[s], index++);
                    }
                }
            }
        }
    }
    
    public void makeFeatureSortOrder(
            String sortBy, 
            HashMap <String, GeneGroup> entrez_group_map, 
            AnalysisContainer analysis, 
            ClusteringParams col_clustering_params, 
            ArrayList<String> filtered_entrez_list,
            Float[][] data
    ) throws MultiSlideException {
        
        this.entrezSortPositionMap = new HashMap <String, Integer> ();
        
        if (sortBy.equalsIgnoreCase("No_Sort")) {
            if (col_clustering_params.isActive) {
                
                BinaryTree linkage_tree = analysis.clusterer.doClustering(
                        data,
                        analysis, 
                        col_clustering_params.dataset_name, 
                        col_clustering_params.type, 
                        col_clustering_params.linkage_function, 
                        col_clustering_params.distance_function, 
                        col_clustering_params.leaf_ordering
                );
                
                for (int i=0; i<filtered_entrez_list.size(); i++) {
                    int new_pos = linkage_tree.leaf_ordering.get(i);
                    this.entrezSortPositionMap.put(filtered_entrez_list.get(i), new_pos);
                }
                
            } else {
                // do nothing
            }
        } else if (sortBy.equalsIgnoreCase("Gene_Group")) {
            // use gene groups
            HashMap <String, Boolean> genegroup = new HashMap <String, Boolean>();
            for (int i=0; i<filtered_entrez_list.size(); i++) {
                genegroup.put(entrez_group_map.get(filtered_entrez_list.get(i)).name, Boolean.FALSE);
            }
            ArrayList <String> genegroup_list = new ArrayList <String> (genegroup.keySet());
            Collections.sort(genegroup_list, ALPHABETICAL_ORDER);
            
            int index = 0;
            for (int i=0; i<genegroup_list.size(); i++) {
                String genegroupname = genegroup_list.get(i);
                for (int s=0; s<filtered_entrez_list.size(); s++) {
                    String group_name = entrez_group_map.get(filtered_entrez_list.get(s)).name;
                    if(group_name.equalsIgnoreCase(genegroupname)) {
                        this.entrezSortPositionMap.put(filtered_entrez_list.get(s), index++);
                    }
                }
            }
        }
        
    }
    
    public String[] sortBySamples(String[] array_to_sort) {
        String[] sorted_array = new String[sample_ids.length];
        for (int s = 0; s < sample_ids.length; s++) {
            int sample_pos = samplePosMap.get(sample_ids[s]);
            sorted_array[sample_pos] = array_to_sort[s];
        }
        return sorted_array;
    }
    
    public ArrayList <String> sortByFeature(ArrayList <String> entrez) {
        ArrayList <IndexedEntrez> indexed_entrez_list = new ArrayList <IndexedEntrez> ();
        for (int i=0; i<entrez.size(); i++) {
            int new_pos = this.entrezSortPositionMap.get(entrez.get(i));
            IndexedEntrez indexed_entrez = new IndexedEntrez(new_pos, entrez.get(i));
            indexed_entrez_list.add(indexed_entrez);
        }
        Collections.sort(indexed_entrez_list, new IndexedEntrezComparator());
        
        ArrayList <String> sorted_entrez = new ArrayList <String> ();
        for (int i=0; i<indexed_entrez_list.size(); i++) {
            sorted_entrez.add(indexed_entrez_list.get(i).entrez);
        }
        return sorted_entrez;
    }
    
    public ArrayList<GeneGroup> sortByFeature(ArrayList <String> entrez_list, HashMap <String, GeneGroup> entrezGeneGroupMap) {
        
        GeneGroup[] sorted_arr = new GeneGroup[entrez_list.size()];
        for (int i=0; i<entrez_list.size(); i++) {
            GeneGroup genegroup = (GeneGroup)entrezGeneGroupMap.get(entrez_list.get(i));
            int new_pos = this.entrezSortPositionMap.get(entrez_list.get(i));
            sorted_arr[new_pos] = genegroup;
        }
        ArrayList<GeneGroup> genegroups = new ArrayList<GeneGroup>(Arrays.asList(sorted_arr));
        return genegroups;
    }
    
    public void prepareDataForGroup(
            AnalysisContainer analysis,
            String identifier,
            String sortByRow,
            String sortByCol,
            ClusteringParams row_clustering_params,
            ClusteringParams col_clustering_params
    ) throws MultiSlideException {
        
        String[] group_ids = analysis.data_selection_state.getGeneGroupNames();
        String[] queryTypes = analysis.data_selection_state.getGeneGroupTypes();
        String[] selected_phenotypes = analysis.data_selection_state.getSelectedPhenotypes();
        
        fs_data = new FilteredSortedData();
        
        /*
        Get all entrez ids for user provided gene groups (pathways, ontologies, genes, etc) from mongodb
        and add to entrez_list 
        */
        HashMap<String, GeneGroup> entrez_group_map = new HashMap<String, GeneGroup>();
        ArrayList<String> entrez_list = new ArrayList<String>();
        for (int i = 0; i < group_ids.length; i++) {
            ArrayList<String> temp = getGroupEntrez(analysis.searcher, group_ids[i], queryTypes[i]);
            entrez_list.addAll(temp);
            GeneGroup gene_group = new GeneGroup(i, queryTypes[i], group_ids[i]);
            for (int j = 0; j < temp.size(); j++) {
                entrez_group_map.put(temp.get(j), gene_group);
            }
        }

        /*
        Find intersection between entrez_list and user provided data files
        and add to filtered_entrez_list
        */
        ArrayList<String> filtered_entrez_list = new ArrayList<String>();
        for (int i = 0; i < entrez_list.size(); i++) {
            if (metadata.entrezMaster.containsKey(entrez_list.get(i))) {
                filtered_entrez_list.add(entrez_list.get(i));
            }
        }
        
        /*
        Make sample sort order
        */
        Float[][] expression_data = null;
        if (sortByRow.equalsIgnoreCase("No_Sort")) {
            if (row_clustering_params.isActive) {
                expression_data = expressions.get(this.datasetIndexMap.get(row_clustering_params.getDatasetName()));
            }
        }
        makeSampleSortOrder(sortByRow, selected_phenotypes, analysis, row_clustering_params, expression_data);
        
        /*
        Make feature sort order
        */
        expression_data = null;
        if (sortByCol.equalsIgnoreCase("No_Sort")) {
            if (col_clustering_params.isActive) {
                expression_data = expressions.get(this.datasetIndexMap.get(col_clustering_params.getDatasetName()));
            }
        }
        makeFeatureSortOrder(sortByCol, entrez_group_map, analysis, col_clustering_params, filtered_entrez_list, expression_data);
        
        ArrayList <GeneGroup> genegroups = new ArrayList <GeneGroup> (entrez_group_map.values());
        fs_data.addEntrezList(sortByFeature(filtered_entrez_list));
        fs_data.setGeneGroups(entrez_group_map);
        
        /*
        Populate filtered sorted data (fs_data)
        */
        
        for (int dataset_index=0; dataset_index<dataset_names.length; dataset_index++) {

            String dataset_name = dataset_names[dataset_index];
            if (analysis.data_selection_state.isDatasetSelected(dataset_name)) {
                
                expression_data = expressions.get(dataset_index);
                Float[][] filtered_data = new Float[sample_ids.length][filtered_entrez_list.size()];

                HashMap <String, Integer> entrezPosMap_i = metadata.entrezPosMaps.get(dataset_name);
                HashMap <String, String> entrezSymbolMap_i = metadata.getIdentifiers(dataset_name, dataset_specs[dataset_index].getMappedColname(identifier), filtered_entrez_list);

                String[] column_headers = new String[filtered_entrez_list.size()];
                for (int i = 0; i < filtered_entrez_list.size(); i++) {

                    String eid = filtered_entrez_list.get(i);
                    column_headers[i] = entrezSymbolMap_i.get(eid);

                    if (entrezPosMap_i.containsKey(eid)) {
                        Integer index = entrezPosMap_i.get(eid);
                        for (int s = 0; s < sample_ids.length; s++) {
                            int sample_pos = samplePosMap.get(sample_ids[s]);
                            filtered_data[sample_pos][i] = expression_data[s][index];
                        }
                    } else {
                        for (int s = 0; s < sample_ids.length; s++) {
                            filtered_data[s][i] = Float.NaN;
                        }
                    }
                }

                fs_data.addExpressions(filtered_data, dataset_name);
                fs_data.addColumnHeaders(column_headers, dataset_name);
                fs_data.addRowNames(sortBySamples(sample_ids), dataset_name);            

                for (int p=0; p<selected_phenotypes.length; p++) {
                    String[] phenotypes = new String[sample_ids.length];
                    for (int s=0; s<sample_ids.length; s++) {
                        phenotypes[s] = this.clinical_info.getPhenotypeValue(sample_ids[s], selected_phenotypes[p]);
                    }
                    fs_data.addPhenotypes(sortBySamples(phenotypes), selected_phenotypes[p]);
                }
                
                fs_data.nDatasets++;
            }
        }
        
        fs_data.computeDataRanges();
    }
    
    /*
    public ArrayList <String> getGroupEntrez(Searcher searcher, String[] group_ids, String[] queryTypes) {
        HashMap <String, String> entrez_group_map = new HashMap <String, String> ();
        ArrayList <String> entrez_list = new ArrayList <String> ();
        for (int i=0; i<group_ids.length; i++) {
            ArrayList <String> temp = getGroupEntrez(searcher, group_ids[i], queryTypes[i]);
            entrez_list.addAll(temp);
            for (int j=0; j<temp.size(); j++) {
                entrez_group_map.put(temp.get(j), group_ids[i]);
            }
        }
        return entrez_list;
    }
    */
    
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
    
    private ArrayList <String[][]> alignSamples(ArrayList <String[][]> raw_datasets) {
        
        HashMap <String, Integer> sampleIdMaster = new HashMap <String, Integer> ();
        for (int i=0; i<raw_datasets.size(); i++) {
            String[][] raw_data = raw_datasets.get(i);
            for (int j=0; j<raw_data[0].length; j++) {
                sampleIdMaster.put(raw_data[0][j], j);
            }
        }
        
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
            int count = 0;
            for (Map.Entry pair : sampleIdMaster.entrySet()) {
                if (sample_ids_i.containsKey((String)pair.getKey())) {
                    int col_id = sample_ids_i.get((String)pair.getKey());
                    for (int k=0; k<raw_data.length; k++) {
                        temp[k][count] = raw_data[k][col_id];
                    }
                } else {
                    for (int k=0; k<raw_data.length; k++) {
                        temp[k][count] = "NA";
                    }
                }
                sample_ids[count] = (String)pair.getKey();
                count++;
            }
            
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
