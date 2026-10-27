package com.vanish.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Enforces strict HTTP response headers to disable browser caching.
 * Ensures that confidential secret notes and dashboard pages cannot be viewed
 * via browser history or back button after logout or burning.
 */
@WebFilter(filterName = "NoCacheFilter", urlPatterns = {"/*"})
public class NoCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
        httpResponse.setDateHeader("Expires", 0); // Proxies

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
