package com.vanish.web;

import com.vanish.exception.AuthenticationException;
import com.vanish.exception.InvalidInputException;
import com.vanish.model.User;
import com.vanish.service.UserService;
import com.vanish.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles user authentication and session establishment via GET/POST.
 * Fulfills CLO1, CLO2, and CLO3 requirements.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User authenticatedUser = userService.authenticate(username, password);

            // Establish secure session with HttpSession tracking
            HttpSession session = req.getSession(true);
            session.setAttribute("user", authenticatedUser);
            session.setAttribute("username", authenticatedUser.getUsername());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes session timeout

            resp.sendRedirect(req.getContextPath() + (authenticatedUser.isAdmin() ? "/admin" : "/dashboard"));
        } catch (InvalidInputException | AuthenticationException e) {
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/login?error=" + errorMsg);
        } catch (Exception e) {
            String errorMsg = URLEncoder.encode("Login failed: " + e.getMessage(), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/login?error=" + errorMsg);
        }
    }
}
