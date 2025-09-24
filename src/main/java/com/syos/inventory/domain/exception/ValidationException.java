package com.syos.inventory.domain.exception;

/**
 * Exception thrown when validation fails in the domain layer
 */
public class ValidationException extends DomainException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}