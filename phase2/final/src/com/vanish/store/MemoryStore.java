package com.vanish.store;

import com.vanish.model.Note;
import com.vanish.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory data store implemented as a Singleton.
 * Uses ConcurrentHashMap to store users and notes without requiring an external database.
 */
public class MemoryStore {

    private static final MemoryStore INSTANCE = new MemoryStore();

    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, Note> notesByToken = new ConcurrentHashMap<>();
    private final AtomicInteger noteIdCounter = new AtomicInteger(1);

    private MemoryStore() {
        // Private constructor for Singleton pattern
    }

    public static MemoryStore getInstance() {
        return INSTANCE;
    }

    // --- User Operations ---

    public void saveUser(User user) {
        if (user != null) {
            usersByUsername.put(user.getUsername().toLowerCase(), user);
        }
    }

    public User findUserByUsername(String username) {
        if (username == null) {
            return null;
        }
        return usersByUsername.get(username.trim().toLowerCase());
    }

    public boolean userExists(String username) {
        return findUserByUsername(username) != null;
    }

    // --- Note Operations ---

    public int getNextNoteId() {
        return noteIdCounter.getAndIncrement();
    }

    public void saveNote(Note note) {
        if (note != null) {
            notesByToken.put(note.getToken(), note);
        }
    }

    public Note findNoteByToken(String token) {
        if (token == null) {
            return null;
        }
        return notesByToken.get(token);
    }

    public List<Note> findNotesByOwner(String ownerUsername) {
        List<Note> ownerNotes = new ArrayList<>();
        if (ownerUsername == null) {
            return ownerNotes;
        }
        for (Note note : notesByToken.values()) {
            if (note.getOwner().equalsIgnoreCase(ownerUsername.trim())) {
                ownerNotes.add(note);
            }
        }
        // Sort by ID ascending for consistent presentation
        ownerNotes.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        return ownerNotes;
    }

    // --- Admin Operations ---

    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>(usersByUsername.values());
        users.sort((a, b) -> a.getRegisteredAt().compareTo(b.getRegisteredAt()));
        return users;
    }

    public List<Note> findAllNotes() {
        List<Note> notes = new ArrayList<>(notesByToken.values());
        notes.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        return notes;
    }

    /** Removes the user and every note they own. Returns false if the user does not exist. */
    public boolean deleteUser(String username) {
        if (username == null) {
            return false;
        }
        String key = username.trim().toLowerCase();
        if (usersByUsername.remove(key) == null) {
            return false;
        }
        notesByToken.values().removeIf(n -> n.getOwner().equalsIgnoreCase(key));
        return true;
    }

    public boolean deleteNote(String token) {
        return token != null && notesByToken.remove(token) != null;
    }

    public void clear() {
        usersByUsername.clear();
        notesByToken.clear();
        noteIdCounter.set(1);
    }
}
