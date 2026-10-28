package com.vanish.service;

import com.vanish.exception.InvalidInputException;
import com.vanish.exception.NoteBurnedException;
import com.vanish.exception.NoteExpiredException;
import com.vanish.exception.NoteNotFoundException;
import com.vanish.exception.NotePendingException;
import com.vanish.model.Note;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface defining note creation, lookup, and self-destruction operations.
 */
public interface NoteService {

    /**
     * Creates a new self-destructing secret note.
     *
     * @param ownerUsername username of the creator
     * @param secretText    secret content (max 500 chars)
     * @param expiryMinutes duration until note expires (1-1440 mins)
     * @param views         number of permitted views before burning (1-5)
     * @return the created Note
     * @throws InvalidInputException if any input violates validation constraints
     */
    Note createNote(String ownerUsername, String secretText, int expiryMinutes, int views)
            throws InvalidInputException;

    /**
     * Creates a note with explicit timestamps (used for testing expired notes).
     */
    Note createNoteWithTimestamps(String ownerUsername, String secretText,
                                  LocalDateTime createdAt, LocalDateTime expiresAt, int views)
            throws InvalidInputException;

    /**
     * Accesses and burns a note identified by its URL token.
     *
     * @param token unique URL-safe token
     * @return the secret message text
     * @throws NoteNotFoundException if no note exists for the given token
     * @throws NoteBurnedException   if the note was already viewed and destroyed, or rejected by the admin
     * @throws NoteExpiredException  if the note's expiry time has elapsed
     * @throws NotePendingException  if the admin has not approved the note yet
     */
    String readNote(String token)
            throws NoteNotFoundException, NoteBurnedException, NoteExpiredException, NotePendingException;

    /**
     * Retrieves all notes created by a specific user for dashboard display.
     *
     * @param ownerUsername the creator's username
     * @return list of notes belonging to the user
     */
    List<Note> getNotesByOwner(String ownerUsername);

    /**
     * Finds note metadata by token without reading/burning it (for status checks).
     *
     * @param token note token
     * @return the Note object or null if not found
     */
    Note findNoteByToken(String token);
}
