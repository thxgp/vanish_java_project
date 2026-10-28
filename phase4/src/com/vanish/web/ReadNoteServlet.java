package com.vanish.web;

import com.vanish.exception.NoteBurnedException;
import com.vanish.exception.NoteExpiredException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.exception.NotePendingException;
import com.vanish.service.NoteService;
import com.vanish.service.impl.NoteServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles recipient secret retrieval and burn-on-read execution.
 * Fulfills server-side self-destruction logic.
 */
@WebServlet(name = "ReadNoteServlet", urlPatterns = {"/read"})
public class ReadNoteServlet extends HttpServlet {

    private final NoteService noteService = new NoteServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String token = req.getParameter("t");

        if (token == null || token.trim().isEmpty()) {
            req.setAttribute("errorMessage", "Missing secret note token in URL.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
            return;
        }

        token = token.trim();

        try {
            String decryptedSecret = noteService.readNote(token);
            req.setAttribute("secret", decryptedSecret);
            req.setAttribute("token", token);
            req.getRequestDispatcher("/read.jsp").forward(req, resp);
        } catch (NoteBurnedException e) {
            req.setAttribute("token", token);
            req.setAttribute("reason", e.getMessage());
            req.getRequestDispatcher("/burned.jsp").forward(req, resp);
        } catch (NoteExpiredException e) {
            req.setAttribute("token", token);
            req.setAttribute("reason", e.getMessage());
            req.getRequestDispatcher("/expired.jsp").forward(req, resp);
        } catch (NotePendingException e) {
            req.setAttribute("token", token);
            req.getRequestDispatcher("/pending.jsp").forward(req, resp);
        } catch (NoteNotFoundException e) {
            req.setAttribute("token", token);
            req.setAttribute("errorMessage", "Invalid link or note does not exist (Token: " + token + ")");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
