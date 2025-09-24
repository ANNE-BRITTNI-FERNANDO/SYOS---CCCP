package com.syos.inventory.domain.value;

import com.syos.inventory.domain.exception.ValidationException;

/**
 * Value object representing a username
 */
public class Username {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;
    private static final String VALID_PATTERN = "^[a-zA-Z0-9_]+$";
    
    private final String value;
    
    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("Username cannot be null or empty");
        }
        
        String trimmedValue = value.trim();
        
        if (trimmedValue.length() < MIN_LENGTH) {
            throw new ValidationException("Username must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (trimmedValue.length() > MAX_LENGTH) {
            throw new ValidationException("Username cannot exceed " + MAX_LENGTH + " characters");
        }
        
        if (!trimmedValue.matches(VALID_PATTERN)) {
            throw new ValidationException("Username can only contain letters, numbers, and underscores");
        }
        
        this.value = trimmedValue;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Username username = (Username) obj;
        return value.equals(username.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public String toString() {
        return value;
    }
}