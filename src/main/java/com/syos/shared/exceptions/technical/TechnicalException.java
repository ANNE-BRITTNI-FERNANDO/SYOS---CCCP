package com.syos.shared.exceptions.technical;

import com.syos.shared.exceptions.base.SYOSException;

/**
 * Base class for all technical exceptions in the SYOS system.
 * 
 * Technical exceptions represent infrastructure failures, system errors,
 * or unexpected technical issues that prevent normal operation.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public abstract class TechnicalException extends SYOSException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new technical exception with the specified detail message.
     * 
     * @param message the detail message
     */
    protected TechnicalException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new technical exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    protected TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new technical exception with technical and user messages.
     * 
     * @param message the technical detail message
     * @param userMessage the user-friendly message
     */
    protected TechnicalException(String message, String userMessage) {
        super(message, userMessage);
    }
    
    /**
     * Constructs a new technical exception with messages and cause.
     * 
     * @param message the technical detail message
     * @param userMessage the user-friendly message
     * @param cause the cause of this exception
     */
    protected TechnicalException(String message, String userMessage, Throwable cause) {
        super(message, userMessage, cause);
    }
    
    /**
     * Generates error code with TECHNICAL prefix.
     * 
     * @return the technical error code
     */
    @Override
    protected String generateErrorCode() {
        return "TECHNICAL_" + super.generateErrorCode();
    }
}