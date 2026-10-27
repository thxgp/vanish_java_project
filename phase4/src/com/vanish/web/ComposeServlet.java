package com.vanish.web;

import com.vanish.exception.InvalidInputException;
import com.vanish.model.Note;
import com.vanish.model.User;
import com.vanish.service.NoteService;
import com.vanish.service.impl.NoteServiceImpl;

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
 * Handles secret note creation requests for authenticated users.
 */
@WebServlet(name = "ComposeServlet", urlPatterns = {"/compose"})
public class ComposeServlet extends HttpServlet {

    private final NoteService noteService = new NoteServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/compose.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=Please+log+in+to+create+notes.");
            return;
        }

        User user = (User) session.getAttribute("user");
        String secretText = req.getParameter("secretText");
        String expiryStr = req.getParameter("expiryMinutes");
        String viewsStr = req.getParameter("views");

        int expiry = 10;
        int views = 1;
        try {
            if (expiryStr != null && !expiryStr.trim().isEmpty()) {
                expiry = Integer.parseInt(expiryStr.trim());
            }
            if (viewsStr != null && !viewsStr.trim().isEmpty()) {
                views = Integer.parseInt(viewsStr.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        try {
            Note note = noteService.createNote(user.getUsername(), secretText, expiry, views);

            // Check if AJAX request requested JSON response
            String accept = req.getHeader("Accept");
            if (accept != null && accept.contains("application/json")) {
                resp.setContentType("application/json;charset=UTF-8");
                String json = String.format(
                        "{\"success\":true,\"id\":%d,\"token\":\"%s\",\"shareLink\":\"%s/read?t=%s\",\"ciphertext\":\"%s\"}",
                        note.getId(),
                        note.getToken(),
                        req.getContextPath(),
                        note.getToken(),
                        note.getCiphertextPreview().replace("\"", "\\\"")
                );
                resp.getWriter().write(json);
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/dashboard?created=" + note.getToken());
        } catch (InvalidInputException e) {
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/compose.html?error=" + errorMsg);
        } catch (Exception e) {
            String errorMsg = URLEncoder.encode("Error creating note: " + e.getMessage(), StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/compose.html?error=" + errorMsg);
        }
    }
}
