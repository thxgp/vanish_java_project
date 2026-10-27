package com.vanish.web;

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
import java.util.List;

/**
 * Controller servlet for the authenticated sender dashboard.
 * Queries note records and forwards to dashboard.jsp.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final NoteService noteService = new NoteServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?error=Session+expired.+Please+log+in.");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        List<Note> notes = noteService.getNotesByOwner(currentUser.getUsername());

        req.setAttribute("user", currentUser);
        req.setAttribute("notes", notes);

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
