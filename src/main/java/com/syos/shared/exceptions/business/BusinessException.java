package com.syos.shared.exceptions.business;

import com.syos.shared.exceptions.base.SYOSException;

/**
 * Base class for all business logic exceptions in the SYOS system.
 * 
 * Business exceptions represent violations of business rules or invalid
 * business operations. These exceptions are expected and should be handled
 * gracefully by the application.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public abstract class BusinessException extends SYOSException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new business exception with the specified detail message.
     * 
     * @param message the detail message
     */
    protected BusinessException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new business exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new business exception with technical and user messages.
     * 
     * @param message the technical detail message
     * @param userMessage the user-friendly message
     */
    protected BusinessException(String message, String userMessage) {
        super(message, userMessage);
    }
    
    /**
     * Constructs a new business exception with messages and cause.
     * 
     * @param message the technical detail message
     * @param userMessage the user-friendly message
     * @param cause the cause of this exception
     */
    protected BusinessException(String message, String userMessage, Throwable cause) {
        super(message, userMessage, cause);
    }
    
    /**
     * Generates error code with BUSINESS prefix.
     * 
     * @return the business error code
     */
    @Override
    protected String generateErrorCode() {
        return "BUSINESS_" + super.generateErrorCode();
    }
}