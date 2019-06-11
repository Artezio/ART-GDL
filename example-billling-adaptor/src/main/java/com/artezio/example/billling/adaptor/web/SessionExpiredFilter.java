/*
 */
package com.artezio.example.billling.adaptor.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JSF timeout filter.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public class SessionExpiredFilter implements Filter {

    /**
     * JSF timeout filter chain method.
     * 
     * @param in Servlet request.
     * @param out Servlet response.
     * @param chain Filter chain access.
     * @throws IOException @see java.io.IOException
     * @throws ServletException @see javax.servlet.ServletException
     */
    @Override
    public void doFilter(ServletRequest in, ServletResponse out, FilterChain chain) throws IOException, ServletException {
        if (in instanceof HttpServletRequest && out instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest) in;
            HttpServletResponse response = (HttpServletResponse) out;
            String expiredPage = "/expired.xhtml";
            String requestUri = request.getRequestURI().toLowerCase();
            if (!requestUri.contains(expiredPage) 
                    && request.getRequestedSessionId() != null 
                    && !request.isRequestedSessionIdValid()) {
                String expiredUrl = request.getContextPath() + expiredPage;
                String facesRequest = request.getHeader("Faces-Request");
                if (facesRequest != null && facesRequest.equalsIgnoreCase("partial/ajax")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                            .append("<partial-response>")
                            .append("<redirect url=\"").append(expiredUrl).append("\">")
                            .append("</redirect></partial-response>");
                    response.setHeader("Cache-Control", "no-cache");
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("text/xml");
                    PrintWriter pw = response.getWriter();
                    pw.println(sb.toString());
                    pw.flush();
                } else {
                    response.sendRedirect(expiredUrl);
                }
            } else {
                chain.doFilter(in, out);
            }
        }
    }

}
