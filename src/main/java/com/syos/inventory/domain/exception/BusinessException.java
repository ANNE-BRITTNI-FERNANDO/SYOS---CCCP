package com.syos.inventory.domain.exception;

/**
 * Exception thrown when business rules are violated
 */
public class BusinessException extends DomainException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}