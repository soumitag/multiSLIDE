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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
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
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
    }  
}