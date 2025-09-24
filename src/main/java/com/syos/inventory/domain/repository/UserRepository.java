package com.syos.inventory.domain.repository;

import com.syos.inventory.domain.entity.User;
import com.syos.inventory.domain.value.Username;
import com.syos.inventory.domain.value.UserRole;
import com.syos.shared.exceptions.technical.DatabaseException;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 */
public interface UserRepository {
    
    /**
     * Save a user (create or update)
     * @param user User to save
     * @return Saved user with ID
     * @throws DatabaseException if save operation fails
     */
    User save(User user) throws DatabaseException;
    
    /**
     * Find user by ID
     * @param id User ID
     * @return User if found
     * @throws DatabaseException if find operation fails
     */
    Optional<User> findById(Long id) throws DatabaseException;
    
    /**
     * Find user by username
     * @param username Username to search for
     * @return User if found
     * @throws DatabaseException if find operation fails
     */
    Optional<User> findByUsername(Username username) throws DatabaseException;
    
    /**
     * Find user by email
     * @param email Email to search for
     * @return User if found
     * @throws DatabaseException if find operation fails
     */
    Optional<User> findByEmail(String email) throws DatabaseException;
    
    /**
     * Find all users
     * @return List of all users
     * @throws DatabaseException if find operation fails
     */
    List<User> findAll() throws DatabaseException;
    
    /**
     * Find users by role
     * @param role User role
     * @return List of users with specified role
     * @throws DatabaseException if find operation fails
     */
    List<User> findByRole(UserRole role) throws DatabaseException;
    
    /**
     * Find active users
     * @param active Active status
     * @return List of users with specified active status
     * @throws DatabaseException if find operation fails
     */
    List<User> findByActive(boolean active) throws DatabaseException;
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return true if username exists
     * @throws DatabaseException if check operation fails
     */
    boolean existsByUsername(Username username) throws DatabaseException;
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return true if email exists
     * @throws DatabaseException if check operation fails
     */
    boolean existsByEmail(String email) throws DatabaseException;
    
    /**
     * Delete user by ID
     * @param id User ID
     * @return true if user was deleted
     * @throws DatabaseException if delete operation fails
     */
    boolean deleteById(Long id) throws DatabaseException;
    
    /**
     * Count total users
     * @return Total number of users
     * @throws DatabaseException if count operation fails
     */
    long count() throws DatabaseException;
    
    /**
     * Count users by role
     * @param role User role
     * @return Number of users with specified role
     * @throws DatabaseException if count operation fails
     */
    long countByRole(UserRole role) throws DatabaseException;
    
    /**
     * Update user's last login timestamp
     * @param userId User ID
     * @return true if update was successful
     * @throws DatabaseException if update operation fails
     */
    boolean updateLastLogin(Long userId) throws DatabaseException;
}