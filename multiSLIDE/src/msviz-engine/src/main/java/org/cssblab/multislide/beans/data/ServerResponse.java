/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cssblab.multislide.beans.data;

/**
 *
 * @author Soumita
 */
public class ServerResponse {
    
    public int status;
    public String message;
    public String detailed_reason;
    
    public ServerResponse() {
        status = 1;
        message = "success";
        detailed_reason = "";
    }
    
    public ServerResponse(int status, String message, String detailed_reason) {
        this.status = status;
        this.message = message;
        this.detailed_reason = detailed_reason;
    }
    
    public ServerResponse(String message, int status){
        this.message = message;
        this.status = status;
    }
}
