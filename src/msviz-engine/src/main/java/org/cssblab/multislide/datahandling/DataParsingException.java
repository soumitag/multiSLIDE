/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.datahandling;

import java.io.Serializable;

/**
 *
 * @author soumitag
 */
public class DataParsingException extends Exception implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public DataParsingException() {
        
    }
    
    public DataParsingException (String message) {
        super(message);
    }
    
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}

