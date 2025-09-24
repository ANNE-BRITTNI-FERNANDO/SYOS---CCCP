package com.syos.shared.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * UserCode value object that ensures user code validity and immutability.
 * 
 * This class follows the Value Object pattern from Domain-Driven Design,
 * ensuring that user codes are always valid and follow the SYOS format.
 * 
 * Format: USR-XXXXXXXX (where X is alphanumeric)
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public final class UserCode {
    
    private static final String PREFIX = "USR-";
    private static final int CODE_LENGTH = 8; // Length after prefix
    private static final int TOTAL_LENGTH = PREFIX.length() + CODE_LENGTH;
    private static final Pattern CODE_PATTERN = Pattern.compile("^" + PREFIX + "[A-Z0-9]{" + CODE_LENGTH + "}$");
    
    private final String value;
    
    /**
     * Creates a new UserCode value object.
     * 
     * @param value the user code string
     * @throws IllegalArgumentException if the user code is invalid
     */
    public UserCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("User code cannot be null");
        }
        
        String trimmedValue = value.trim().toUpperCase();
        
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException("User code cannot be empty");
        }
        
        if (trimmedValue.length() != TOTAL_LENGTH) {
            throw new IllegalArgumentException(
                String.format("User code must be exactly %d characters long", TOTAL_LENGTH)
            );
        }
        
        if (!CODE_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException(
                String.format("User code must follow format %sXXXXXXXX (where X is alphanumeric): %s", 
                    PREFIX, trimmedValue)
            );
        }
        
        this.value = trimmedValue;
    }
    
    /**
     * Gets the user code value.
     * 
     * @return the user code as a string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Gets the code part without the prefix.
     * 
     * @return the code part (8 characters after USR-)
     */
    public String getCodePart() {
        return value.substring(PREFIX.length());
    }
    
    /**
     * Gets the prefix part.
     * 
     * @return the prefix (USR-)
     */
    public String getPrefix() {
        return PREFIX;
    }
    
    /**
     * Generates a new UserCode with the given code part.
     * 
     * @param codePart the 8-character alphanumeric code part
     * @return new UserCode instance
     * @throws IllegalArgumentException if code part is invalid
     */
    public static UserCode generate(String codePart) {
        if (codePart == null) {
            throw new IllegalArgumentException("Code part cannot be null");
        }
        
        String trimmedCodePart = codePart.trim().toUpperCase();
        
        if (trimmedCodePart.length() != CODE_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Code part must be exactly %d characters long", CODE_LENGTH)
            );
        }
        
        if (!trimmedCodePart.matches("[A-Z0-9]{" + CODE_LENGTH + "}")) {
            throw new IllegalArgumentException(
                "Code part must contain only alphanumeric characters: " + trimmedCodePart
            );
        }
        
        return new UserCode(PREFIX + trimmedCodePart);
    }
    
    /**
     * Generates a random UserCode.
     * This method creates a random 8-character alphanumeric code.
     * 
     * @return new randomly generated UserCode
     */
    public static UserCode generateRandom() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        
        return new UserCode(PREFIX + codeBuilder.toString());
    }
    
    /**
     * Creates a UserCode from a string, returning null if invalid.
     * This is useful when you want to avoid exceptions for invalid codes.
     * 
     * @param value the user code string
     * @return UserCode object if valid, null otherwise
     */
    public static UserCode tryCreate(String value) {
        try {
            return new UserCode(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Validates if a string is a valid user code format.
     * 
     * @param value the string to validate
     * @return true if valid user code format
     */
    public static boolean isValid(String value) {
        return tryCreate(value) != null;
    }
    
    /**
     * Checks if this UserCode belongs to a specific role type.
     * Role types are determined by the first character after the prefix.
     * 
     * @param roleChar the role character (A=Admin, C=Cashier, U=User, etc.)
     * @return true if the user belongs to the specified role type
     */
    public boolean belongsToRole(char roleChar) {
        if (getCodePart().isEmpty()) {
            return false;
        }
        return getCodePart().charAt(0) == Character.toUpperCase(roleChar);
    }
    
    /**
     * Gets the role character of this UserCode.
     * 
     * @return the first character of the code part
     */
    public char getRoleIndicator() {
        return getCodePart().charAt(0);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCode userCode = (UserCode) o;
        return Objects.equals(value, userCode.value);
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