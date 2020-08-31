/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cssblab.multislide.utils;

import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.cssblab.multislide.beans.data.ServerResponse;

/**
 *
 * @author soumitaghosh
 */
public class HttpClientManager implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public String request_string;
    public String server_address;
    private final CloseableHttpClient httpClient;
    //String request_string = "http://127.0.0.1:5000/do_significance_testing?file_id=112233&test_type=one_way_anova&use_parametric=True&n_rows=7729&n_cols=73&fdr_rate=0.5";

    public HttpClientManager(String server_address) {
        this.httpClient = HttpClients.createDefault();
        this.server_address = server_address;
    }
    
    public ServerResponse doGet(String resource, HashMap <String, String> params) {
        try {
            this.request_string = server_address + "/" + resource;
            boolean isFirst = true;
            for (Map.Entry <String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (isFirst) {
                    isFirst = false;
                    this.request_string += "?" + key + "=" + value;
                } else {
                    this.request_string += "&" + key + "=" + value;
                }
            }
        } catch (Exception e) {
            return new ServerResponse(0, "Exception parsing params in HttpClientManager.doGet()", e.getLocalizedMessage());
        }
        try {
            return sendGet();
        } catch (Exception e) {
            return new ServerResponse(0, "Exception in HttpClientManager.doGet()", e.getLocalizedMessage());
        }
    }

    // one instance, reuse
    private void close() throws IOException {
        this.httpClient.close();
    }

    public ServerResponse sendGet() throws Exception {

        Utils.log_info("Request:" + this.request_string);
        HttpGet request = new HttpGet(this.request_string);

        try (CloseableHttpResponse response = this.httpClient.execute(request)) {

            // Get HttpResponse Status
            Utils.log_info("Response:" + response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            Utils.log_info(headers.toString());

            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                Gson g = new Gson();
                ServerResponse resp = g.fromJson(result, ServerResponse.class);
                Utils.log_info(result);
                return resp;
            }
            
            return new ServerResponse(0, "Entity is null", "");

        } catch (Exception e) {

            Utils.log_exception(e, "Exception in HttpClientManager.sendGet()");
            return new ServerResponse(0, "Exception in HttpClientManager.sendGet()", e.getLocalizedMessage());
            
        }

    }

}
