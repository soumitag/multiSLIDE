package org.cssblab.multislide.beans.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.FormElementMapper;

/**
 *
 * @author Soumita
 */
public class DatasetSpecs implements Serializable {
    
    public static final List<String> OMICS_TYPES = new ArrayList<String>() {{
        add("cnv");
        add("dna_meth");
        add("m_rna");
        add("mi_rna");
        add("protein");
        add("phosphoproteome");
        add("gene_isoform_expression");
        add("metabolome");
        add("lipidome");
    }};
    
    public static final List<String> OMICS_TYPE_DIPSLAY_NAMES = new ArrayList<String>() {{
        add("Copy Number Variation");
        add("DNA Methylation");
        add("Transcriptome");
        add("microRNA Expression");
        add("Protein");
        add("Phosphoproteome");
        add("Gene Isoform Expression");
        add("Metabolome");
        add("Lipidome");
    }};

    private static final long serialVersionUID = 1L;
    
    public String analysis_name;
    public final String unique_name;
    public final String display_name;
    public String expanded_filename;
    public final String filename_within_analysis_folder;
    
    public final String filename;    // original uploaded filename; only used when communicating messages back to user about the filename
    public final String delimiter;
    public final String omics_type;

    public String[] metadata_columns;
    public boolean has_linker;
    public boolean has_mi_rna;
    private String linker_colname;
    private String mi_rna_colname;
    public String linker_identifier_type;
    public boolean has_additional_identifiers;
    public String[] identifier_metadata_columns;

    /*
    public DatasetSpecs(String analysis_name, String folderpath) 
            throws MultiSlideException {
        this.analysis_name = analysis_name;
        this.unique_name = this.createUniqueDatasetName(this.omics_type, folderpath, "unique_name");
        this.display_name = this.createUniqueDatasetName(this.mapOmicsTypeStringToDisplayName(this.omics_type), folderpath, "display_name");
        this.filename_within_analysis_folder = this.unique_name;
        this.expanded_filename = this.analysis_name + "_" + this.unique_name;
        //this.setExpandedFilename();
        //this.setFilenameWithinAnalysisFolder();
    }
    */
    
    public DatasetSpecs(String analysis_name, String filename, String delimiter, String omics_type, String folderpath) 
            throws MultiSlideException {
        this.analysis_name = analysis_name;
        this.filename = filename;
        this.delimiter = delimiter;
        this.omics_type = omics_type;
        this.unique_name = this.createUniqueDatasetName(this.omics_type, folderpath, "unique_name");
        //this.display_name = this.createUniqueDatasetName(this.mapOmicsTypeStringToDisplayName(this.omics_type), folderpath, "display_name");
        this.display_name = this.createUniqueDatasetName(this.omics_type, folderpath, "display_name");
        this.filename_within_analysis_folder = this.unique_name;
        this.expanded_filename = this.analysis_name + "_" + this.unique_name;
        //this.setExpandedFilename();
        //this.setFilenameWithinAnalysisFolder();
        this.metadata_columns = new String[0];
        this.identifier_metadata_columns = new String[0];
        this.has_linker = false;
        this.has_additional_identifiers = false;
        
        this.has_mi_rna = false;
        this.mi_rna_colname = "";
    }
    
    public DatasetSpecs(HashMap <String, String> data, String folderpath) 
            throws MultiSlideException {
        
        if (data.containsKey("analysis_name")) {
            this.analysis_name = data.get("analysis_name");
        } else {
            throw new MultiSlideException("Analysis name is missing in call to DatasetSpecs");
        }
        
        if (data.containsKey("upload_type")) {
            this.omics_type = FormElementMapper.parseDataUploadType(data.get("upload_type"));
        } else {
            throw new MultiSlideException("Omics type is missing in call to DatasetSpecs");
        }
        
        if (data.containsKey("filename")) {
            this.filename = data.get("filename");
        } else {
            throw new MultiSlideException("Filename is missing in call to DatasetSpecs");
        }
        
        if (data.containsKey("delimiter")) {
            this.delimiter = data.get("delimiter");
        } else {
            throw new MultiSlideException("Delimiter is missing in call to DatasetSpecs");
        }
        
        this.unique_name = this.createUniqueDatasetName(this.omics_type, folderpath, "unique_name");
        this.display_name = this.createUniqueDatasetName(this.omics_type, folderpath, "display_name");
        this.filename_within_analysis_folder = this.unique_name;
        this.expanded_filename = this.analysis_name + "_" + this.unique_name;
        //this.setExpandedFilename();
        //this.setFilenameWithinAnalysisFolder();
        this.metadata_columns = new String[0];
        this.identifier_metadata_columns = new String[0];
        this.has_linker = false;
        this.has_additional_identifiers = false;
    }
    
    public void changeAnalysisName(String analysis_name) {
        this.analysis_name = analysis_name;
        this.expanded_filename = this.analysis_name + "_" + this.unique_name;
    }
    
    public void update(
            String uploadFolder, 
            String analysis_name, 
            String expanded_filename,
            String[] metadata_columns,
            boolean has_linker,
            String linker_colname,
            String linker_identifier_type,
            boolean has_additional_identifiers,
            String[] identifier_metadata_columns
    ) throws MultiSlideException {
        ListOrderedMap <String, DatasetSpecs> dataset_specs_map = DatasetSpecs.loadSpecsMap(uploadFolder, analysis_name);
        dataset_specs_map.get(expanded_filename)
                .update(
                        metadata_columns, has_linker, linker_colname, linker_identifier_type, has_additional_identifiers, 
                        identifier_metadata_columns
                );
        DatasetSpecs.serializeSpecsMap(uploadFolder, analysis_name, dataset_specs_map);
    }
    
    public void update (
            String[] metadata_columns, 
            boolean has_linker,
            String linker_colname,
            String linker_identifier_type,
            boolean has_additional_identifiers,
            String[] identifier_metadata_columns
    ) throws MultiSlideException {
        this.metadata_columns = metadata_columns;
        this.has_linker = has_linker;
        this.linker_colname = linker_colname;
        this.linker_identifier_type = linker_identifier_type;
        this.has_additional_identifiers = has_additional_identifiers;
        this.identifier_metadata_columns = identifier_metadata_columns;
        
        if (this.linker_identifier_type.equals("mirna_id_2021158607524066")) {
            this.has_mi_rna = true;
            this.mi_rna_colname = this.linker_identifier_type;
        }
    }
    
    private String createUniqueDatasetName(String omics_type, String folderpath, String name_type) 
            throws MultiSlideException {
        
        String concater = "";
        switch (name_type) {
            case "unique_name":
                concater = "_";
                break;
            case "display_name":
                concater = " ";
                break;
            default:
                throw new MultiSlideException("Unknown name type");
        }
        
        // clinical info should be overwritted if already exists
        if (omics_type.equalsIgnoreCase("clinical-info")) {
            return "clinical-info";
        }
        
        // check if the usual name already exists and if so create a new one
        String unique_dataset_name;
        ListOrderedMap <String, DatasetSpecs> map = DatasetSpecs.loadSpecsMap(folderpath, analysis_name);
        if (this.datasetExists(map, omics_type)) {
            int count = 1;
            while (this.datasetExists(map, omics_type + concater + count)) {
                count++;
            }
            if (name_type.equals("unique_name")) {
                unique_dataset_name = omics_type + concater + count;
            } else if (name_type.equals("display_name")) {
                unique_dataset_name = this.mapOmicsTypeStringToDisplayName(omics_type) + concater + count;
            } else {
                throw new MultiSlideException("Unknown name type");
            }
        } else {
            if (name_type.equals("unique_name")) {
                unique_dataset_name = omics_type;
            } else if (name_type.equals("display_name")) {
                unique_dataset_name = this.mapOmicsTypeStringToDisplayName(omics_type);
            } else {
                throw new MultiSlideException("Unknown name type");
            }
        }
        return unique_dataset_name;
    }
    
    public String getDelimiter() {
        return delimiter;
    }

    public String getOmicsType() {
        return omics_type;
    }
    
    public String getLinkerColname() {
        if(this.has_linker) {
            return this.linker_colname;
        } else {
            return "__pseudo__linker__";
        }
    }
    
    /*
    Provided for backward compatibility only, new implementations should use getLinkerColname
    public HashMap <String, String> getLinkerColnameMapping() {
        HashMap <String, String> map = new HashMap <> ();
        map.put(this.linker_identifier_type, this.linker_colname);
        return map;
    }
    */
    
    public String getLinkerIdentifierType() {
        return this.linker_identifier_type;
    }
    
    public String getUniqueName() {
        return this.unique_name;
    }
    
    public String[] getIdentifierMetadataColumns() {
        return this.identifier_metadata_columns;
    }

    public String getDisplayName() {
        return display_name;
    }

    public boolean hasLinker() {
        return has_linker;
    }

    public boolean hasAdditionalIdentifiers() {
        return has_additional_identifiers;
    }
    
    public ArrayList <String> getLinkerAndIdentifierColumnNames() {
        ArrayList <String> colnames = new ArrayList <> ();
        if (this.has_linker) {
            colnames.add(this.linker_colname);
        }
        if (this.has_additional_identifiers) {
            colnames.addAll(Arrays.asList(this.identifier_metadata_columns));
        }
        return colnames;
    }
    
    /*
    private void setFilenameWithinAnalysisFolder() {
        this.filename_within_analysis_folder = this.unique_name;
    }
    
    private void setExpandedFilename() {
        this.expanded_filename = this.analysis_name + "_" + this.unique_name;
    }
    */
    
    public String getFilename() {
        return this.filename;
    }
    
    public String getExpandedFilename() {
        return this.expanded_filename;
    }
    
    public String getFilenameWithinAnalysisFolder() {
        return this.filename_within_analysis_folder;
    }
    
    public String[] getMetaDataColumns() {
        return this.metadata_columns;
    }
    
    /*
    public HashMap<String, String> getIdentifierMetadataColumnMappings() {
        return this.identifier_metadata_column_mappings;
    }
    */
    
    private boolean datasetExists(ListOrderedMap <String, DatasetSpecs> map, String name) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (spec.unique_name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addToAnalysis(String folderpath) throws MultiSlideException, Exception {
        
        String path = DatasetSpecs.getPathToMap(folderpath, this.analysis_name);
        File f = new File(path);
        
        if (f.exists()) {
            if(this.omics_type.equals("clinical-info")) {
                DatasetSpecs.removeClinicalInfo(folderpath, this.analysis_name);
            }
            this.appendToSpecsMap(folderpath);
        } else {
            ListOrderedMap <String, DatasetSpecs> map = new ListOrderedMap <> ();
            map.put(this.expanded_filename, this);
            DatasetSpecs.serializeSpecsMap(folderpath, this.analysis_name, map);
        }
        
    }
    
    public static void removeFromAnalysis (String folderpath, String analysis_name, String expanded_filename) throws Exception{
        
        String path = DatasetSpecs.getPathToMap(folderpath, analysis_name);
        File f = new File(path);
        
        if (f.exists()) {
            ListOrderedMap <String, DatasetSpecs> map = DatasetSpecs.loadSpecsMap(folderpath, analysis_name);
            if (map.containsKey(expanded_filename)) {
                map.remove(expanded_filename);
            } else {
                throw new MultiSlideException("Unable to delete. Dataset specs not found in analysis.");
            }
            DatasetSpecs.serializeSpecsMap(folderpath, analysis_name, map);
        } else {
            throw new MultiSlideException("Unable to delete. The associated data file was not found in analysis.");
        }
    }
    
    public static void serializeSpecsMap(String folderpath, String analysis_name, ListOrderedMap <String, DatasetSpecs> map) {
        
        String path = DatasetSpecs.getPathToMap(folderpath, analysis_name);
        
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(map);
            oos.flush();
            oos.close();
        } catch (IOException io) {
            io.printStackTrace();
	} finally {
            if (oos != null) {
                try {
                    oos.close();
		} catch (IOException e) {
                    e.printStackTrace();
		}
            }
	}
    }
    
    public static ListOrderedMap <String, DatasetSpecs> loadSpecsMap (String folderpath, String analysis_name) {
        
        String path = DatasetSpecs.getPathToMap(folderpath, analysis_name);
        ListOrderedMap <String, DatasetSpecs> map = new ListOrderedMap <>();
        ObjectInputStream ois = null;
        
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            map = (ListOrderedMap <String, DatasetSpecs>) ois.readObject();
            ois.close();
        } catch (Exception io) {
            io.printStackTrace();
	} finally {
            if (ois != null) {
                try {
                    ois.close();
		} catch (IOException e) {
                    e.printStackTrace();
		}
            }
	}
        
        return map;
    }

    private void appendToSpecsMap(String folderpath) {
        ListOrderedMap <String, DatasetSpecs> map = DatasetSpecs.loadSpecsMap(folderpath, this.analysis_name);
        map.put(this.expanded_filename, this);
        DatasetSpecs.serializeSpecsMap(folderpath, this.analysis_name, map);
    }
 
    private static String getPathToMap(String folderpath, String analysis_name) {
        String path = folderpath + File.separator + analysis_name + ".dataset.specs";
        return path;
    }
    
    private static void removeClinicalInfo(String folderpath, String analysis_name) throws MultiSlideException, Exception {
        ListOrderedMap <String, DatasetSpecs> map = DatasetSpecs.loadSpecsMap(folderpath, analysis_name);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (spec.omics_type.equals("clinical-info")) {
                DatasetSpecs.removeFromAnalysis(folderpath, analysis_name, spec.expanded_filename);
            }
        }
    }
    
    private String mapOmicsTypeStringToDisplayName(String omics_type) throws MultiSlideException {
        
        if (null == omics_type) {
            throw new MultiSlideException("Omics type is null");
        } else {
            int i = DatasetSpecs.OMICS_TYPES.indexOf(omics_type);
            if (i == -1)
                return "Other";
            else
                return DatasetSpecs.OMICS_TYPE_DIPSLAY_NAMES.get(i);
        }
        /*
        if (null == omics_type) {
            throw new MultiSlideException("Omics type is null");
        } else switch (omics_type) {
            case "cnv":
                return "Copy Number Variation";
            case "dna_meth":
                return "DNA Methylation";
            case "m_rna":
                return "mRNA";
            case "mi_rna":
                return "microRNA";
            case "protein":
                return "Protein";
            default:
                return "Other";
        }
        */
    }
}
