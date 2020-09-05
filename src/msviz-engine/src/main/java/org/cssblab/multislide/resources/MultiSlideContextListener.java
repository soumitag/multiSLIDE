/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.resources;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.spark.sql.SparkSession;
import org.cssblab.multislide.graphics.ColorPalette;
import org.cssblab.multislide.graphics.PyColorMaps;
import org.cssblab.multislide.structure.AnalysisContainer;
import org.cssblab.multislide.utils.Utils;
/**
 *
 * @author soumitag
 */
@WebListener("application context listener")
public class MultiSlideContextListener implements ServletContextListener {

    public static int N_SPARK_PARTITIONS = 1;

    /**
     * Initialize log4j when the application is being started
     * @param event
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        
        // set install path
        Map <String, String> env = System.getenv();
        String install_path = env.get("MULTISLIDE_HOME");
        
        ServletContext context = event.getServletContext();
        context.setAttribute("install_path", install_path);
        /*
        String install_path = context.getInitParameter("install-path");
        context.setAttribute("install_path", install_path);
        */
        
        // initialize log4
        String fullPath = install_path + File.separator + "config" + File.separator + "log4j_config.xml";
        try {
            ConfigurationSource source = new ConfigurationSource (new FileInputStream (fullPath));
            Configurator.initialize(null, source);
        } catch (Exception e) {
            Utils.log_exception(e,"Failed to initialize logging service.");
        }
        
        Logger logger = LogManager.getRootLogger();
        //context.setAttribute("logger", logger);
        
        logger.trace("Logger Ready.");
        logger.error("Logger Ready.");
        
        //set colormaps
        PyColorMaps colormaps = new PyColorMaps(install_path + File.separator + "db" + File.separator + "py_colormaps");
        context.setAttribute("colormaps", colormaps);
        
        ColorPalette categorical_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "categorical_color_palette.txt");
        context.setAttribute("categorical_palatte", categorical_palette);
        
        ColorPalette continuous_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "continuous_color_palette.txt");
        context.setAttribute("continuous_palatte", continuous_palette);
        
        ColorPalette gene_group_color_palette = new ColorPalette(install_path + File.separator + "db" + File.separator + "gene_group_color_palette.txt");
        context.setAttribute("gene_group_color_palette", gene_group_color_palette);
        
        HashMap <Integer, String> identifier_name_map = AnalysisContainer.createIdentifierNameMap();
        context.setAttribute("identifier_name_map", identifier_name_map);
        
        HashMap <String, Integer> identifier_index_map = AnalysisContainer.createIdentifierIndexMap();
        context.setAttribute("identifier_index_map", identifier_index_map);
        
        //get analytics_server_address from pyslide_queue_manager
        context.setAttribute("analytics_server_address", "http://127.0.0.1:5000");
        
        /*
        Start spark session
        */
        
        //System.setProperty("hadoop.home.dir", "C:\\hadoop");
        
        SparkSession spark = SparkSession.builder()
                                         .appName("MultiSLIDE")
                                         .config("key", "value")
                                         .master("local[*]")
                                         .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                                         .config("spark.executor.memory", "4g")
                                         .config("spark.kryo.unsafe", "true")
                                         .config("spark.broadcast.compress", "false")
                                         .config("spark.default.parallelism", N_SPARK_PARTITIONS + "")
                                         .config("spark.sql.shuffle.partitions", N_SPARK_PARTITIONS + "")
                                         .config("spark.sql.inMemoryColumnarStorage.compressed","false")
                                         .config("spark.sql.warehouse.dir", install_path + File.separator + "temp")
                                         .getOrCreate();
        //this.spark_session.conf().set("spark.executor.memory", "10g");
        spark.sparkContext().setLogLevel("OFF");
        context.setAttribute("spark", spark);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
    }
    
}