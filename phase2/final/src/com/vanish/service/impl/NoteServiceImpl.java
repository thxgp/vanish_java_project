package com.vanish.service.impl;

import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteBurnedException;
import com.vanish.exception.NoteExpiredException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.model.Note;
import com.vanish.service.NoteService;
import com.vanish.store.MemoryStore;
import com.vanish.util.TokenGenerator;
import com.vanish.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of NoteService managing note lifecycle, cryptographically
 * secure token generation, and self-destructing on read.
 */
public class NoteServiceImpl implements NoteService {

    private final MemoryStore store;

    public NoteServiceImpl() {
        this.store = MemoryStore.getInstance();
    }

    public NoteServiceImpl(MemoryStore store) {
        this.store = store;
    }

    @Override
    public Note createNote(String ownerUsername, String secretText, int expiryMinutes, int views)
            throws InvalidInputException {

        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            throw new InvalidInputException("Note creator username cannot be empty.");
        }
        ValidationUtil.validateNoteText(secretText);
        ValidationUtil.validateExpiry(expiryMinutes);
        ValidationUtil.validateViews(views);

        int id = store.getNextNoteId();
        String token = TokenGenerator.generateToken();

        Note note = new Note(id, token, ownerUsername.trim(), secretText, expiryMinutes, views);
        store.saveNote(note);
        return note;
    }

    @Override
    public Note createNoteWithTimestamps(String ownerUsername, String secretText,
                                         LocalDateTime createdAt, LocalDateTime expiresAt, int views)
            throws InvalidInputException {

        if (ownerUsername == null || ownerUsername.trim().isEmpty()) {
            throw new InvalidInputException("Note creator username cannot be empty.");
        }
        ValidationUtil.validateNoteText(secretText);
        ValidationUtil.validateViews(views);

        int id = store.getNextNoteId();
        String token = TokenGenerator.generateToken();

        Note note = new Note(id, token, ownerUsername.trim(), secretText, createdAt, expiresAt, views);
        store.saveNote(note);
        return note;
    }

    @Override
    public String readNote(String token)
            throws NoteNotFoundException, NoteBurnedException, NoteExpiredException {

        if (token == null || token.trim().isEmpty()) {
            throw new NoteNotFoundException("Invalid or empty link token.");
        }

        Note note = store.findNoteByToken(token.trim());
        if (note == null) {
            throw new NoteNotFoundException("No note found with token: " + token);
        }

        if (note.isBurned()) {
            throw new NoteBurnedException("This secret note has already been read and burned.");
        }

        if (note.isExpired()) {
            throw new NoteExpiredException("This secret note has expired.");
        }

        // Return secret text and burn if views exhausted
        return note.read();
    }

    @Override
    public List<Note> getNotesByOwner(String ownerUsername) {
        return store.findNotesByOwner(ownerUsername);
    }

    @Override
    public Note findNoteByToken(String token) {
        return store.findNoteByToken(token);
    }
}
