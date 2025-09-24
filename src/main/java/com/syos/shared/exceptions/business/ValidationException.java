package com.syos.shared.exceptions.business;

/**
 * Exception thrown when input validation fails.
 * 
 * This exception represents validation failures for user input,
 * request parameters, or data that doesn't meet required criteria.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class ValidationException extends BusinessException {
    
    private static final long serialVersionUID = 1L;
    
    private final String fieldName;
    private final Object invalidValue;
    private final String validationRule;
    
    /**
     * Constructs a new validation exception.
     * 
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value
     * @param validationRule the validation rule that was violated
     */
    public ValidationException(String fieldName, Object invalidValue, String validationRule) {
        super(
            String.format("Validation failed for field '%s' with value '%s': %s", 
                fieldName, invalidValue, validationRule),
            String.format("Invalid %s: %s", fieldName, validationRule)
        );
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.validationRule = validationRule;
    }
    
    /**
     * Constructs a new validation exception with custom user message.
     * 
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value
     * @param validationRule the validation rule that was violated
     * @param userMessage custom user-friendly message
     */
    public ValidationException(String fieldName, Object invalidValue, 
                             String validationRule, String userMessage) {
        super(
            String.format("Validation failed for field '%s' with value '%s': %s", 
                fieldName, invalidValue, validationRule),
            userMessage
        );
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.validationRule = validationRule;
    }
    
    /**
     * Constructs a validation exception for general validation failure.
     * 
     * @param message the validation error message
     */
    public ValidationException(String message) {
        super(message, "Invalid input provided");
        this.fieldName = null;
        this.invalidValue = null;
        this.validationRule = message;
    }
    
    /**
     * Gets the name of the field that failed validation.
     * 
     * @return the field name, or null if not field-specific
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Gets the invalid value that caused the validation failure.
     * 
     * @return the invalid value, or null if not applicable
     */
    public Object getInvalidValue() {
        return invalidValue;
    }
    
    /**
     * Gets the validation rule that was violated.
     * 
     * @return the validation rule
     */
    public String getValidationRule() {
        return validationRule;
    }
}