package com.vanish.web;

import com.vanish.exception.InvalidInputException;
import com.vanish.exception.UserAlreadyExistsException;
import com.vanish.model.User;
import com.vanish.service.UserService;
import com.vanish.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles user account creation via GET/POST as required by CLO1 & CLO2.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            User newUser = userService.register(username, password, confirmPassword);
            String successMsg = URLEncoder.encode("Account successfully created for " + newUser.getUsername() + "! Please sign in.", StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/login?msg=" + successMsg);
        } catch (InvalidInputException | UserAlreadyExistsException e) {
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/register.html?error=" + errorMsg);
        } catch (Exception e) {
            String errorMsg = URLEncoder.encode("Registration error: " + e.getMessage(), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/register.html?error=" + errorMsg);
        }
    }
}
