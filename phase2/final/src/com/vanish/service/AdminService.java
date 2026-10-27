package com.vanish.service;

import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.model.Note;
import com.vanish.model.User;

import java.util.List;

/**
 * Admin-only operations. Exposes metadata and moderation actions; it never
 * returns secret note text.
 */
public interface AdminService {

    /** Aggregate counters shown on the admin overview. */
    record Stats(int users, int notes, int pending, int unread, int burned, int expired, int rejected) { }

    Stats getStats();

    List<User> listUsers();

    List<Note> listNotes();

    /**
     * Deletes a user and all of their notes.
     *
     * @throws InvalidInputException if the user does not exist or is an admin account
     */
    void deleteUser(String username) throws InvalidInputException;

    /** Permanently removes a note and its link. */
    void deleteNote(String token) throws NoteNotFoundException;

    /** Purges the ciphertext and kills the link, keeping the note visible as BURNED. */
    void burnNote(String token) throws NoteNotFoundException;

    /** Middleman approval: the link goes live and the expiry clock starts. */
    void approveNote(String token) throws NoteNotFoundException, InvalidInputException;

    /** Middleman refusal: ciphertext purged, recipient sees a rejected notice. */
    void rejectNote(String token) throws NoteNotFoundException, InvalidInputException;

    /** Notes waiting for approval, oldest first. */
    List<Note> listPendingNotes();
}
