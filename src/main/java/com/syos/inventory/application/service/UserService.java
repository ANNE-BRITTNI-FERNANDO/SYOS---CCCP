package com.syos.inventory.application.service;

import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.repository.UserRepository;
import com.syos.inventory.domain.value.Username;
import com.syos.inventory.domain.value.Password;
import com.syos.inventory.domain.value.UserRole;
import com.syos.inventory.domain.exception.BusinessException;
import com.syos.inventory.domain.exception.EntityNotFoundException;
import com.syos.inventory.domain.exception.ValidationException;
import com.syos.shared.exceptions.technical.DatabaseException;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Application service for managing users and authentication
 */
public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Authenticate a user with username and password
     * @param username User's username
     * @param password User's password
     * @return Authenticated user
     * @throws ValidationException if credentials are invalid
     */
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
        
        try {
            Username userNameValue = new Username(username.trim());
            
            Optional<User> user = userRepository.findByUsername(userNameValue);
            if (!user.isPresent()) {
                logger.warning("Authentication failed - user not found: " + username);
                throw new ValidationException("Invalid username or password");
            }
            
            if (!user.get().verifyPassword(password)) {
                logger.warning("Authentication failed - invalid password for user: " + username);
                throw new ValidationException("Invalid username or password");
            }
            
            if (!user.get().isActive()) {
                logger.warning("Authentication failed - user inactive: " + username);
                throw new ValidationException("Account is inactive");
            }
            
            logger.info("User authenticated successfully: " + username);
            return user.get();
            
        } catch (ValidationException | BusinessException e) {
            throw e;
        } catch (DatabaseException e) {
            logger.severe("Authentication error for user " + username + ": " + e.getMessage());
            throw new BusinessException("Authentication service temporarily unavailable");
        }
    }
    
    /**
     * Create a new user
     * @param username Username for new user
     * @param password Password for new user
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email
     * @param role User's role
     * @return Created user
     * @throws ValidationException if user data is invalid
     * @throws BusinessException if user already exists
     */
    public User createUser(String username, String password, String firstName, 
                          String lastName, String email, UserRole role) {
        try {
            Username userNameValue = new Username(username);
            
            // Check if user already exists
            if (userRepository.findByUsername(userNameValue).isPresent()) {
                throw new BusinessException("User already exists with username: " + username);
            }
            
            Password passwordValue = new Password(password);
            User user = new User(userNameValue, passwordValue, firstName, lastName, email, role);
            
            User savedUser = userRepository.save(user);
            logger.info("User created successfully: " + username);
            
            return savedUser;
            
        } catch (ValidationException | BusinessException e) {
            throw e;
        } catch (DatabaseException e) {
            logger.severe("Error creating user " + username + ": " + e.getMessage());
            throw new BusinessException("User service temporarily unavailable");
        }
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User
     * @throws EntityNotFoundException if user not found
     */
    public User getUserById(Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                throw new EntityNotFoundException("User not found with ID: " + userId);
            }
            return user.get();
        } catch (DatabaseException e) {
            logger.severe("Database error retrieving user " + userId + ": " + e.getMessage());
            throw new BusinessException("Failed to retrieve user", e);
        }
    }
    
    /**
     * Get user by username
     * @param username Username
     * @return User
     * @throws EntityNotFoundException if user not found
     */
    public User getUserByUsername(String username) {
        try {
            Username userNameValue = new Username(username);
            Optional<User> user = userRepository.findByUsername(userNameValue);
            if (!user.isPresent()) {
                throw new EntityNotFoundException("User not found with username: " + username);
            }
            return user.get();
        } catch (DatabaseException e) {
            logger.severe("Database error retrieving user by username " + username + ": " + e.getMessage());
            throw new BusinessException("Error retrieving user", e);
        } catch (EntityNotFoundException e) {
            throw e;
        }
    }
    
    /**
     * Get user by email
     * @param email User email
     * @return User if found
     * @throws BusinessException if user not found or database error
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("Email cannot be null or empty");
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email.trim());
            if (userOpt.isPresent()) {
                return userOpt.get();
            } else {
                throw new BusinessException("User with email '" + email + "' not found");
            }
        } catch (DatabaseException e) {
            logger.severe("Database error retrieving user by email " + email + ": " + e.getMessage());
            throw new BusinessException("User service temporarily unavailable", e);
        }
    }
    
    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (DatabaseException e) {
            logger.severe("Database error retrieving all users: " + e.getMessage());
            throw new BusinessException("Failed to retrieve users", e);
        }
    }
    
    /**
     * Update user information
     * @param userId User ID
     * @param firstName New first name
     * @param lastName New last name
     * @param email New email
     * @return Updated user
     * @throws EntityNotFoundException if user not found
     */
    public User updateUser(Long userId, String firstName, String lastName, String email) {
        try {
            User user = getUserById(userId);
            user.updateProfile(firstName, lastName, email);
            
            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully: " + user.getUsername().getValue());
            
            return updatedUser;
            
        } catch (DatabaseException e) {
            logger.severe("Database error updating user " + userId + ": " + e.getMessage());
            throw new BusinessException("Failed to update user", e);
        } catch (EntityNotFoundException | ValidationException e) {
            throw e;
        }
    }
    
    /**
     * Change user password
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @throws EntityNotFoundException if user not found
     * @throws ValidationException if current password is incorrect
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        try {
            User user = getUserById(userId);
            
            if (!user.verifyPassword(currentPassword)) {
                throw new ValidationException("Current password is incorrect");
            }
            
            Password newPasswordValue = new Password(newPassword);
            user.changePassword(newPasswordValue);
            userRepository.save(user);
            
            logger.info("Password changed successfully for user: " + user.getUsername().getValue());
            
        } catch (DatabaseException e) {
            logger.severe("Database error changing password for user " + userId + ": " + e.getMessage());
            throw new BusinessException("Failed to change password", e);
        } catch (EntityNotFoundException | ValidationException e) {
            throw e;
        }
    }
    
    /**
     * Activate or deactivate user
     * @param userId User ID
     * @param active New active status
     * @return Updated user
     * @throws EntityNotFoundException if user not found
     */
    public User setUserActive(Long userId, boolean active) {
        try {
            User user = getUserById(userId);
            user.setActive(active);
            
            User updatedUser = userRepository.save(user);
            logger.info("User " + (active ? "activated" : "deactivated") + ": " + user.getUsername().getValue());
            
            return updatedUser;
            
        } catch (DatabaseException e) {
            logger.severe("Database error setting user active status " + userId + ": " + e.getMessage());
            throw new BusinessException("Failed to update user status", e);
        } catch (EntityNotFoundException e) {
            throw e;
        }
    }
    
    /**
     * Get users by role
     * @param role User role
     * @return List of users with specified role
     */
    public List<User> getUsersByRole(UserRole role) {
        try {
            return userRepository.findByRole(role);
        } catch (DatabaseException e) {
            logger.severe("Database error retrieving users by role " + role + ": " + e.getMessage());
            throw new BusinessException("Failed to retrieve users by role", e);
        }
    }
}