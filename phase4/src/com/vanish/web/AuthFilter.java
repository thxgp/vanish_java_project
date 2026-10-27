package com.vanish.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Access-control filter that restricts protected dashboard & note creation
 * routes to authenticated users with an active HttpSession.
 * Fulfills CLO1, CLO2, CLO3 syllabus requirement.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/dashboard", "/dashboard.jsp", "/compose", "/compose.jsp"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            String redirectUrl = httpRequest.getContextPath() + "/login?error=Access+restricted.+Please+login+first.";
            httpResponse.sendRedirect(redirectUrl);
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
