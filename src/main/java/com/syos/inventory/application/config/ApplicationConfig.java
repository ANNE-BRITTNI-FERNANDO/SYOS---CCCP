package com.syos.inventory.application.config;

import com.syos.inventory.application.service.UserService;
import com.syos.inventory.application.service.ProductService;
import com.syos.inventory.domain.repository.UserRepository;
import com.syos.inventory.domain.repository.ProductRepository;
import com.syos.inventory.infrastructure.database.DatabaseManager;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Application configuration and dependency injection
 */
public class ApplicationConfig {
    private static final Logger logger = Logger.getLogger(ApplicationConfig.class.getName());
    
    private static ApplicationConfig instance;
    private Properties properties;
    private DatabaseManager databaseManager;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private UserService userService;
    private ProductService productService;
    
    private ApplicationConfig() {
        initialize();
    }
    
    public static ApplicationConfig getInstance() {
        if (instance == null) {
            synchronized (ApplicationConfig.class) {
                if (instance == null) {
                    instance = new ApplicationConfig();
                }
            }
        }
        return instance;
    }
    
    private void initialize() {
        try {
            loadProperties();
            initializeDatabase();
            initializeRepositories();
            initializeServices();
            
            logger.info("Application configuration initialized successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to initialize application configuration: " + e.getMessage());
            throw new RuntimeException("Application initialization failed", e);
        }
    }
    
    private void loadProperties() throws IOException {
        properties = new Properties();
        
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/application.properties")) {
            
            if (input == null) {
                logger.warning("application.properties not found, using defaults");
                setDefaultProperties();
                return;
            }
            
            properties.load(input);
            logger.info("Application properties loaded successfully");
            
        } catch (IOException e) {
            logger.warning("Error loading application.properties, using defaults: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        properties.setProperty("database.type", "sqlite");
        properties.setProperty("database.path", "data/syos_inventory.db");
        properties.setProperty("database.schema.file", "database/schema.sql");
        properties.setProperty("database.sample.data.file", "database/sample_data.sql");
        properties.setProperty("database.load_sample_data", "false");
        properties.setProperty("database.connection.timeout", "30000");
        properties.setProperty("database.pragma.foreign_keys", "ON");
        properties.setProperty("database.pragma.journal_mode", "WAL");
        properties.setProperty("database.pragma.synchronous", "NORMAL");
    }
    
    private void initializeDatabase() {
        databaseManager = DatabaseManager.getInstance();
    }
    
    private void initializeRepositories() {
        // TODO: Initialize actual repository implementations
        // For now, these will be null until we create the infrastructure layer
        userRepository = null;
        productRepository = null;
    }
    
    private void initializeServices() {
        // Initialize services with repositories
        // For now, we'll initialize them as null since repositories are not ready
        if (userRepository != null) {
            userService = new UserService(userRepository);
        }
        if (productRepository != null) {
            productService = new ProductService(productRepository);
        }
    }
    
    // Getters for dependencies
    public Properties getProperties() {
        return properties;
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public UserRepository getUserRepository() {
        return userRepository;
    }
    
    public ProductRepository getProductRepository() {
        return productRepository;
    }
    
    public UserService getUserService() {
        return userService;
    }
    
    public ProductService getProductService() {
        return productService;
    }
    
    // Setters for testing or manual dependency injection
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userService = new UserService(userRepository);
    }
    
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.productService = new ProductService(productRepository);
    }
    
    /**
     * Clean shutdown of application resources
     */
    public void shutdown() {
        try {
            if (databaseManager != null) {
                databaseManager.close();
            }
            logger.info("Application shutdown completed");
        } catch (Exception e) {
            logger.severe("Error during application shutdown: " + e.getMessage());
        }
    }
}