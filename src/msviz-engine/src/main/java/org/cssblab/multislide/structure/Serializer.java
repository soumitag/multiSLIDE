package org.cssblab.multislide.structure;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.cssblab.multislide.utils.Utils;

/**
 *
 * @author soumitag
 */
public class Serializer implements Serializable {
    
    public static String TYPE_OBJ = "object";
    public static String TYPE_JSON = "json";
    
    private static final long serialVersionUID = 1L;
    
    public Serializer(){}
    
    public String serializeAnalysis(AnalysisContainer analysis, String filepath, String session_folder_path, String type) throws Exception {
        
        AnalysisState state = new AnalysisState(analysis, session_folder_path);

        if (type.equals(Serializer.TYPE_OBJ)) {
            
            ObjectOutputStream oos
                    = new ObjectOutputStream(new FileOutputStream(filepath + File.separator + state.analysis_name + ".mslide"));
            
            oos.writeObject(state);
            Utils.log_info("Analysis saved.");
            return state.analysis_name + ".mslide";
            
        } else if (type.equals(Serializer.TYPE_JSON)) {
            
            String json = state.asJSON();
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath + File.separator + state.analysis_name + ".mslide", false));
            writer.append(json);
            writer.close();
            return state.analysis_name + ".mslide";
            
        } else {
            throw new MultiSlideException("Unknown type: " + type + ". Can be 'object' or 'json'.");
        }

    }
    
    public AnalysisState loadAnalysis(String filepath, String type) throws Exception {
        
        if (type.equals(Serializer.TYPE_OBJ)) {
            
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath));
            AnalysisState state = (AnalysisState) ois.readObject();
            return state;
            
        } else if (type.equals(Serializer.TYPE_JSON)) {
            
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            AnalysisState state = new Gson().fromJson(br, AnalysisState.class);  
            return state;
            
        } else {
            throw new MultiSlideException("Unknown type: " + type + ". Can be 'object' or 'json'.");
        }
    }
    
    public static void mslideObjectToJSON(String infile, String outfile) throws Exception {
        
        Serializer s = new Serializer();
        AnalysisState state = s.loadAnalysis(infile, Serializer.TYPE_OBJ);
        
        String json = state.asJSON();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile, false));
        writer.append(json);
        writer.close();
    }
    
}
