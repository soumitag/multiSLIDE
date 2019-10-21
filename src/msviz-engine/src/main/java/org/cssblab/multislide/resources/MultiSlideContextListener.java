/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import org.cssblab.multislide.utils.SessionManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.PyColorMaps;
/**
 *
 * @author soumitag
 */
@WebListener("application context listener")
public class MultiSlideContextListener implements ServletContextListener {

    /**
     * Initialize log4j when the application is being started
     * @param event
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        
        // set install path
        ServletContext context = event.getServletContext();
        String install_path = context.getInitParameter("install-path");
        context.setAttribute("install_path", install_path);
        
        // initialize log4
        //String log4jConfigFile = context.getInitParameter("log4j-config-location");
        //String fullPath = context.getRealPath("") + File.separator + log4jConfigFile;
        String fullPath = install_path + File.separator + "config" + File.separator + "log4j_config.xml";
        try {
            ConfigurationSource source = new ConfigurationSource (new FileInputStream (fullPath));
            Configurator.initialize(null, source);
        } catch (Exception e) {
            System.out.println("Failed to initialize logging service.");
        }
        
        Logger logger = LogManager.getRootLogger();
        //context.setAttribute("logger", logger);
        
        logger.trace("TRACE: Logger Ready.");
        logger.error("ERROR: Logger Ready.");
        
        //set colormaps
        PyColorMaps colormaps = new PyColorMaps(install_path + File.separator + "db" + File.separator + "py_colormaps");
        context.setAttribute("colormaps", colormaps);
        
        ColorPalette categorical_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "categorical_color_palette.txt");
        context.setAttribute("categorical_palatte", categorical_palette);
        
        ColorPalette continuous_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "continuous_color_palette.txt");
        context.setAttribute("continuous_palatte", continuous_palette);
        
        ColorPalette gene_group_color_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "gene_group_color_palette.txt");
        context.setAttribute("gene_group_color_palette", gene_group_color_palette);
        
        HashMap <Integer, String> identifier_name_map = createIdentifierNameMap();
        context.setAttribute("identifier_name_map", identifier_name_map);
        
        HashMap <String, Integer> identifier_index_map = createIdentifierIndexMap();
        context.setAttribute("identifier_index_map", identifier_index_map);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
    }
    
    private HashMap <Integer, String> createIdentifierNameMap() {
        HashMap <Integer, String> identifier_name_map = new HashMap <> ();
        identifier_name_map.put(0, "entrez_2021158607524066");
        identifier_name_map.put(1, "genesymbol_2021158607524066");
        identifier_name_map.put(2, "refseq_2021158607524066");
        identifier_name_map.put(3, "ensembl_gene_id_2021158607524066");
        identifier_name_map.put(4, "ensembl_transcript_id_2021158607524066");
        identifier_name_map.put(5, "ensembl_protein_id_2021158607524066");
        identifier_name_map.put(6, "uniprot_id_2021158607524066");
        identifier_name_map.put(7, "mirna_id_2021158607524066");
        return identifier_name_map;
    }
    
    private HashMap <String, Integer> createIdentifierIndexMap() {
        HashMap <String, Integer> identifier_index_map  = new HashMap <> ();
        identifier_index_map.put("entrez_2021158607524066", 0);
        identifier_index_map.put("genesymbol_2021158607524066", 1);
        identifier_index_map.put("refseq_2021158607524066", 2);
        identifier_index_map.put("ensembl_gene_id_2021158607524066", 3);
        identifier_index_map.put("ensembl_transcript_id_2021158607524066", 4);
        identifier_index_map.put("ensembl_protein_id_2021158607524066", 5);
        identifier_index_map.put("uniprot_id_2021158607524066", 6);
        identifier_index_map.put("mirna_id_2021158607524066", 7);
        return identifier_index_map;
    }
}