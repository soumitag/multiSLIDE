package org.cssblab.multislide.filters;


import java.io.IOException;
import java.io.Serializable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cssblab.multislide.utils.Utils;
 
/**
 * Servlet Filter implementation class CORSFilter
 */
// Enable it for Servlet 3.x implementations
/* @ WebFilter(asyncSupported = true, urlPatterns = { "/*" }) */
public class CORSFilter implements Filter, Serializable {
    
    private static final long serialVersionUID = 1L;
    /**
     * Default constructor.
     */
    public CORSFilter() {
        // TODO Auto-generated constructor stub
    }
 
    /**
     * @see Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }
 
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
 
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        Utils.log_info("CORSFilter HTTP Request: " + request.getMethod());
        
        String profile = "docker_remote";
        //String profile = "local";
        //String profile = "server_109";
        
        if (profile.equals("docker_remote")) {
            /*
                For remote (Docker) installation
            */
            ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "http://localhost:8080/multislide");
            
        } else if (profile.equals("local")) {
            /*
                For local development
            */
            ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "http://localhost:4200");
            //((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "*");
            
        } else if (profile.equals("server_109")) {
            /*
                For installation on 109 server
            */
            ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "http://137.132.97.109:56695/multislide");
        }
        
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Credentials", "true");
        ((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods","GET, OPTIONS, HEAD, PUT, POST");
        ((HttpServletResponse) servletResponse).addHeader("X-XSS-Protection", "1; mode=block");
        ((HttpServletResponse) servletResponse).addHeader("X-Frame-Options", "SAMEORIGIN");
        ((HttpServletResponse) servletResponse).addHeader("X-Content-Type-Options", "nosniff");
 
        // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
        /*
        if (request.getMethod().equals("OPTIONS")) {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }
        */
 
        // pass the request along the filter chain
        chain.doFilter(request, servletResponse);
    }
 
    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated method stub
    }
 
}