package com.syos.inventory.domain.entity;

import com.syos.inventory.domain.value.Username;
import com.syos.inventory.domain.value.Password;
import com.syos.inventory.domain.value.UserRole;
import com.syos.inventory.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User entity representing a system user
 */
public class User {
    private Long id;
    private final Username username;
    private Password password;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private UserRole role;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    /**
     * Constructor for creating new user
     */
    public User(Username username, Password password, String firstName, 
                String lastName, String email, UserRole role) {
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor for loading existing user from database
     */
    public User(Long id, Username username, Password password, String firstName, 
                String lastName, String email, String phone, String address, UserRole role, boolean active,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }
    
    /**
     * Verify password against plain text password
     * @param plainPassword Plain text password to verify
     * @return true if password matches
     */
    public boolean verifyPassword(String plainPassword) {
        return this.password.verify(plainPassword);
    }
    
    /**
     * Verify password against Password object
     * @param password Password object to verify
     * @return true if password matches
     */
    public boolean verifyPassword(Password password) {
        return this.password.equals(password);
    }
    
    /**
     * Change password
     * @param newPassword New password
     */
    public void changePassword(Password newPassword) {
        this.password = Objects.requireNonNull(newPassword, "New password cannot be null");
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Update user profile
     * @param firstName New first name
     * @param lastName New last name
     * @param email New email
     */
    public void updateProfile(String firstName, String lastName, String email) {
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Record last login
     */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get full name
     * @return Full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Check if user has specific role
     * @param role Role to check
     * @return true if user has the role
     */
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }
    
    /**
     * Check if user can perform admin actions
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return role.isAdmin();
    }
    
    /**
     * Check if user can manage inventory
     * @return true if user can manage inventory
     */
    public boolean canManageInventory() {
        return role.canManageInventory();
    }
    
    /**
     * Check if user can access reports
     * @return true if user can access reports
     */
    public boolean canAccessReports() {
        return role.canAccessReports();
    }
    
    // Validation methods
    private void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First name cannot be null or empty");
        }
        if (firstName.trim().length() > 50) {
            throw new ValidationException("First name cannot exceed 50 characters");
        }
        this.firstName = firstName.trim();
    }
    
    private void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last name cannot be null or empty");
        }
        if (lastName.trim().length() > 50) {
            throw new ValidationException("Last name cannot exceed 50 characters");
        }
        this.lastName = lastName.trim();
    }
    
    private void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        
        String trimmedEmail = email.trim();
        if (trimmedEmail.length() > 100) {
            throw new ValidationException("Email cannot exceed 100 characters");
        }
        
        // Basic email validation
        if (!isValidEmail(trimmedEmail)) {
            throw new ValidationException("Invalid email format");
        }
        
        this.email = trimmedEmail.toLowerCase();
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && 
               email.indexOf("@") > 0 && 
               email.lastIndexOf(".") > email.indexOf("@") &&
               email.lastIndexOf(".") < email.length() - 1;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Username getUsername() {
        return username;
    }
    
    public Password getPassword() {
        return password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username=" + username +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}