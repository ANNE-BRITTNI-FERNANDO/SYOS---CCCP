package com.syos.shared.exceptions.base;

/**
 * Base exception class for all SYOS application exceptions.
 * 
 * This class follows the Exception hierarchy design pattern and provides
 * a common base for all business and technical exceptions in the SYOS system.
 * It implements clean exception handling practices.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public abstract class SYOSException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final String userMessage;
    
    /**
     * Constructs a new SYOS exception with the specified detail message.
     * 
     * @param message the detail message
     */
    protected SYOSException(String message) {
        super(message);
        this.errorCode = generateErrorCode();
        this.userMessage = message;
    }
    
    /**
     * Constructs a new SYOS exception with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    protected SYOSException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = generateErrorCode();
        this.userMessage = message;
    }
    
    /**
     * Constructs a new SYOS exception with error code and user-friendly message.
     * 
     * @param message the technical detail message
     * @param userMessage the user-friendly message
     */
    protected SYOSException(String message, String userMessage) {
        super(message);
        this.errorCode = generateErrorCode();
        this.userMessage = userMessage;
    }
    
    /**
     * Constructs a new SYOS exception with error code, user message and cause.
     * 
     * @param message the technical detail message
     * @param userMessage the user-friendly message
     * @param cause the cause of this exception
     */
    protected SYOSException(String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = generateErrorCode();
        this.userMessage = userMessage;
    }
    
    /**
     * Gets the error code for this exception.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the user-friendly message for this exception.
     * 
     * @return the user message
     */
    public String getUserMessage() {
        return userMessage;
    }
    
    /**
     * Generates a unique error code for this exception.
     * Subclasses can override this method to provide specific error codes.
     * 
     * @return the generated error code
     */
    protected String generateErrorCode() {
        return this.getClass().getSimpleName().toUpperCase().replace("EXCEPTION", "");
    }
    
    /**
     * Returns a string representation of this exception including error code.
     * 
     * @return string representation of the exception
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", 
            getErrorCode(), 
            getClass().getSimpleName(), 
            getMessage());
    }
}