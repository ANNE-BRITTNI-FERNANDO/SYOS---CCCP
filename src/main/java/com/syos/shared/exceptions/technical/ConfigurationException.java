package com.syos.shared.exceptions.technical;

/**
 * Exception thrown when configuration-related errors occur.
 * 
 * This exception represents failures in loading, parsing, or validating
 * application configuration settings.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class ConfigurationException extends TechnicalException {
    
    private static final long serialVersionUID = 1L;
    
    private final String configurationKey;
    private final String configurationSource;
    
    /**
     * Constructs a new configuration exception.
     * 
     * @param configurationKey the configuration key that caused the error
     * @param message the detail message
     */
    public ConfigurationException(String configurationKey, String message) {
        super(
            String.format("Configuration error for key '%s': %s", configurationKey, message),
            "Application configuration error. Please contact system administrator."
        );
        this.configurationKey = configurationKey;
        this.configurationSource = null;
    }
    
    /**
     * Constructs a new configuration exception with source.
     * 
     * @param configurationKey the configuration key that caused the error
     * @param configurationSource the configuration source (file, database, etc.)
     * @param message the detail message
     */
    public ConfigurationException(String configurationKey, String configurationSource, String message) {
        super(
            String.format("Configuration error for key '%s' in source '%s': %s", 
                configurationKey, configurationSource, message),
            "Application configuration error. Please contact system administrator."
        );
        this.configurationKey = configurationKey;
        this.configurationSource = configurationSource;
    }
    
    /**
     * Constructs a new configuration exception with cause.
     * 
     * @param configurationKey the configuration key that caused the error
     * @param configurationSource the configuration source
     * @param message the detail message
     * @param cause the underlying cause
     */
    public ConfigurationException(String configurationKey, String configurationSource, 
                                String message, Throwable cause) {
        super(
            String.format("Configuration error for key '%s' in source '%s': %s", 
                configurationKey, configurationSource, message),
            "Application configuration error. Please contact system administrator.",
            cause
        );
        this.configurationKey = configurationKey;
        this.configurationSource = configurationSource;
    }
    
    /**
     * Gets the configuration key that caused the error.
     * 
     * @return the configuration key
     */
    public String getConfigurationKey() {
        return configurationKey;
    }
    
    /**
     * Gets the configuration source.
     * 
     * @return the configuration source, or null if not specified
     */
    public String getConfigurationSource() {
        return configurationSource;
    }
}