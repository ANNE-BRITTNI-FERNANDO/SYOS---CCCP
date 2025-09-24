package com.syos.domain.repositories;

import com.syos.domain.entities.User;
import com.syos.shared.valueobjects.Email;
import com.syos.shared.valueobjects.UserCode;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * 
 * This interface follows the Repository pattern and defines the contract
 * for User persistence operations. It belongs to the domain layer
 * and follows the Dependency Inversion Principle.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public interface UserRepository {
    
    /**
     * Saves a user to the repository.
     * If the user already exists, it will be updated.
     * 
     * @param user the user to save
     * @return the saved user
     * @throws IllegalArgumentException if user is null
     */
    User save(User user);
    
    /**
     * Finds a user by their user code.
     * 
     * @param userCode the user code
     * @return an Optional containing the user if found, empty otherwise
     * @throws IllegalArgumentException if userCode is null
     */
    Optional<User> findByUserCode(UserCode userCode);
    
    /**
     * Finds a user by their user code string.
     * 
     * @param userCode the user code as string
     * @return an Optional containing the user if found, empty otherwise
     * @throws IllegalArgumentException if userCode is null or empty
     */
    Optional<User> findByUserCode(String userCode);
    
    /**
     * Finds a user by their email address.
     * 
     * @param email the email address
     * @return an Optional containing the user if found, empty otherwise
     * @throws IllegalArgumentException if email is null
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * Finds a user by their email address string.
     * 
     * @param email the email address as string
     * @return an Optional containing the user if found, empty otherwise
     * @throws IllegalArgumentException if email is null or empty
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds all users in the repository.
     * 
     * @return a list of all users
     */
    List<User> findAll();
    
    /**
     * Finds all active users.
     * 
     * @return a list of active users
     */
    List<User> findAllActive();
    
    /**
     * Finds users by role.
     * 
     * @param role the user role
     * @return a list of users with the specified role
     * @throws IllegalArgumentException if role is null
     */
    List<User> findByRole(User.Role role);
    
    /**
     * Finds active users by role.
     * 
     * @param role the user role
     * @return a list of active users with the specified role
     * @throws IllegalArgumentException if role is null
     */
    List<User> findActiveByRole(User.Role role);
    
    /**
     * Finds users by name (case-insensitive partial match on first or last name).
     * 
     * @param name the name or partial name
     * @return a list of users matching the name
     * @throws IllegalArgumentException if name is null or empty
     */
    List<User> findByNameContaining(String name);
    
    /**
     * Checks if a user exists with the given user code.
     * 
     * @param userCode the user code
     * @return true if a user exists with the given code
     * @throws IllegalArgumentException if userCode is null
     */
    boolean existsByUserCode(UserCode userCode);
    
    /**
     * Checks if a user exists with the given user code string.
     * 
     * @param userCode the user code as string
     * @return true if a user exists with the given code
     * @throws IllegalArgumentException if userCode is null or empty
     */
    boolean existsByUserCode(String userCode);
    
    /**
     * Checks if a user exists with the given email address.
     * 
     * @param email the email address
     * @return true if a user exists with the given email
     * @throws IllegalArgumentException if email is null
     */
    boolean existsByEmail(Email email);
    
    /**
     * Checks if a user exists with the given email address string.
     * 
     * @param email the email address as string
     * @return true if a user exists with the given email
     * @throws IllegalArgumentException if email is null or empty
     */
    boolean existsByEmail(String email);
    
    /**
     * Deletes a user from the repository.
     * Note: This should be used carefully as it permanently removes the user.
     * Consider deactivating users instead of deleting them.
     * 
     * @param user the user to delete
     * @throws IllegalArgumentException if user is null
     */
    void delete(User user);
    
    /**
     * Deletes a user by their user code.
     * Note: This should be used carefully as it permanently removes the user.
     * Consider deactivating users instead of deleting them.
     * 
     * @param userCode the user code
     * @throws IllegalArgumentException if userCode is null
     */
    void deleteByUserCode(UserCode userCode);
    
    /**
     * Counts the total number of users.
     * 
     * @return the total number of users
     */
    long count();
    
    /**
     * Counts the number of active users.
     * 
     * @return the number of active users
     */
    long countActive();
    
    /**
     * Counts the number of users with a specific role.
     * 
     * @param role the user role
     * @return the number of users with the specified role
     * @throws IllegalArgumentException if role is null
     */
    long countByRole(User.Role role);
    
    /**
     * Counts the number of active users with a specific role.
     * 
     * @param role the user role
     * @return the number of active users with the specified role
     * @throws IllegalArgumentException if role is null
     */
    long countActiveByRole(User.Role role);
}