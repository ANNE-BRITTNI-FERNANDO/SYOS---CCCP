package com.syos.inventory.domain.value;

import com.syos.inventory.domain.exception.ValidationException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Value object representing a password with encryption
 */
public class Password {
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 100;
    private static final int SALT_LENGTH = 16;
    
    private final String hashedPassword;
    private final String salt;
    
    /**
     * Create a new password (for new users)
     * @param plainPassword Plain text password
     */
    public Password(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new ValidationException("Password cannot be null or empty");
        }
        
        if (plainPassword.length() < MIN_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (plainPassword.length() > MAX_LENGTH) {
            throw new ValidationException("Password cannot exceed " + MAX_LENGTH + " characters");
        }
        
        // Validate password strength
        if (!isValidPassword(plainPassword)) {
            throw new ValidationException("Password must contain at least one letter and one number");
        }
        
        this.salt = generateSalt();
        this.hashedPassword = hashPassword(plainPassword, this.salt);
    }
    
    /**
     * Create password from stored hash and salt (for existing users)
     * @param hashedPassword Already hashed password
     * @param salt Salt used for hashing
     */
    public Password(String hashedPassword, String salt) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new ValidationException("Hashed password cannot be null or empty");
        }
        if (salt == null || salt.trim().isEmpty()) {
            throw new ValidationException("Salt cannot be null or empty");
        }
        
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }
    
    /**
     * Verify if provided plain password matches this password
     * @param plainPassword Plain text password to verify
     * @return true if password matches
     */
    public boolean verify(String plainPassword) {
        if (plainPassword == null) {
            return false;
        }
        
        String hashedInput = hashPassword(plainPassword, this.salt);
        return this.hashedPassword.equals(hashedInput);
    }
    
    /**
     * Get the hashed password for storage
     * @return Hashed password
     */
    public String getHashedPassword() {
        return hashedPassword;
    }
    
    /**
     * Get the salt for storage
     * @return Salt
     */
    public String getSalt() {
        return salt;
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return true if password meets requirements
     */
    private boolean isValidPassword(String password) {
        boolean hasLetter = false;
        boolean hasNumber = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
            
            if (hasLetter && hasNumber) {
                break;
            }
        }
        
        return hasLetter && hasNumber;
    }
    
    /**
     * Generate a random salt
     * @return Base64 encoded salt
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[SALT_LENGTH];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
    
    /**
     * Hash password with salt using SHA-256
     * @param password Plain password
     * @param salt Salt
     * @return Hashed password
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Password password = (Password) obj;
        return hashedPassword.equals(password.hashedPassword) && salt.equals(password.salt);
    }
    
    @Override
    public int hashCode() {
        return hashedPassword.hashCode() + salt.hashCode();
    }
    
    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}