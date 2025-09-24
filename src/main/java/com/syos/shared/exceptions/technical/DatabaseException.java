package com.syos.shared.exceptions.technical;

/**
 * Exception thrown when database operations fail.
 * 
 * This exception represents technical failures in database connectivity,
 * SQL execution, or data persistence operations.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class DatabaseException extends TechnicalException {
    
    private static final long serialVersionUID = 1L;
    
    private final String operation;
    private final String sqlState;
    private final int errorCode;
    
    /**
     * Constructs a new database exception.
     * 
     * @param operation the database operation that failed
     * @param message the detail message
     */
    public DatabaseException(String operation, String message) {
        super(
            String.format("Database operation '%s' failed: %s", operation, message),
            "A database error occurred. Please try again later."
        );
        this.operation = operation;
        this.sqlState = null;
        this.errorCode = 0;
    }
    
    /**
     * Constructs a new database exception with cause.
     * 
     * @param operation the database operation that failed
     * @param message the detail message
     * @param cause the underlying cause
     */
    public DatabaseException(String operation, String message, Throwable cause) {
        super(
            String.format("Database operation '%s' failed: %s", operation, message),
            "A database error occurred. Please try again later.",
            cause
        );
        this.operation = operation;
        this.sqlState = null;
        this.errorCode = 0;
    }
    
    /**
     * Constructs a new database exception with SQL details.
     * 
     * @param operation the database operation that failed
     * @param message the detail message
     * @param sqlState the SQL state code
     * @param errorCode the database error code
     * @param cause the underlying cause
     */
    public DatabaseException(String operation, String message, String sqlState, 
                           int errorCode, Throwable cause) {
        super(
            String.format("Database operation '%s' failed [%s-%d]: %s", 
                operation, sqlState, errorCode, message),
            "A database error occurred. Please try again later.",
            cause
        );
        this.operation = operation;
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the database operation that failed.
     * 
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * Gets the SQL state code.
     * 
     * @return the SQL state, or null if not available
     */
    public String getSqlState() {
        return sqlState;
    }
    
    /**
     * Gets the database error code.
     * 
     * @return the error code as string, or "0" if not available
     */
    @Override
    public String getErrorCode() {
        return String.valueOf(errorCode);
    }
}