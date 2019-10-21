package org.cssblab.multislide.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.cssblab.multislide.searcher.Searcher;

/**
 *
 * @author soumitag
 */
public class Serializer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public Serializer(){}
    
    public void serializeAnalysis(AnalysisContainer analysis, String filepath) {
        
        try (ObjectOutputStream oos
                = new ObjectOutputStream(new FileOutputStream(filepath + File.separator + analysis.analysis_name + ".mslide"))) {
            
            analysis.searcher = null;
            oos.writeObject(analysis);
            System.out.println("Analysis saved.");
            
            //create a searcher object
            analysis.setSearcher(new Searcher(analysis.species));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    public AnalysisContainer loadAnalysis(String filepath) {
        
        AnalysisContainer analysis = null;

        try (ObjectInputStream ois
                = new ObjectInputStream(new FileInputStream(filepath))) {

            analysis = (AnalysisContainer) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return analysis;
    }
    
}
