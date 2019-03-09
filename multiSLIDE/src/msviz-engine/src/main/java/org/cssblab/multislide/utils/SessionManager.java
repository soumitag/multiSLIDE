/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cssblab.multislide.beans.data.DatasetSpecs;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.structure.MultiSlideException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author soumitag
 */
public class SessionManager {
    
    // Returns STATUS_CODE
    // STATUS_CODE = 2  --> new session and analysis directory created
    // STATUS_CODE = 1  --> new analysis directory created inside existing session directory
    // STATUS_CODE = 0  --> both analysis and session directories exist, means analysis name is already in use
    public static int handleSessionState (String analysis_name, HttpServletRequest request, String installPath) {
        
        int STATUS_CODE;
        
        // get the current session's id
        String session_id;
        HttpSession session = request.getSession(false);
        
        // check if a session already exists 
        if (session == null) {
            // session does not exist, create it
            session = request.getSession(true);
            session_id = session.getId();
            STATUS_CODE = 2;
        } else {
            // session exists
            // check if an analysis of same name exists in the session
            if (session.getAttribute(analysis_name) == null) {
                // analysis does not exist 
                // create required temp folders for this analysis
                session_id = session.getId();
                STATUS_CODE = 1;
            } else {
                // analysis already exists, return STATUS_CODE = 0 immediately
                STATUS_CODE = 0;
                return STATUS_CODE;
            }
        }

        // create session directory
        SessionManager.createSessionDir(installPath, session_id);
        
        // set base url
        String url = request.getRequestURL().toString();
        String base_url = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
        session.setAttribute("base_url", base_url);
        
        return STATUS_CODE;
    }
    
    /*
    public static String getInstallPath (HttpSession session) {
        
        InputStream in = session.getServletContext().getResourceAsStream("/WEB-INF/multi-slide.config");
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            br.close();
            in.close();
            return line;
        } catch (Exception e) {
            System.out.println ("Failed to read multi-slide.config");
            return null;
        }
    }
    */
    
    // used to intialize install_path in servlet context in MultiSlideContextListener
    public static String getInstallPath (InputStream in) {
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            br.close();
            in.close();
            return line;
        } catch (Exception e) {
            System.out.println ("Failed to read multi-slide.config");
            return null;
        }
    }
    
    public static String getPyModulePath (String installPath) {
        return installPath + File.separator + "src";
    }
    
    public static String getBasePath (String installPath, String session_id, String analysis_name) {
        return installPath + File.separator + "temp" + File.separator + session_id + File.separator + analysis_name;
    }
    
    public static String createSessionDir (String installPath, String session_id) {
        String session_dir_path = installPath + File.separator + "temp" + File.separator + session_id;
        File session_dir = new File(session_dir_path);
        if (!session_dir.exists()) { 
            session_dir.mkdir();
        }
        return session_dir_path;
    }
    
    public static String getSessionDir (HttpSession session) {
        
        String installPath = (String) session.getServletContext().getAttribute("install_path");
        String session_id = session.getId();
 
        String session_dir_path = installPath + File.separator + "temp" + File.separator + session_id;
        return session_dir_path;
    }
    
    public static void createAnalysisDirs (AnalysisContainer analysis) {
        
        File analysis_dir = new File(analysis.base_path);
        if (!analysis_dir.exists()) {
            analysis_dir.mkdir();
        }
        
        File images_dir = new File(analysis.base_path + File.separator + "images");
        if (!images_dir.exists()) {
            images_dir.mkdir();
        }
            
        File data_dir = new File(analysis.base_path + File.separator + "data");
        if (!data_dir.exists()) {
            data_dir.mkdir();
        }
    }
    
    public static String[] moveInputFilesToAnalysisDir (
            String installPath, String session_id, String analysis_name,
            String filename_in, 
            String sample_series_mapping_filename_in) {
        
        // copy the data file from temp location to data dir
        //String tempFolder = pageContext.getServletContext().getRealPath("") + File.separator + "temp" + File.separator + request.getSession().getId();
        String session_dir = installPath + File.separator + "temp" + File.separator + session_id;
        
        // source file paths
        String temp_data_filepath = session_dir + File.separator + 
                                    analysis_name + "_" + filename_in;
        
        String temp_map_filepath = session_dir + File.separator + 
                                   analysis_name + "_" + sample_series_mapping_filename_in;
        
        // target file paths
        String filename = session_dir + File.separator + 
                          analysis_name + File.separator + 
                          "data" + File.separator + 
                          filename_in;
        
        String sample_series_mapping_filename = session_dir + File.separator + 
                                                analysis_name + File.separator + 
                                                "data" + File.separator + 
                                                sample_series_mapping_filename_in;

        // copy two files from source to target
        File data_file = new File(temp_data_filepath);
        data_file.renameTo(new File(filename));
        File map_file = new File(temp_map_filepath);
        map_file.renameTo(new File(sample_series_mapping_filename));
        
        return new String[]{filename, sample_series_mapping_filename};
    }
    
    public static void moveInputFilesToAnalysisDir (String uploadFolder, String analysis_basepath, HashMap <String, DatasetSpecs> dataset_specs_map)
    throws MultiSlideException {
        
        try {
            Iterator it = dataset_specs_map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                DatasetSpecs spec = (DatasetSpecs)pair.getValue();
                String orig_path = uploadFolder + File.separator + spec.expanded_filename;
                String new_path = analysis_basepath + File.separator + spec.filename_within_analysis_folder;
                File data_file_source = new File(orig_path);
                File data_file_target = new File(new_path);
                FileUtils.copyFile(data_file_source, data_file_target);
                //File data_file = new File(orig_path);
                //data_file.renameTo(new File(new_path));
            }
        } catch (Exception e) {
            throw new MultiSlideException("Error transferring data to analysis directory: " + e.getMessage());
        }
        
    }
    
    public static void moveInputFilesToAnalysisDir_ForDemo (String uploadFolder, String analysis_basepath, HashMap <String, DatasetSpecs> dataset_specs_map)
    throws MultiSlideException {
        
        try {
            Iterator it = dataset_specs_map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                DatasetSpecs spec = (DatasetSpecs)pair.getValue();
                String orig_path = uploadFolder + File.separator + spec.filename;
                String new_path = analysis_basepath + File.separator + spec.filename_within_analysis_folder;
                File data_file_source = new File(orig_path);
                File data_file_target = new File(new_path);
                FileUtils.copyFile(data_file_source, data_file_target);
                //File data_file = new File(orig_path);
                //data_file.renameTo(new File(new_path));
            }
        } catch (Exception e) {
            throw new MultiSlideException("Error transferring data to analysis directory: " + e.getMessage());
        }
        
    }

    public static String getSessionDir(String installPath, String id) {
        return installPath + File.separator + "temp" + File.separator + id;
    }
    
    
}
