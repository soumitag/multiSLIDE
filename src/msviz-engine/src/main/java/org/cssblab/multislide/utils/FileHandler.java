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
import java.util.StringTokenizer;
import org.cssblab.multislide.datahandling.DataParsingException;

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
                System.out.println("Error reading input data:");
                System.out.println(e);
            }
        } else {
            try {
                br = new BufferedReader(new FileReader(filepath));
                line = br.readLine();
            } catch (Exception e) {
                System.out.println("Error reading input data:");
                System.out.println(e);
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
            
            System.out.println("Error reading input data:");
            System.out.println(e);
            return null;
            
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch(IOException ioe) {
                System.out.println("Error reading input data:");
                System.out.println(ioe);
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
            
            System.out.println("Error reading input data:");
            System.out.println(e);
            return null;
            
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch(IOException ioe) {
                System.out.println("Error reading input data:");
                System.out.println(ioe);
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
    
    public static void saveDataMatrix (String filename, String delim, float[][] datacells) {
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
            System.out.println(e);
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
            System.out.println(e);
        }
    }
}
