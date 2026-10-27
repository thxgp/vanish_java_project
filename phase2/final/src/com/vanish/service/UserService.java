package com.vanish.service;

import com.vanish.exception.AuthenticationException;
import com.vanish.exception.InvalidInputException;
import com.vanish.exception.UserAlreadyExistsException;
import com.vanish.model.User;

/**
 * Service interface defining user lifecycle and authentication operations.
 * Fulfills CLO2 requirements for interface-driven architecture.
 */
public interface UserService {

    /**
     * Registers a new user after validating username and password inputs.
     *
     * @param username        chosen username (3-20 chars, no spaces)
     * @param password        chosen password (min 6 chars)
     * @param confirmPassword confirmation password matching password
     * @return the newly registered User
     * @throws InvalidInputException      if any field fails validation rules
     * @throws UserAlreadyExistsException  if the username is already registered
     */
    User register(String username, String password, String confirmPassword)
            throws InvalidInputException, UserAlreadyExistsException;

    /**
     * Registers an account with the ADMIN role. Used only for startup seeding, never from a public form.
     *
     * @throws InvalidInputException      if username or password fail validation rules
     * @throws UserAlreadyExistsException  if the username is already registered
     */
    User registerAdmin(String username, String password)
            throws InvalidInputException, UserAlreadyExistsException;

    /**
     * Authenticates an existing user with username and password.
     *
     * @param username user's username
     * @param password user's password
     * @return the authenticated User
     * @throws InvalidInputException  if input format is invalid
     * @throws AuthenticationException if credentials are incorrect or user not found
     */
    User authenticate(String username, String password)
            throws InvalidInputException, AuthenticationException;

    /**
     * Finds a user by their username.
     *
     * @param username user's username
     * @return the User if found, or null otherwise
     */
    User findByUsername(String username);
}
