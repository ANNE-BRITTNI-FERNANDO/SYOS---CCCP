package com.syos.shared.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * ProductCode value object that ensures product code validity and immutability.
 * 
 * This class follows the Value Object pattern from Domain-Driven Design,
 * ensuring that product codes are always valid and follow the SYOS format.
 * 
 * Format: PRD-XXXXXXXX (where X is alphanumeric)
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public final class ProductCode {
    
    private static final String PREFIX = "PRD-";
    private static final int CODE_LENGTH = 8; // Length after prefix
    private static final int TOTAL_LENGTH = PREFIX.length() + CODE_LENGTH;
    private static final Pattern CODE_PATTERN = Pattern.compile("^" + PREFIX + "[A-Z0-9]{" + CODE_LENGTH + "}$");
    
    private final String value;
    
    /**
     * Creates a new ProductCode value object.
     * 
     * @param value the product code string
     * @throws IllegalArgumentException if the product code is invalid
     */
    public ProductCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Product code cannot be null");
        }
        
        String trimmedValue = value.trim().toUpperCase();
        
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be empty");
        }
        
        if (trimmedValue.length() != TOTAL_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Product code must be exactly %d characters long", TOTAL_LENGTH)
            );
        }
        
        if (!CODE_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException(
                String.format("Product code must follow format %sXXXXXXXX (where X is alphanumeric): %s", 
                    PREFIX, trimmedValue)
            );
        }
        
        this.value = trimmedValue;
    }
    
    /**
     * Gets the product code value.
     * 
     * @return the product code as a string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Gets the code part without the prefix.
     * 
     * @return the code part (8 characters after PRD-)
     */
    public String getCodePart() {
        return value.substring(PREFIX.length());
    }
    
    /**
     * Gets the prefix part.
     * 
     * @return the prefix (PRD-)
     */
    public String getPrefix() {
        return PREFIX;
    }
    
    /**
     * Generates a new ProductCode with the given code part.
     * 
     * @param codePart the 8-character alphanumeric code part
     * @return new ProductCode instance
     * @throws IllegalArgumentException if code part is invalid
     */
    public static ProductCode generate(String codePart) {
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
        
        return new ProductCode(PREFIX + trimmedCodePart);
    }
    
    /**
     * Generates a random ProductCode.
     * This method creates a random 8-character alphanumeric code.
     * 
     * @return new randomly generated ProductCode
     */
    public static ProductCode generateRandom() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        
        return new ProductCode(PREFIX + codeBuilder.toString());
    }
    
    /**
     * Creates a ProductCode from a string, returning null if invalid.
     * This is useful when you want to avoid exceptions for invalid codes.
     * 
     * @param value the product code string
     * @return ProductCode object if valid, null otherwise
     */
    public static ProductCode tryCreate(String value) {
        try {
            return new ProductCode(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Validates if a string is a valid product code format.
     * 
     * @param value the string to validate
     * @return true if valid product code format
     */
    public static boolean isValid(String value) {
        return tryCreate(value) != null;
    }
    
    /**
     * Checks if this ProductCode belongs to a specific category.
     * Categories are determined by the first character after the prefix.
     * 
     * @param categoryChar the category character (A-Z)
     * @return true if the product belongs to the specified category
     */
    public boolean belongsToCategory(char categoryChar) {
        if (getCodePart().isEmpty()) {
            return false;
        }
        return getCodePart().charAt(0) == Character.toUpperCase(categoryChar);
    }
    
    /**
     * Gets the category character of this ProductCode.
     * 
     * @return the first character of the code part
     */
    public char getCategory() {
        return getCodePart().charAt(0);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCode that = (ProductCode) o;
        return Objects.equals(value, that.value);
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