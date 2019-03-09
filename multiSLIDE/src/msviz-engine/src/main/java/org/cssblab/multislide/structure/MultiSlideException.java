/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.structure;

/**
 *
 * @author abhikdatta
 */
public class MultiSlideException extends Exception {
    
    public MultiSlideException() {
        
    }
    
    public MultiSlideException (String message) {
        super(message);
    }
    
    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
