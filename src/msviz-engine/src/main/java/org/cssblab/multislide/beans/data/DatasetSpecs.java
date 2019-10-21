package org.cssblab.multislide.beans.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.MultiSlideException;
import org.cssblab.multislide.utils.FileHandler;
import org.cssblab.multislide.utils.FormElementMapper;

/**
 *
 * @author Soumita
 */
public class DatasetSpecs implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public String analysis_name;
    public String filename;
    public String delimiter;
    public String omics_type;
    public String row_identifier;
    public String expanded_filename;
    public String filename_within_analysis_folder;
    
    public String[] metadata_columns;
    public HashMap <String, String> identifier_metadata_column_mappings;
    
    public DatasetSpecs(String analysis_name) {
        this.analysis_name = analysis_name;
    }
    
    public DatasetSpecs(String analysis_name, String filename, String delimiter, String omics_type, String row_identifier) {
        this.analysis_name = analysis_name;
        this.filename = filename;
        this.delimiter = delimiter;
        this.omics_type = omics_type;
        this.row_identifier = row_identifier;
        this.setExpandedFilename();
        this.setFilenameWithinAnalysisFolder();
    }
    
    public DatasetSpecs(HashMap <String, String> data) {
        
        if (data.containsKey("analysis_name")) {
            this.analysis_name = data.get("analysis_name");
        }
        if (data.containsKey("filename")) {
            this.filename = data.get("filename");
        }
        if (data.containsKey("delimiter")) {
            this.delimiter = data.get("delimiter");
        }
        if (data.containsKey("upload_type")) {
            this.omics_type = FormElementMapper.parseDataUploadType(data.get("upload_type"));
            //this.omics_type = data.get("upload_type");
        }
        if (data.containsKey("identifier_type")) {
            this.row_identifier = FormElementMapper.parseRowIdentifierType(data.get("identifier_type"));
            //this.row_identifier = data.get("identifier_type");
        }
       
        this.setExpandedFilename();
        this.setFilenameWithinAnalysisFolder();
        
    }
    
    public String getMappedColname(String identifier) {
        return this.identifier_metadata_column_mappings.get(identifier);
    }
    
    public final void setFilenameWithinAnalysisFolder() {
        this.filename_within_analysis_folder = this.omics_type + "_"
                + this.row_identifier + "_"
                + this.delimiter + "_"
                + this.filename;
    }
    
    public final void setExpandedFilename() {
        this.expanded_filename = this.analysis_name + "_"
                + this.omics_type + "_"
                + this.row_identifier + "_"
                + this.delimiter + "_"
                + this.filename;
    }
    
    public void setMetaDatColumns(
            String uploadFolder, 
            String analysis_name, 
            String expanded_filename,
            String[] metadata_columns, 
            String[] cols_with_id_mappings, 
            String[] identifiers
    ) throws MultiSlideException {
        HashMap <String, DatasetSpecs> dataset_specs_map = (new DatasetSpecs(analysis_name)).loadSpecsMap(uploadFolder);
        dataset_specs_map.get(expanded_filename).setMetaDataColumns(metadata_columns, cols_with_id_mappings, identifiers);
        dataset_specs_map.get(expanded_filename).serializeSpecsMap(uploadFolder, dataset_specs_map);
    }
    
    public void setMetaDataColumns (
            String[] metadata_columns, 
            String[] cols_with_id_mappings, 
            String[] identifiers
    ) throws MultiSlideException {
        if (cols_with_id_mappings.length != identifiers.length) {
            throw new MultiSlideException("Number of columns and identifiers they map to, do not match");
        }
        this.identifier_metadata_column_mappings = new HashMap <String, String> ();
        this.metadata_columns = metadata_columns;
        for (int i=0; i<cols_with_id_mappings.length; i++) {
            this.identifier_metadata_column_mappings.put(identifiers[i], cols_with_id_mappings[i]);
        }
    }
    
    public void addToAnalysis(String folderpath) throws MultiSlideException, Exception {
        
        String path = this.getPathToMap(folderpath);
        File f = new File(path);
        
        if (f.exists()) {
            if(this.omics_type.equals("clinical-info")) {
                this.removeClinicalInfo(folderpath);
            }
            this.appendToSpecsMap(folderpath);
        } else {
            HashMap <String, DatasetSpecs> map = new HashMap <String, DatasetSpecs> ();
            map.put(this.expanded_filename, this);
            this.serializeSpecsMap(folderpath, map);
        }
        
    }
    
    public void removeFromAnalysis (String folderpath) throws Exception{
        
        String path = this.getPathToMap(folderpath);
        File f = new File(path);
        
        if (f.exists()) {
            HashMap <String, DatasetSpecs> map = this.loadSpecsMap(folderpath);
            map.remove(this.expanded_filename);
            this.serializeSpecsMap(folderpath, map);
        } else {
            throw new Exception();
        }
    }
    
    private void serializeSpecsMap(String folderpath, HashMap <String, DatasetSpecs> map) {
        
        String path = this.getPathToMap(folderpath);
        
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
    
    public HashMap <String, DatasetSpecs> loadSpecsMap(String folderpath) {
        
        String path = this.getPathToMap(folderpath);
        HashMap <String, DatasetSpecs> map = new HashMap <String, DatasetSpecs>();
        ObjectInputStream ois = null;
        
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            map = (HashMap <String, DatasetSpecs>) ois.readObject();
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
        HashMap <String, DatasetSpecs> map = this.loadSpecsMap(folderpath);
        map.put(this.expanded_filename, this);
        this.serializeSpecsMap(folderpath, map);
    }
 
    private String getPathToMap(String folderpath) {
        String path = folderpath + File.separator + this.analysis_name + ".dataset.specs";
        return path;
    }
    
    private void removeClinicalInfo(String folderpath) throws MultiSlideException, Exception {
        HashMap <String, DatasetSpecs> map = this.loadSpecsMap(folderpath);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            DatasetSpecs spec = (DatasetSpecs)pair.getValue();
            if (spec.omics_type.equals("clinical-info")) {
                spec.removeFromAnalysis(folderpath);
            }
        }
    }
}
