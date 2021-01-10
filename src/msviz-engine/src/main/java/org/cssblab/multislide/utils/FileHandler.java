/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.utils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.cssblab.multislide.datahandling.DataParsingException;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MultiSlideException;

/**
 *
 * @author Soumita
 */
public class FileHandler {
    
    public static ArrayList<String> loadListData(String filepath, String delimiter) {

        ArrayList<String> list_data = new ArrayList<String>();
        BufferedReader br = null;
        String line = null;

        if (delimiter.equals("\n")) {
            try {
                br = new BufferedReader(new FileReader(filepath));
                while ((line = br.readLine()) != null) {
                    list_data.add(line.trim().toLowerCase());
                }
            } catch (Exception e) {
                Utils.log_exception(e, "Error reading input data:");
            }
        } else {
            try {
                br = new BufferedReader(new FileReader(filepath));
                line = br.readLine();
            } catch (Exception e) {
                Utils.log_exception(e, "Error reading input data:");
            }
            StringTokenizer st = new StringTokenizer(line, delimiter);
            while (st.hasMoreTokens()) {
                list_data.add(st.nextToken().trim().toLowerCase());
            }
        }

        return list_data;
    }
    
    public static String[][] previewFile (String folderpath, String fileDelimiter, int previewRows) throws DataParsingException, IOException{
        
        //String path = this.getPathToMap(folderpath);
        //String path = this.expanded_filename;
        File f = new File(folderpath);
        String[][] previewData = new String[previewRows][];        
        
        if (f.exists()) {
            String delim = FormElementMapper.parseDelimiter(fileDelimiter);
            previewData = FileHandler.loadDelimData(folderpath, delim, false, previewRows);
        } else {
            throw new DataParsingException("");
        }
        return previewData;
    }
    
    public static String checkFileHeader (String folderpath, String fileDelimiter) throws DataParsingException, IOException {
        
        File f = new File(folderpath);
        String[][] previewData;        
        
        if (f.exists()) {
            
            String delim = FormElementMapper.parseDelimiter(fileDelimiter);
            previewData = FileHandler.loadDelimData(folderpath, delim, false, 1);
            
            ArrayList <String> offending_cols = new ArrayList <> ();
            for (String header: previewData[0]) {
                if (header.contains(".")) {
                    offending_cols.add(header);
                }
            }
            
            if(offending_cols.size() > 0) {
                String message = "";
                message += "multiSLIDE uses Apache Spark which does not like dots in column names.";
                message += " Kindly replace the dots in the following column names and try again: ";
                message += String.join(", ", offending_cols);
                return message;
            }
            
        } else {
            throw new DataParsingException("");
        }
        
        return "";
    }
    
    public static String[][] loadDelimData (String inFile, 
                                            String delim, 
                                            boolean hasHeader, 
                                            int maxRows) 
    throws DataParsingException, IOException{
             
        BufferedReader br = null;
        String line;
        
        ArrayList <String []> dataVec = new ArrayList <> ();

        try {
        
            br = new BufferedReader(new FileReader(inFile));

            int count = 0;
            String[] lineData = null;
            boolean isFirst = true;
            int line_length = -1;
            while ((line = br.readLine()) != null) {

                if (isFirst & hasHeader) {
                    isFirst = false;
                } else {
                    if(!delim.equals("|")){
                        lineData = line.split(delim, -1);
                    } else {
                        lineData = line.split("\\" + delim, -1);
                    }
                    if (count > 0) {
                        if (line_length != lineData.length) {
                            br.close();
                            throw new DataParsingException(
                                    "Error while reading data: Rows " + count + " and " + (count + 1)
                                    + " have different number of columns. All rows must have the same number of columns. Please verify that the selected delimiter is correct.");
                        }
                    }
                    line_length = lineData.length;
                    dataVec.add(lineData);
                    count++;
                }

                if (count == maxRows) {
                    break;
                }
            }

            br.close();

            String[][] data = new String[dataVec.size()][lineData.length];
            for (int i = 0; i < dataVec.size(); i++) {
                for (int j = 0; j < lineData.length; j++) {
                    data[i][j] = dataVec.get(i)[j];
                }
            }

            return data;

        } finally {
            if (br != null) {
                br.close();
            }
        }
        
    }
    
    public static String[][] loadDelimData (String inFile, String delim, boolean hasHeader) 
    throws DataParsingException, IOException {
             
        BufferedReader br1 = null;
        BufferedReader br2 = null;
        String line;
        
        String[][] data;
        String[] lineData = null;
                
        try {

            int count = 1;
            br1 = new BufferedReader(new FileReader(inFile));
            line = br1.readLine();
            
            if (line == null) {
                data = new String[0][0];
                return data;
            }
            
            if(!delim.equals("|")){
                lineData = line.split(delim, -1);
            } else {
                lineData = line.split("\\" + delim, -1);
            }
            
            while ((line = br1.readLine()) != null) {
                count++;
            }
            br1.close();
            
            if (hasHeader) {
                data = new String[count-1][lineData.length];
            } else {
                data = new String[count][lineData.length];
            }
            
            count = 0;
            br2 = new BufferedReader(new FileReader(inFile));
            boolean isFirst = true;
            int line_length = -1;
            while ((line = br2.readLine()) != null) {

                if (isFirst & hasHeader) {
                    isFirst = false;
                } else {
                    if(!delim.equals("|")){
                        lineData = line.split(delim, -1);
                    } else {
                        lineData = line.split("\\" + delim, -1);
                    }
                    if (count > 0) {
                        if (line_length != lineData.length) {
                            br2.close();
                            throw new DataParsingException(
                                    "Error while reading data: Rows " + count + " and " + (count + 1)
                                    + " have different number of columns. All rows must have the same number of columns. Please verify that the selected delimiter is correct.");
                        }
                    }
                    line_length = lineData.length;
                    data[count++] = lineData;
                }
            }
            br2.close();
            
            return data;
        
        } finally {
            if (br1 != null) {
                br1.close();
            }
            if (br2 != null) {
                br2.close();
            }
        }
        
    }
    
    public static List<List<String>> loadDelimListData (String inFile, String delim, boolean hasHeader) 
    throws DataParsingException, IOException {
             
        BufferedReader br2 = null;
        String line;
        
        List<List<String>> data = new ArrayList();
        List<String> lineData;
                
        try {

            br2 = new BufferedReader(new FileReader(inFile));
            boolean isFirst = true;
            
            while ((line = br2.readLine()) != null) {

                if (isFirst & hasHeader) {
                    isFirst = false;
                } else {
                    if(!delim.equals("|")){
                        lineData = CollectionUtils.asList(line.split(delim, -1));
                    } else {
                        lineData = CollectionUtils.asList(line.split("\\" + delim, -1));
                    }
                    data.add(lineData);
                }
            }
            br2.close();
            
            return data;
        
        } finally {
            if (br2 != null) {
                br2.close();
            }
        }
        
    }
    
    public static int[] getFileDimensions (String inFile, String delim) {
        
        boolean isFirst = true;
        int[] height_width = new int[2];
        String line;
        
        BufferedReader br = null;
        
        try {
            
            br = new BufferedReader(new FileReader(inFile));
        
            while ((line = br.readLine()) != null) {
                if (isFirst) {
                    if(!delim.equals("|")){
                        height_width[1] = (line.split(delim, -1)).length;
                    } else {
                        height_width[1] = (line.split("\\" + delim, -1)).length;
                    }
                    isFirst = false;
                }
                height_width[0]++;
            }
            
            return height_width;
            
        } catch (Exception e) {
            
            Utils.log_exception(e, "Error reading input data:");
            return null;
            
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch(IOException ioe) {
                Utils.log_exception(ioe, "Error reading input data");
                return null;
            }
            
        }
        
    }
    
    public static String[] getFileHeader (String inFile, String delim) {
        
        String[] headers = null;
        
        BufferedReader br = null;
        
        try {
            
            br = new BufferedReader(new FileReader(inFile));
            String line = br.readLine();
            if (line != null) {
                if(!delim.equals("|")){
                    headers = line.split(delim, -1);
                } else {
                    headers = line.split("\\" + delim, -1);
                }
            }
        
            return headers;
            
        } catch (Exception e) {
            
            Utils.log_exception(e, "Error reading input data:");
            return null;
            
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch(IOException ioe) {
                Utils.log_exception(ioe, "Error reading input data:");
                return null;
            }
        }
        
    }
    
    public static double[][] loadDoubleDelimData (String inFile, String delim, boolean hasHeader) 
    throws DataParsingException, IOException {
        String[][] data = loadDelimData (inFile, delim, hasHeader);
        double[][] doubleData = new double[data.length][data[0].length];
        for (int i=0; i<data.length; i++) {
            for (int j=0; j<data[0].length; j++) {
                doubleData[i][j] = Double.parseDouble(data[i][j]);
            }
        }
        return doubleData;
    }
    
    public static boolean makeDirectoryPath(String folderpath) throws MultiSlideException {
        File folder = new File(folderpath);
        try {
            return folder.mkdirs();
        } catch (Exception e) {
            throw new MultiSlideException("Enrichment analysis could not create target directory in cache");
        }
    }
    
    public static void saveDataMatrix (String filename, String delim, float[][] datacells) throws MultiSlideException {
        try {
            int height = datacells.length;
            int width = datacells[0].length;
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
            for (int i=0; i<height; i++) {
                for (int j=0; j<width - 1; j++) {
                    writer.append(datacells[i][j] + delim);
                }
                writer.append(datacells[i][width-1] + "");
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Error in Filehandler.saveDataMatrix()");
        }
    }
    
    public static void saveDataMatrix (String filename, String delim, String[][] datacells) throws MultiSlideException {
        try {
            int height = datacells.length;
            int width = datacells[0].length;
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
            for (int i=0; i<height; i++) {
                for (int j=0; j<width - 1; j++) {
                    writer.append(datacells[i][j] + delim);
                }
                writer.append(datacells[i][width-1]);
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            Utils.log_exception(e, "");
            throw new MultiSlideException("Error in Filehandler.saveDataMatrix()");
        }
    }
    
    public static void saveDataMatrix (String filename, String delim, ArrayList <ArrayList<Float>> datacells) {
        try {
            int height = datacells.size();
            int width = datacells.get(0).size();
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
            for (int i=0; i<height; i++) {
                for (int j=0; j<width-1; j++) {
                    writer.append(datacells.get(i).get(j) + delim);
                }
                writer.append(datacells.get(i).get(width-1) + "");
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            Utils.log_exception(e, "");
        }
    }
    
    public static HashMap <String, HashMap <List<String>, List<String>>> parseUserSpecifiedPathways(
            String filename, String delim, HashMap <String, String> filename_map, AnalysisContainer analysis) 
    throws DataParsingException, IOException {
        
        HashMap <String, HashMap <List<String>, List<String>>> r = new HashMap <> ();
        
        String line;
        BufferedReader br = new BufferedReader(new FileReader(filename));
        /*
        get rid of the header
        */
        line = br.readLine();
        
        while ((line = br.readLine()) != null) {
            String[] name_list = line.split("\t", -1);
            String func_grp_name = name_list[0];
            String fname = name_list[1].toLowerCase();
            String colname = name_list[2];
            
            if(!filename_map.containsKey(name_list[1].toLowerCase())) {
                String err_msg = "The given filename '" + fname + "' has no match. Available filenames are:";
                int i = 1;
                for (String f: filename_map.keySet()) {
                    err_msg += " " + i + ")" + f;
                    i++;
                }
                throw new DataParsingException(err_msg);
            }
            String dataset_name = filename_map.get(fname);
            ArrayList <String> avail_cols = analysis.data.datasets.get(dataset_name).specs.getLinkerAndIdentifierColumnNames();
            
            if (!avail_cols.contains(colname)) {
                throw new DataParsingException("File '" + fname + "' (" + dataset_name + ") does not have a metadata column by the name: '" + colname + "'");
            }
            
            List <String> dataset_column_name_key = Arrays.asList(dataset_name, colname);
            
            if (r.containsKey(func_grp_name)) {
                
                HashMap <List<String>, List<String>> d = r.get(func_grp_name);
                if (d.containsKey(dataset_column_name_key)) {
                    d.get(dataset_column_name_key).add(name_list[3]);
                } else {
                    List <String> a = new ArrayList <> ();
                    a.add(name_list[3]);
                    d.put(dataset_column_name_key, a);
                }
                r.put(func_grp_name, d);
                
            } else {
                
                HashMap <List<String>, List<String>> d = new HashMap <> ();
                List <String> a = new ArrayList <> ();
                a.add(name_list[3]);
                d.put(dataset_column_name_key, a);
                r.put(func_grp_name, d);
            }
        }
        
        if (r.isEmpty()) {
            throw new DataParsingException("Could not create pathway, as user specified pathway is empty.");
        }
        
        return r;
    }
    
    public static void savePropertiesFile (String filename, HashMap <String, String> data) 
    throws IOException {
        
        String line = "";
        BufferedWriter b = new BufferedWriter(new FileWriter(filename));
        for (String key: data.keySet()) {
            line += key + "=" + data.get(key) + "\n";
        }
        b.write(line);
        b.close();
    }
}
