/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.beans.data.SearchResults;
import org.cssblab.multislide.searcher.GeneObject;
import static org.cssblab.multislide.searcher.SearchHandler.doSearch;
import org.cssblab.multislide.searcher.SearchResultObject;
import org.cssblab.multislide.searcher.Searcher;

/**
 *
 * @author Soumita
 */
public class MetaData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public HashMap <String, HashMap <String, String[]>> values;
    public HashMap <String, HashMap <String, ArrayList<Integer>>> entrezPosMaps;
    public HashMap <String, HashMap <Integer, String>> positionEntrezMaps;
    public HashMap <String, boolean[]> metadata_col_ind;
    public HashMap <String, Boolean> entrezMaster;
    public HashMap <String, Integer> entrezColIndices;
    
    MetaData() {
        
        values = new HashMap <String, HashMap <String, String[]>> ();
        entrezPosMaps = new HashMap <String, HashMap <String, ArrayList<Integer>>> ();
        positionEntrezMaps = new HashMap <String, HashMap <Integer, String>> ();
        metadata_col_ind = new HashMap <String, boolean[]> ();
        entrezMaster = new HashMap <String, Boolean> ();
        entrezColIndices = new HashMap <String, Integer> ();
    }
    
    public String[] getMetaDataColumnNames(String dataset_name) {
        HashMap <String, String[]> dataset_metadata = this.values.get(dataset_name);
        String[] a = new String[dataset_metadata.keySet().size()];
        dataset_metadata.keySet().toArray(a);
        return a;
    }

    
    public void extract(
            String[] dataset_names, 
            ArrayList <String[][]> raw_datasets, 
            DatasetSpecs[] dataset_specs, 
            Searcher searcher, 
            HashMap <String, Integer> identifier_index_map
    ) throws MultiSlideException {
        for (int i=0; i<raw_datasets.size(); i++) {
            this.extract(dataset_names[i], raw_datasets.get(i), dataset_specs[i]);
            HashMap <String, ArrayList<Integer>> entrezPosMap_i = this.makeEntrezPositionMap (dataset_names[i], raw_datasets.get(i), dataset_specs[i], searcher, identifier_index_map);
            entrezPosMaps.put(dataset_names[i], entrezPosMap_i);
            for (String entrez : entrezPosMap_i.keySet()) {
                entrezMaster.put(entrez, Boolean.TRUE);
            }
            positionEntrezMaps.put(dataset_names[i], reverseMap(entrezPosMap_i));
        }
    }
    
    public void extract(String dataset_name, String[][] raw_data, DatasetSpecs specs) {
        
        raw_data = trimAndLowerCase(raw_data);
        
        HashMap <String, String[]> values_i = new HashMap <String, String[]> ();
        boolean[] metadata_col_ind_i = new boolean[raw_data[0].length];
        int entrez_col_index = -1;
        String entrez_colname = specs.getMappedColname("entrez_2021158607524066");
        if (entrez_colname != null) {
            entrez_colname = entrez_colname.toUpperCase();
        }

        for (int col = 0; col < raw_data[0].length; col++) {
            metadata_col_ind_i[col] = false;
            for (int i = 0; i < specs.metadata_columns.length; i++) {
                if (raw_data[0][col].equals(specs.metadata_columns[i].toUpperCase())) {
                    metadata_col_ind_i[col] = true;
                    break;
                }
            }
            
            if (metadata_col_ind_i[col]) {
                String[] metadat_col_i_values = new String[raw_data.length-1];
                for (int row=1; row<raw_data.length; row++) {
                    metadat_col_i_values[row-1] = raw_data[row][col];
                }
                values_i.put(raw_data[0][col], metadat_col_i_values);
                
                if (raw_data[0][col].equals(entrez_colname)) {
                    entrez_col_index = col;
                }
            }
        }
        
        /*
        for (int row=1; row<raw_data.length; row++) {
            String entrez_id = raw_data[row][entrez_col_index];
            this.entrezMaster.put(entrez_id, true);
            entrezPosMap_i.put(entrez_id, row-1);
        }
        */
        
        values.put(dataset_name, values_i);
        //metadata_colname_pos_map.put(dataset_name, metadata_colname_pos_map_i);
        //entrezPosMaps.put(dataset_name, entrezPosMap_i);
        metadata_col_ind.put(dataset_name, metadata_col_ind_i);
        entrezColIndices.put(dataset_name, entrez_col_index);
        
    }
    
    private void addToEntrezPositionMap(HashMap <String, ArrayList<Integer>> entrezPosMap_i, String entrez, int position) {
        if (entrezPosMap_i.containsKey(entrez)) {
            entrezPosMap_i.get(entrez).add(position);
        } else {
            ArrayList <Integer> t = new ArrayList <Integer> ();
            t.add(position);
            entrezPosMap_i.put(entrez, t);
        }
    }
    
    public HashMap <String, ArrayList<Integer>> makeEntrezPositionMap (
            String dataset_name, 
            String[][] raw_data, 
            DatasetSpecs specs, 
            Searcher searcher, 
            HashMap <String, Integer> identifier_index_map
    ) throws MultiSlideException {
        if (specs.omics_type.equals("mi_rna")) {
            if (!specs.identifier_metadata_column_mappings.containsKey("mirna_id_2021158607524066")) {
                throw new MultiSlideException("Dataset with omics type microRNA expression must have miRNA IDs.");
            }
            String mirna_id_colname = specs.identifier_metadata_column_mappings.get("mirna_id_2021158607524066").toUpperCase();
            String[] mirna_ids = values.get(dataset_name).get(mirna_id_colname);
            HashMap <String, ArrayList<Integer>> entrezPosMap_i = new HashMap <String, ArrayList<Integer>>();
            for (int row=1; row<raw_data.length; row++) {
                addToEntrezPositionMap(entrezPosMap_i, mirna_ids[row-1], row-1);
            }
            return entrezPosMap_i;
        }
        int entrez_col_index = entrezColIndices.get(dataset_name);
        if (entrez_col_index == -1) {
            HashMap <String, ArrayList<Integer>> entrezPosMap_i = new HashMap <String, ArrayList<Integer>>();
            if (specs.identifier_metadata_column_mappings.size() > 0) {
                String[] metadata_colnames = new String[specs.identifier_metadata_column_mappings.size()];
                String[] metadata_col_identifiers = new String[specs.identifier_metadata_column_mappings.size()];
                int i = 0;
                for (Map.Entry <String, String> entry : specs.identifier_metadata_column_mappings.entrySet()) {
                    metadata_col_identifiers[i] = entry.getKey();
                    metadata_colnames[i] = entry.getValue().toUpperCase();
                    i++;
                }
                HashMap <String, String[]> metadata_col_values = values.get(dataset_name);
                boolean[] entrezFound = new boolean[raw_data.length-1];
                for (i=0; i<metadata_colnames.length; i++) {
                    int metadata_index = identifier_index_map.get(metadata_col_identifiers[i]);
                    String[] metdata_values = metadata_col_values.get(metadata_colnames[i]);
                    for (int row=1; row<raw_data.length; row++) {
                        if (!entrezFound[row-1]) {
                            ArrayList <String> entrezs = searcher.getEntrezFromDB(metdata_values[row-1], metadata_index);
                            if (entrezs != null) {
                                entrezFound[row-1] = true;
                                for (int e=0; e<entrezs.size(); e++) {
                                    addToEntrezPositionMap(entrezPosMap_i, entrezs.get(e), row-1);
                                }
                            }
                        }
                    }
                }
                i = -1;
                for (int row=1; row<raw_data.length; row++) {
                    if (!entrezFound[row-1]) {
                        addToEntrezPositionMap(entrezPosMap_i, i+"", row-1);
                        i--;
                    }
                }
            } else {
                int c = -1;
                for (int row=1; row<raw_data.length; row++) {
                    //this.entrezMaster.put(c+"", true);
                    addToEntrezPositionMap(entrezPosMap_i, c+"", row-1);
                    c--;
                }
            }
            return entrezPosMap_i;
        } else {
            HashMap <String, ArrayList<Integer>> entrezPosMap_i = new HashMap <String, ArrayList<Integer>>();
            for (int row=1; row<raw_data.length; row++) {
                String entrez_id = raw_data[row][entrez_col_index];
                //this.entrezMaster.put(entrez_id, true);
                addToEntrezPositionMap(entrezPosMap_i, entrez_id, row-1);
            }
            return entrezPosMap_i;
        }
    }
    
    public ArrayList <String[][]> getNonMetaDataColumns(String[] dataset_names, ArrayList <String[][]> raw_datasets) {
        ArrayList <String[][]> data = new ArrayList <String[][]> ();
        for (int i=0; i<raw_datasets.size(); i++) {
            data.add(getNonMetaDataColumns(dataset_names[i], raw_datasets.get(i)));
        }
        return data;
    }
    
    public String[][] getNonMetaDataColumns(String dataset_name, String[][] raw_data) {
        String[][] data = new String[raw_data.length][raw_data[0].length-this.values.get(dataset_name).size()];
        boolean[] metadata_column_ind = metadata_col_ind.get(dataset_name);
        int col_index = 0;
        for (int col = 0; col < raw_data[0].length; col++) {
            if (!metadata_column_ind[col]) {
                for (int row=0; row<raw_data.length; row++) {
                    data[row][col_index] = raw_data[row][col];
                }
                col_index++;
            }
        }
        return data;
    }
    
    /*
    public HashMap <String, String> getIdentifiers(String dataset_name, String metadata_column_name, ArrayList <String> entrez) {
        String[] col_values = values.get(dataset_name).get(metadata_column_name.toUpperCase());
        HashMap <String, ArrayList<Integer>> entrezPosMap_i = entrezPosMaps.get(dataset_name);
        HashMap <String, String> identifiers = new HashMap <String, String> ();
        for (int i=0; i<entrez.size(); i++) {
            System.out.println(entrez.get(i) + ":" + i);
            if(!entrezPosMap_i.containsKey(entrez.get(i))){
                identifiers.put(entrez.get(i), "-");
            } else {
                int row_index = entrezPosMap_i.get(entrez.get(i));
                //System.out.println(entrez.get(i) + ":" + row_index);
                identifiers.put(entrez.get(i), col_values[row_index]);
            }
        }
        return identifiers;
    }
    */
    
    public String[][] trimAndLowerCase(String[][] raw_data) {
        String[][] cleaned_data = new String[raw_data.length][raw_data[0].length];
        for (int i=0; i<raw_data.length; i++) {
            for (int j=0; j<raw_data[0].length; j++) {
                cleaned_data[i][j] = raw_data[i][j].trim().toUpperCase();
            }
        }
        return cleaned_data;
    }
    
    public ArrayList <SearchResultObject> processMetaColQuery(String query) throws MultiSlideException{
        if(query.contains(":")) { // wildcard
            
            String[] parts = query.split(":", -1); 
            String keyword = parts[0].trim().toUpperCase();
            keyword = keyword.substring(1, keyword.length()-1);
            String search_type = "contains";
            String search_term = parts[1].trim().toUpperCase();
            return doMetadataColSearch(keyword, search_type, search_term);
            
        } else if(query.contains("=")) { //exact
            
            String[] parts = query.split("=", -1); 
            String keyword = parts[0].trim().toUpperCase();
            keyword = keyword.substring(1, keyword.length()-1);
            String search_type = "exact";
            String search_term = parts[1].trim().toUpperCase();
            return doMetadataColSearch(keyword, search_type, search_term);
            
        } else {
            throw new MultiSlideException("Metadata column search query must contain a keyword and either an '=' or a ':'");
        }
    }


    public ArrayList <SearchResultObject> doMetadataColSearch(String metadata_column_name, String search_type, String search_term) {
        
        ArrayList <SearchResultObject> current_search_results = new ArrayList <SearchResultObject>();
        HashMap <String, Boolean> selected_entrezs = new HashMap <String, Boolean> ();
        String entrez = "";
        
        if (search_type.equals("exact")) {
            StringTokenizer st = new StringTokenizer(search_term, ",");
            while (st.hasMoreTokens()) {
                String query = st.nextToken();
                query = query.trim().toUpperCase();
                for (Map.Entry <String, HashMap<String,String[]>> entry : values.entrySet()) {
                    String dataset_name = entry.getKey();
                    HashMap <Integer, String> positionEntrezMap = this.positionEntrezMaps.get(dataset_name);
                    String[] metadata_values = entry.getValue().get(metadata_column_name.toUpperCase());
                    for (int i=0; i<metadata_values.length; i++) {
                        entrez = positionEntrezMap.get(i);
                        if (!selected_entrezs.containsKey(entrez)) {
                            if (metadata_values[i].equals(query)) {
                                GeneObject GO = GeneObject.createMetacolGeneObject(entrez, metadata_values[i], metadata_column_name);
                                current_search_results.add(SearchResultObject.makeSearchResultObject(query, GO));
                                selected_entrezs.put(entrez, Boolean.TRUE);
                            }
                        }
                    }
                }
            }
        } else if (search_type.equals("contains")) {
            StringTokenizer st = new StringTokenizer(search_term, ",");
            while (st.hasMoreTokens()) {
                String query = st.nextToken();
                query = query.trim().toUpperCase();
                for (Map.Entry <String, HashMap<String,String[]>> entry : values.entrySet()) {
                    String dataset_name = entry.getKey();
                    HashMap <Integer, String> positionEntrezMap = this.positionEntrezMaps.get(dataset_name);
                    String[] metadata_values = entry.getValue().get(metadata_column_name.toUpperCase());
                    for (int i=0; i<metadata_values.length; i++) {
                        entrez = positionEntrezMap.get(i);
                        if (!selected_entrezs.containsKey(entrez)) {
                            if (metadata_values[i].contains(query)) {
                                GeneObject GO = GeneObject.createMetacolGeneObject(entrez, metadata_values[i], metadata_column_name);
                                current_search_results.add(SearchResultObject.makeSearchResultObject(query, GO));
                                selected_entrezs.put(entrez, Boolean.TRUE);
                            }
                        }
                    }
                }
            }
        }
        
        return current_search_results;
    }
    
    private HashMap <Integer, String> reverseMap(HashMap <String, ArrayList<Integer>> map) {
        HashMap <Integer, String> reverse_map = new HashMap <Integer, String> ();
        for (Map.Entry <String, ArrayList<Integer>> entry : map.entrySet()) {
            String entrez = entry.getKey();
            ArrayList<Integer> positions = entry.getValue();
            for (int p=0; p<positions.size(); p++) {
                reverse_map.put(positions.get(p), entrez);
            }
        }
        return reverse_map;
    }
    
    /*
    public ArrayList <String> getNonStandardMetaColNames() {
        ArrayList <String> nonstandard_metacolnames = new ArrayList<>();
        ArrayList <String> metacolnames = new ArrayList<>(metadata_in.keySet());
        for (int i=0; i<metacolnames.size(); i++) {
            if (!identifier_index_map.containsKey(metacolnames.get(i))) {
                nonstandard_metacolnames.add(metacolnames.get(i));
            }
        }
        return nonstandard_metacolnames;
    }
    
    public boolean hasStandardMetaData() {
        return hasMappedData;
    }
    
    public void mapFeatureEntrezs( String species, 
                                   ArrayList <Feature> features) {
        
        Searcher searcher = null;
        if (this.hasMappedData) {
            searcher = new Searcher(species);
        }
        
        boolean hasEntrezData = metadata_in.containsKey("entrez_2021158607524066");
        
        String parsed_entrez;
        int bad_entrez_counter = -1;
        boolean hasIdentifiers;
        boolean usesBadEntrez;
        
        for (int i=0; i<this.nFeatures; i++) {
            
            usesBadEntrez = false;
            //ArrayList <String> db_entrezs = new ArrayList <> ();
            HashSet <String> db_entrezs = new HashSet <String> ();
            
            String entrez_in = "";
            if (hasEntrezData) {
                entrez_in = metadata_in.get("entrez_2021158607524066")[i];
            }
            
            if (!entrez_in.equals("")) {
                parsed_entrez = entrez_in; 
            } else {
               
                hasIdentifiers = false;
                HashMap <Integer, String> available_identifier_type_value_map = null;
                if (this.hasMappedData) {
                    available_identifier_type_value_map = new HashMap <> ();
                    for (int k=0; k<6; k++) {
                        String identifier_in_name = identifier_name_map.get(k+1);
                        if (metadata_in.containsKey(identifier_in_name)) {
                            String identifier_in_value = metadata_in.get(identifier_in_name)[i];
                            if (!identifier_in_value.equals("")) {
                                hasIdentifiers = true;
                                available_identifier_type_value_map.put(k+1, identifier_in_value);
                            }
                        }
                    }
                }
                
                if(hasIdentifiers) {
                    
                    for (Map.Entry<Integer, String> entry : available_identifier_type_value_map.entrySet()) {
                        db_entrezs.addAll(searcher.getEntrezFromDB(entry.getValue(), entry.getKey()));
                    }
                    
                    if (db_entrezs.size() > 0) {
                        parsed_entrez = getConsensusValue(db_entrezs);
                    } else {
                        parsed_entrez = "" + bad_entrez_counter;
                        usesBadEntrez = true;
                        bad_entrez_counter--;
                    }
                    
                } else {
                    parsed_entrez = "" + bad_entrez_counter;
                    usesBadEntrez = true;
                    bad_entrez_counter--;
                }
            }
            
            Feature f = new Feature();
            f.setEntrezs(parsed_entrez, usesBadEntrez, db_entrezs);
            features.add(f);
        }
        
    }
    
    public HashMap <String, ArrayList <String>> mapFeatureIdentifiers (String species, 
                                                                       String identifier_name,
                                                                       ArrayList <Feature> features) {
        
        HashMap <String, ArrayList <String>> entrezIdentifierMap = new HashMap <> ();
        
        Searcher searcher = null;
        if (this.hasMappedData) {
            searcher = new Searcher(species);
        }
        
        boolean hasIdentifierData = metadata_in.containsKey(identifier_name);
        
        String parsed_identifier;
        int bad_identifier_counter = 0;
        ArrayList <String> db_identifiers = null;
        
        for (int i=0; i<this.nFeatures; i++) {
            
            db_identifiers = new ArrayList <> ();
            
            String identifier_in = "";
            if (hasIdentifierData) {
                identifier_in = metadata_in.get(identifier_name)[i];
            }
            
            if (!identifier_in.equals("")) {
                
                parsed_identifier = identifier_in;
                
            } else {
                
                if (this.hasMappedData) {
                    db_identifiers = searcher.getIdentifiersFromDB(features.get(i).entrez, identifier_index_map.get(identifier_name));
                }
                
                if (db_identifiers.size() > 0) {
                    parsed_identifier = db_identifiers.get(0);
                } else {
                    parsed_identifier = "Unknown_" + bad_identifier_counter;
                    bad_identifier_counter++;
                }
            }
            
            if (!(db_identifiers.contains(parsed_identifier))) {
                db_identifiers.add(0, parsed_identifier);
            }
            
            features.get(i).identifier = parsed_identifier;
            features.get(i).identifier_aliases = db_identifiers;
            
            entrezIdentifierMap.put(features.get(i).entrez, db_identifiers);
        }
        
        return entrezIdentifierMap;
    }
    */
    
}
