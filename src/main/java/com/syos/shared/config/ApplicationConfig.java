package com.syos.shared.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Application Configuration Manager.
 * 
 * This class implements the Singleton pattern and manages all application
 * configuration properties. It follows the Single Responsibility Principle
 * by only handling configuration management.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public final class ApplicationConfig {
    
    private static final Logger LOGGER = Logger.getLogger(ApplicationConfig.class.getName());
    private static ApplicationConfig instance;
    private static final Object LOCK = new Object();
    
    private Properties properties;
    private boolean initialized = false;
    
    // Configuration file paths
    private static final String APP_CONFIG_FILE = "/config/application.properties";
    private static final String LOG_CONFIG_FILE = "/config/logging.properties";
    
    /**
     * Private constructor to prevent direct instantiation.
     * Follows Singleton pattern.
     */
    private ApplicationConfig() {
        // Private constructor
    }
    
    /**
     * Gets the singleton instance of ApplicationConfig.
     * Thread-safe implementation using double-checked locking.
     * 
     * @return ApplicationConfig instance
     */
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ApplicationConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initializes the application configuration.
     * Loads all configuration files and validates required properties.
     * 
     * @throws RuntimeException if configuration loading fails
     */
    public static void initialize() throws RuntimeException {
        ApplicationConfig config = getInstance();
        if (!config.initialized) {
            config.loadConfiguration();
            config.initialized = true;
            LOGGER.info("Application configuration initialized successfully");
        }
    }
    
    /**
     * Loads configuration from properties files.
     * 
     * @throws RuntimeException if loading fails
     */
    private void loadConfiguration() throws RuntimeException {
        try {
            properties = new Properties();
            
            // Load application properties
            loadPropertiesFile(APP_CONFIG_FILE);
            
            // Load logging properties
            loadPropertiesFile(LOG_CONFIG_FILE);
            
            // Validate required properties
            validateRequiredProperties();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration files", e);
        }
    }
    
    /**
     * Loads a properties file from the classpath.
     * 
     * @param fileName The properties file name
     * @throws IOException if file loading fails
     */
    private void loadPropertiesFile(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Configuration file not found: " + fileName);
            }
            properties.load(inputStream);
        }
    }
    
    /**
     * Validates that all required properties are present.
     * 
     * @throws RuntimeException if required properties are missing
     */
    private void validateRequiredProperties() throws RuntimeException {
        String[] requiredProperties = {
            "database.path",
            "database.schema.file",
            "business.reorder.threshold",
            "file.storage.bills.path"
        };
        
        for (String property : requiredProperties) {
            if (getProperty(property) == null) {
                throw new RuntimeException("Required property missing: " + property);
            }
        }
    }
    
    /**
     * Gets a property value by key.
     * 
     * @param key The property key
     * @return The property value, or null if not found
     */
    public String getProperty(String key) {
        return properties != null ? properties.getProperty(key) : null;
    }
    
    /**
     * Gets a property value with a default fallback.
     * 
     * @param key The property key
     * @param defaultValue Default value if property is not found
     * @return The property value or default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties != null ? properties.getProperty(key, defaultValue) : defaultValue;
    }
    
    /**
     * Gets an integer property value.
     * 
     * @param key The property key
     * @param defaultValue Default value if property is not found or invalid
     * @return The integer property value
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid integer property value for key: " + key + ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * Gets a boolean property value.
     * 
     * @param key The property key
     * @param defaultValue Default value if property is not found
     * @return The boolean property value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Gets a double property value.
     * 
     * @param key The property key
     * @param defaultValue Default value if property is not found or invalid
     * @return The double property value
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid double property value for key: " + key + ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }
}