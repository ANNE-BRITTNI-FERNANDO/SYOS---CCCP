package com.syos.inventory.infrastructure.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database initialization utilities
 */
public class DatabaseInitializer {
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    
    /**
     * Initialize the database schema
     * @return true if successful
     */
    public static boolean initializeDatabase() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Connection connection = dbManager.getConnection();
            
            // Load and execute schema
            String schemaFile = dbManager.getConfig().getProperty("database.schema.file", "database/schema.sql");
            String schemaSql = loadResourceFile(schemaFile);
            
            if (schemaSql != null && !schemaSql.trim().isEmpty()) {
                executeStatements(connection, schemaSql);
                logger.info("Database schema initialized successfully");
                return true;
            } else {
                logger.warning("Schema file is empty or not found: " + schemaFile);
                return false;
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }
    
    /**
     * Load sample data into the database
     * @return true if successful
     */
    public static boolean loadSampleData() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Connection connection = dbManager.getConnection();
            
            // Load and execute sample data
            String sampleDataFile = dbManager.getConfig().getProperty("database.sample.data.file", "database/sample_data.sql");
            String sampleDataSql = loadResourceFile(sampleDataFile);
            
            if (sampleDataSql != null && !sampleDataSql.trim().isEmpty()) {
                executeStatements(connection, sampleDataSql);
                logger.info("Sample data loaded successfully");
                return true;
            } else {
                logger.warning("Sample data file is empty or not found: " + sampleDataFile);
                return false;
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load sample data", e);
            return false;
        }
    }
    
    /**
     * Reset the database (drop all tables and recreate)
     * @return true if successful
     */
    public static boolean resetDatabase() {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            Connection connection = dbManager.getConnection();
            
            // Drop all tables
            dropAllTables(connection);
            
            // Reinitialize
            return initializeDatabase();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reset database", e);
            return false;
        }
    }
    
    private static String loadResourceFile(String resourcePath) throws IOException {
        try (InputStream inputStream = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {
            
            if (inputStream == null) {
                logger.warning("Resource file not found: " + resourcePath);
                return null;
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            return content.toString();
        }
    }
    
    private static void executeStatements(Connection connection, String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Clean SQL by removing comment lines first
            String[] lines = sql.split("\n");
            StringBuilder cleanSql = new StringBuilder();
            
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    cleanSql.append(line).append("\n");
                }
            }
            
            // Split cleaned SQL into individual statements using semicolon
            String[] statements = cleanSql.toString().split(";");
            
            logger.info("Total statements to execute: " + statements.length);
            
            for (int i = 0; i < statements.length; i++) {
                String trimmedStatement = statements[i].trim();
                logger.info("Statement " + (i+1) + " (length=" + trimmedStatement.length() + "): " + 
                           trimmedStatement.substring(0, Math.min(100, trimmedStatement.length())) + "...");
                
                // Skip empty statements
                if (!trimmedStatement.isEmpty()) {
                    try {
                        logger.info("Executing statement " + (i+1));
                        stmt.execute(trimmedStatement);
                        logger.info("Statement " + (i+1) + " executed successfully");
                    } catch (SQLException e) {
                        logger.severe("Failed to execute SQL statement: " + trimmedStatement);
                        throw e;
                    }
                } else {
                    logger.info("Skipping statement " + (i+1) + " (empty)");
                }
            }
        }
    }
    
    private static void dropAllTables(Connection connection) throws SQLException {
        String[] tables = {"audit_logs", "bill_items", "bills", "inventory_movements", "products", "users"};
        
        try (Statement stmt = connection.createStatement()) {
            // Disable foreign key constraints temporarily
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            // Drop tables in reverse order to avoid foreign key conflicts
            for (int i = tables.length - 1; i >= 0; i--) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + tables[i]);
                } catch (SQLException e) {
                    logger.warning("Could not drop table " + tables[i] + ": " + e.getMessage());
                }
            }
            
            // Re-enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON");
            
            logger.info("All tables dropped successfully");
        }
    }
}