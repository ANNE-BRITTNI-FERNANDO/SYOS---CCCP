package com.syos.inventory.domain.exception;

/**
 * Exception thrown when a requested entity is not found
 */
public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}