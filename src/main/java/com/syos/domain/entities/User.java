package com.syos.domain.entities;

import com.syos.shared.valueobjects.Email;
import com.syos.shared.valueobjects.UserCode;
import com.syos.shared.valueobjects.Timestamp;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User entity representing a user in the SYOS inventory system.
 * 
 * This class follows Domain-Driven Design principles and encapsulates
 * all user-related business logic and rules.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class User {
    
    /**
     * Enumeration for user roles in the system.
     */
    public enum Role {
        ADMIN("Administrator", "Full system access"),
        MANAGER("Manager", "Store management access"),
        CASHIER("Cashier", "Point of sale access"),
        INVENTORY_CLERK("Inventory Clerk", "Inventory management access");
        
        private final String displayName;
        private final String description;
        
        Role(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    private final UserCode userCode;
    private String firstName;
    private String lastName;
    private Email email;
    private String phoneNumber;
    private Role role;
    private String passwordHash;
    private boolean isActive;
    private final Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastLoginAt;
    
    // Business rule constants
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * Creates a new User entity.
     * This constructor is used for creating new users.
     * 
     * @param userCode the unique user code
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param phoneNumber the user's phone number (optional)
     * @param role the user's role
     * @param passwordHash the hashed password
     * @throws IllegalArgumentException if any parameter violates business rules
     */
    public User(UserCode userCode, String firstName, String lastName, 
                Email email, String phoneNumber, Role role, String passwordHash) {
        this.userCode = Objects.requireNonNull(userCode, "User code cannot be null");
        this.createdAt = Timestamp.now();
        this.updatedAt = this.createdAt;
        this.isActive = true; // New users are active by default
        
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setRole(role);
        setPasswordHash(passwordHash);
    }
    
    /**
     * Reconstitutes a User entity from persistence.
     * This constructor is used when loading users from the database.
     * 
     * @param userCode the unique user code
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param phoneNumber the user's phone number
     * @param role the user's role
     * @param passwordHash the hashed password
     * @param isActive the active status
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     * @param lastLoginAt the last login timestamp
     */
    public User(UserCode userCode, String firstName, String lastName, 
                Email email, String phoneNumber, Role role, String passwordHash,
                boolean isActive, Timestamp createdAt, Timestamp updatedAt, Timestamp lastLoginAt) {
        this.userCode = Objects.requireNonNull(userCode, "User code cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at cannot be null");
        this.lastLoginAt = lastLoginAt; // Can be null if user never logged in
        this.isActive = isActive;
        
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setRole(role);
        setPasswordHash(passwordHash);
    }
    
    /**
     * Gets the user code.
     * 
     * @return the user code
     */
    public UserCode getUserCode() {
        return userCode;
    }
    
    /**
     * Gets the first name.
     * 
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Sets the first name.
     * 
     * @param firstName the first name
     * @throws IllegalArgumentException if first name violates business rules
     */
    public void setFirstName(String firstName) {
        Objects.requireNonNull(firstName, "First name cannot be null");
        
        String trimmedFirstName = firstName.trim();
        if (trimmedFirstName.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("First name must be at least %d characters long", MIN_NAME_LENGTH)
            );
        }
        
        if (trimmedFirstName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("First name cannot exceed %d characters", MAX_NAME_LENGTH)
            );
        }
        
        this.firstName = trimmedFirstName;
        updateTimestamp();
    }
    
    /**
     * Gets the last name.
     * 
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Sets the last name.
     * 
     * @param lastName the last name
     * @throws IllegalArgumentException if last name violates business rules
     */
    public void setLastName(String lastName) {
        Objects.requireNonNull(lastName, "Last name cannot be null");
        
        String trimmedLastName = lastName.trim();
        if (trimmedLastName.length() < MIN_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Last name must be at least %d characters long", MIN_NAME_LENGTH)
            );
        }
        
        if (trimmedLastName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Last name cannot exceed %d characters", MAX_NAME_LENGTH)
            );
        }
        
        this.lastName = trimmedLastName;
        updateTimestamp();
    }
    
    /**
     * Gets the email address.
     * 
     * @return the email address
     */
    public Email getEmail() {
        return email;
    }
    
    /**
     * Sets the email address.
     * 
     * @param email the email address
     * @throws IllegalArgumentException if email is null
     */
    public void setEmail(Email email) {
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        updateTimestamp();
    }
    
    /**
     * Gets the phone number.
     * 
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * Sets the phone number.
     * 
     * @param phoneNumber the phone number (can be null)
     * @throws IllegalArgumentException if phone number format is invalid
     */
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            String trimmedPhone = phoneNumber.trim();
            if (!trimmedPhone.isEmpty() && !PHONE_PATTERN.matcher(trimmedPhone).matches()) {
                throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
            }
            this.phoneNumber = trimmedPhone.isEmpty() ? null : trimmedPhone;
        } else {
            this.phoneNumber = null;
        }
        updateTimestamp();
    }
    
    /**
     * Gets the user role.
     * 
     * @return the user role
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Sets the user role.
     * 
     * @param role the user role
     * @throws IllegalArgumentException if role is null
     */
    public void setRole(Role role) {
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        updateTimestamp();
    }
    
    /**
     * Gets the password hash.
     * 
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    /**
     * Sets the password hash.
     * 
     * @param passwordHash the password hash
     * @throws IllegalArgumentException if password hash is null or empty
     */
    public void setPasswordHash(String passwordHash) {
        Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        
        if (passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        
        this.passwordHash = passwordHash.trim();
        updateTimestamp();
    }
    
    /**
     * Validates a plain text password against business rules.
     * 
     * @param password the plain text password
     * @throws IllegalArgumentException if password violates business rules
     */
    public static void validatePassword(String password) {
        Objects.requireNonNull(password, "Password cannot be null");
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH)
            );
        }
        
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        
        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
    
    /**
     * Checks if the user is active.
     * 
     * @return true if the user is active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Activates the user.
     * Active users can log in and perform operations.
     */
    public void activate() {
        if (!this.isActive) {
            this.isActive = true;
            updateTimestamp();
        }
    }
    
    /**
     * Deactivates the user.
     * Inactive users cannot log in or perform operations.
     */
    public void deactivate() {
        if (this.isActive) {
            this.isActive = false;
            updateTimestamp();
        }
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     * 
     * @return the last update timestamp
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Gets the last login timestamp.
     * 
     * @return the last login timestamp (can be null if user never logged in)
     */
    public Timestamp getLastLoginAt() {
        return lastLoginAt;
    }
    
    /**
     * Records a successful login.
     * This method should be called when the user successfully authenticates.
     */
    public void recordLogin() {
        this.lastLoginAt = Timestamp.now();
        updateTimestamp();
    }
    
    /**
     * Updates the last modification timestamp.
     * This method is called automatically when any property is changed.
     */
    private void updateTimestamp() {
        this.updatedAt = Timestamp.now();
    }
    
    /**
     * Gets the full name.
     * 
     * @return the full name (first name + last name)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Checks if the user has admin privileges.
     * 
     * @return true if the user is an admin
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    /**
     * Checks if the user has manager privileges.
     * 
     * @return true if the user is a manager or admin
     */
    public boolean isManager() {
        return role == Role.ADMIN || role == Role.MANAGER;
    }
    
    /**
     * Checks if the user can access inventory functions.
     * 
     * @return true if the user can access inventory
     */
    public boolean canAccessInventory() {
        return role == Role.ADMIN || role == Role.MANAGER || role == Role.INVENTORY_CLERK;
    }
    
    /**
     * Checks if the user can perform sales operations.
     * 
     * @return true if the user can perform sales
     */
    public boolean canPerformSales() {
        return role == Role.ADMIN || role == Role.MANAGER || role == Role.CASHIER;
    }
    
    /**
     * Validates if the user can perform a specific operation based on their role.
     * 
     * @return true if the user can login and is active
     */
    public boolean canLogin() {
        return isActive;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userCode, user.userCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userCode);
    }
    
    @Override
    public String toString() {
        return String.format("User{code=%s, name='%s', email=%s, role=%s, active=%s}",
            userCode, getFullName(), email, role, isActive);
    }
}