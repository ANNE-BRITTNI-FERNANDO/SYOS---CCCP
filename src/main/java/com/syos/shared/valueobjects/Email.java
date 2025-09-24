package com.syos.shared.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * 
 * This class ensures email address validity and provides immutable
 * email handling throughout the SYOS system.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public final class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private final String value;
    
    /**
     * Private constructor for creating Email instances.
     * 
     * @param value the email address string
     */
    private Email(String value) {
        this.value = value.toLowerCase().trim();
    }
    
    /**
     * Creates an Email instance from a string.
     * 
     * @param email the email address string
     * @return new Email instance
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        return new Email(trimmed);
    }
    
    /**
     * Gets the email address value.
     * 
     * @return the email address string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Gets the local part of the email (before @).
     * 
     * @return the local part
     */
    public String getLocalPart() {
        return value.substring(0, value.indexOf('@'));
    }
    
    /**
     * Gets the domain part of the email (after @).
     * 
     * @return the domain part
     */
    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Email email = (Email) obj;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}