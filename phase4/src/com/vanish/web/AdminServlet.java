package com.vanish.web;

import com.vanish.model.Note;
import com.vanish.service.AdminService;
import com.vanish.service.impl.AdminServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Admin overview, user/note lists and moderation actions. Access guarded by AdminFilter. */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin", "/admin/*"})
public class AdminServlet extends HttpServlet {

    private final AdminService admin = new AdminServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            req.setAttribute("stats", admin.getStats());
            req.getRequestDispatcher("/admin.jsp").forward(req, resp);
        } else if (path.equals("/users")) {
            Map<String, Integer> noteCounts = new HashMap<>();
            for (Note n : admin.listNotes()) noteCounts.merge(n.getOwner(), 1, Integer::sum);
            req.setAttribute("users", admin.listUsers());
            req.setAttribute("noteCounts", noteCounts);
            req.getRequestDispatcher("/admin_users.jsp").forward(req, resp);
        } else if (path.equals("/notes")) {
            req.setAttribute("notes", admin.listNotes());
            req.getRequestDispatcher("/admin_notes.jsp").forward(req, resp);
        } else if (path.equals("/approvals")) {
            req.setAttribute("pending", admin.listPendingNotes());
            req.getRequestDispatcher("/admin_approvals.jsp").forward(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        String base = req.getContextPath() + "/admin/";
        String list;
        String msg;
        try {
            if ("/users/delete".equals(path)) {
                list = "users";
                admin.deleteUser(req.getParameter("username"));
                msg = "User deleted.";
            } else if ("/notes/delete".equals(path)) {
                list = "notes";
                admin.deleteNote(req.getParameter("token"));
                msg = "Note deleted.";
            } else if ("/notes/burn".equals(path)) {
                list = "notes";
                admin.burnNote(req.getParameter("token"));
                msg = "Note burned.";
            } else if ("/notes/approve".equals(path)) {
                list = "approvals";
                admin.approveNote(req.getParameter("token"));
                msg = "Note approved.";
            } else if ("/notes/reject".equals(path)) {
                list = "approvals";
                admin.rejectNote(req.getParameter("token"));
                msg = "Note rejected.";
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (Exception e) {
            list = path.startsWith("/users") ? "users"
                    : path.equals("/notes/approve") || path.equals("/notes/reject") ? "approvals" : "notes";
            resp.sendRedirect(base + list + "?error=" + enc(e.getMessage()));
            return;
        }
        resp.sendRedirect(base + list + "?msg=" + enc(msg));
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "Action failed." : s, StandardCharsets.UTF_8);
    }
}
