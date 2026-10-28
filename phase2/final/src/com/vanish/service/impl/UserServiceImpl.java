package com.vanish.service.impl;

import com.vanish.exception.AuthenticationException;
import com.vanish.exception.InvalidInputException;
import com.vanish.exception.UserAlreadyExistsException;
import com.vanish.model.Role;
import com.vanish.model.User;
import com.vanish.service.UserService;
import com.vanish.store.MemoryStore;
import com.vanish.util.ValidationUtil;

/**
 * Implementation of UserService using MemoryStore and ValidationUtil.
 */
public class UserServiceImpl implements UserService {

    private final MemoryStore store;

    public UserServiceImpl() {
        this.store = MemoryStore.getInstance();
    }

    public UserServiceImpl(MemoryStore store) {
        this.store = store;
    }

    @Override
    public User register(String username, String password, String confirmPassword)
            throws InvalidInputException, UserAlreadyExistsException {

        // Validate individual fields and matching passwords
        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);
        ValidationUtil.validatePasswordMatch(password, confirmPassword);

        String trimmedUsername = username.trim();

        // Check for duplicate username
        if (store.userExists(trimmedUsername)) {
            throw new UserAlreadyExistsException("The username '" + trimmedUsername + "' is already registered.");
        }

        User newUser = new User(trimmedUsername, password);
        store.saveUser(newUser);
        return newUser;
    }

    @Override
    public User registerAdmin(String username, String password)
            throws InvalidInputException, UserAlreadyExistsException {

        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);

        String trimmedUsername = username.trim();
        if (store.userExists(trimmedUsername)) {
            throw new UserAlreadyExistsException("The username '" + trimmedUsername + "' is already registered.");
        }

        User admin = new User(trimmedUsername, password, Role.ADMIN);
        store.saveUser(admin);
        return admin;
    }

    @Override
    public User authenticate(String username, String password)
            throws InvalidInputException, AuthenticationException {

        ValidationUtil.validateUsername(username);
        ValidationUtil.validatePassword(password);

        User user = store.findUserByUsername(username);
        if (user == null || !user.matches(username, password)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return user;
    }

    @Override
    public User findByUsername(String username) {
        return store.findUserByUsername(username);
    }
}
