package com.vanish.service.impl;

import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.model.Note;
import com.vanish.model.NoteStatus;
import com.vanish.model.User;
import com.vanish.service.AdminService;
import com.vanish.store.MemoryStore;

import java.util.List;

public class AdminServiceImpl implements AdminService {

    private final MemoryStore store;

    public AdminServiceImpl() {
        this.store = MemoryStore.getInstance();
    }

    public AdminServiceImpl(MemoryStore store) {
        this.store = store;
    }

    @Override
    public Stats getStats() {
        List<Note> notes = store.findAllNotes();
        int pending = 0, unread = 0, burned = 0, expired = 0, rejected = 0;
        for (Note n : notes) {
            switch (n.getStatus()) {
                case PENDING -> pending++;
                case BURNED -> burned++;
                case EXPIRED -> expired++;
                case REJECTED -> rejected++;
                default -> unread++;
            }
        }
        return new Stats(store.findAllUsers().size(), notes.size(), pending, unread, burned, expired, rejected);
    }

    @Override
    public List<User> listUsers() {
        return store.findAllUsers();
    }

    @Override
    public List<Note> listNotes() {
        return store.findAllNotes();
    }

    @Override
    public void deleteUser(String username) throws InvalidInputException {
        User target = store.findUserByUsername(username);
        if (target == null) {
            throw new InvalidInputException("User not found: " + username);
        }
        // Refusing every admin also covers self-delete and keeps at least one admin alive.
        if (target.isAdmin()) {
            throw new InvalidInputException("Admin accounts cannot be deleted.");
        }
        store.deleteUser(target.getUsername());
    }

    @Override
    public void deleteNote(String token) throws NoteNotFoundException {
        if (!store.deleteNote(token)) {
            throw new NoteNotFoundException("No note found with token: " + token);
        }
    }

    @Override
    public void burnNote(String token) throws NoteNotFoundException {
        Note note = store.findNoteByToken(token);
        if (note == null) {
            throw new NoteNotFoundException("No note found with token: " + token);
        }
        note.burn();
    }

    @Override
    public void approveNote(String token) throws NoteNotFoundException, InvalidInputException {
        pendingNote(token).approve();
    }

    @Override
    public void rejectNote(String token) throws NoteNotFoundException, InvalidInputException {
        pendingNote(token).reject();
    }

    @Override
    public List<Note> listPendingNotes() {
        List<Note> pending = store.findAllNotes();
        pending.removeIf(n -> !n.isPending());
        return pending;
    }

    private Note pendingNote(String token) throws NoteNotFoundException, InvalidInputException {
        Note note = store.findNoteByToken(token);
        if (note == null) {
            throw new NoteNotFoundException("No note found with token: " + token);
        }
        if (!note.isPending()) {
            throw new InvalidInputException("Note " + token + " is not waiting for approval.");
        }
        return note;
    }
}
