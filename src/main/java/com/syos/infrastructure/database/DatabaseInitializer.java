package com.syos.infrastructure.database;

import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database initialization utility for the SYOS system.
 * 
 * This utility provides methods to initialize and manage the SQLite database
 * for the SYOS inventory system.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class DatabaseInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DatabaseInitializer() {
        // Utility class
    }
    
    /**
     * Initializes the database with the default configuration.
     * This method should be called during application startup.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeDatabase() {
        return initializeDatabase("data/syos_inventory.db");
    }
    
    /**
     * Initializes the database with the specified path.
     * 
     * @param databasePath the path to the SQLite database file
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeDatabase(String databasePath) {
        LOGGER.info("Starting database initialization...");
        
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance(databasePath);
            
            // Setup database (creates directory and initializes schema)
            dbManager.setupDatabase();
            
            // Test the connection
            if (!dbManager.testConnection()) {
                LOGGER.severe("Database connection test failed");
                return false;
            }
            
            LOGGER.info("Database initialization completed successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during database initialization", e);
            return false;
        }
    }
    
    /**
     * Checks if the database is properly initialized and accessible.
     * 
     * @return true if database is ready, false otherwise
     */
    public static boolean isDatabaseReady() {
        return isDatabaseReady("data/syos_inventory.db");
    }
    
    /**
     * Checks if the database at the specified path is properly initialized and accessible.
     * 
     * @param databasePath the path to the SQLite database file
     * @return true if database is ready, false otherwise
     */
    public static boolean isDatabaseReady(String databasePath) {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance(databasePath);
            return dbManager.isSchemaInitialized() && dbManager.testConnection();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking database readiness", e);
            return false;
        }
    }
    
    /**
     * Resets the database by dropping all tables and recreating the schema.
     * WARNING: This will delete all data!
     * 
     * @return true if reset was successful, false otherwise
     */
    public static boolean resetDatabase() {
        return resetDatabase("data/syos_inventory.db");
    }
    
    /**
     * Resets the database at the specified path by dropping all tables and recreating the schema.
     * WARNING: This will delete all data!
     * 
     * @param databasePath the path to the SQLite database file
     * @return true if reset was successful, false otherwise
     */
    public static boolean resetDatabase(String databasePath) {
        LOGGER.warning("Resetting database - ALL DATA WILL BE LOST!");
        
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance(databasePath);
            
            // Execute drop script if available
            try {
                dbManager.executeScript("database/drop_schema.sql");
            } catch (SQLException e) {
                LOGGER.info("Drop script not found or failed, continuing with reset...");
            }
            
            // Reinitialize schema
            dbManager.initializeSchema();
            
            LOGGER.info("Database reset completed successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database reset failed", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during database reset", e);
            return false;
        }
    }
    
    /**
     * Loads sample data into the database for testing purposes.
     * 
     * @return true if sample data was loaded successfully, false otherwise
     */
    public static boolean loadSampleData() {
        return loadSampleData("data/syos_inventory.db");
    }
    
    /**
     * Loads sample data into the database at the specified path for testing purposes.
     * 
     * @param databasePath the path to the SQLite database file
     * @return true if sample data was loaded successfully, false otherwise
     */
    public static boolean loadSampleData(String databasePath) {
        LOGGER.info("Loading sample data...");
        
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance(databasePath);
            
            // Execute sample data script if available
            dbManager.executeScript("database/sample_data.sql");
            
            LOGGER.info("Sample data loaded successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load sample data", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error loading sample data", e);
            return false;
        }
    }
}