package com.syos.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database connection manager for SQLite.
 * 
 * This class follows the Singleton pattern and provides centralized
 * database connection management for the SYOS system.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 */
public class DatabaseManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    
    private final String databasePath;
    private Connection connection;
    
    /**
     * Private constructor for Singleton pattern.
     * 
     * @param databasePath the path to the SQLite database file
     */
    private DatabaseManager(String databasePath) {
        this.databasePath = databasePath;
    }
    
    /**
     * Gets the singleton instance of DatabaseManager.
     * 
     * @param databasePath the path to the SQLite database file
     * @return the DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance(String databasePath) {
        if (instance == null) {
            instance = new DatabaseManager(databasePath);
        }
        return instance;
    }
    
    /**
     * Gets the singleton instance with default database path.
     * 
     * @return the DatabaseManager instance
     */
    public static DatabaseManager getInstance() {
        return getInstance("data/syos_inventory.db");
    }
    
    /**
     * Establishes a connection to the SQLite database.
     * 
     * @return the database connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                
                // Create connection URL
                String url = "jdbc:sqlite:" + databasePath;
                
                // Establish connection
                connection = DriverManager.getConnection(url);
                
                // Enable foreign key constraints
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    stmt.execute("PRAGMA journal_mode = WAL;"); // Write-Ahead Logging for better performance
                    stmt.execute("PRAGMA synchronous = NORMAL;"); // Balanced safety and performance
                }
                
                LOGGER.info("Database connection established: " + databasePath);
                
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found", e);
                throw new SQLException("SQLite JDBC driver not found", e);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to database: " + databasePath, e);
                throw e;
            }
        }
        
        return connection;
    }
    
    /**
     * Initializes the database schema if it doesn't exist.
     * This method reads the schema.sql file and executes it.
     * 
     * @throws SQLException if schema initialization fails
     */
    public void initializeSchema() throws SQLException {
        LOGGER.info("Initializing database schema...");
        
        try (Connection conn = getConnection();
             InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("database/schema.sql")) {
            
            if (schemaStream == null) {
                throw new SQLException("Schema file not found: database/schema.sql");
            }
            
            // Read the schema file
            String schemaSQL;
            try (Scanner scanner = new Scanner(schemaStream, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                schemaSQL = scanner.hasNext() ? scanner.next() : "";
            }
            
            // Execute the schema SQL
            try (Statement stmt = conn.createStatement()) {
                // Split by semicolon and execute each statement
                String[] statements = schemaSQL.split(";");
                
                for (String sql : statements) {
                    String trimmedSQL = sql.trim();
                    if (!trimmedSQL.isEmpty() && !trimmedSQL.startsWith("--")) {
                        stmt.execute(trimmedSQL);
                    }
                }
                
                LOGGER.info("Database schema initialized successfully");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
            throw new SQLException("Failed to initialize database schema", e);
        }
    }
    
    /**
     * Checks if the database schema is already initialized.
     * 
     * @return true if schema exists, false otherwise
     */
    public boolean isSchemaInitialized() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Check if main tables exist
            String checkSQL = "SELECT COUNT(*) FROM sqlite_master " +
                "WHERE type='table' AND name IN (" +
                "'users', 'products', 'bills', 'bill_items'" +
                ")";
            
            var resultSet = stmt.executeQuery(checkSQL);
            if (resultSet.next()) {
                int tableCount = resultSet.getInt(1);
                return tableCount >= 4; // Main tables exist
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking schema initialization", e);
        }
        
        return false;
    }
    
    /**
     * Creates the database directory if it doesn't exist.
     */
    public void ensureDatabaseDirectory() {
        java.io.File dbFile = new java.io.File(databasePath);
        java.io.File parentDir = dbFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                LOGGER.info("Created database directory: " + parentDir.getAbsolutePath());
            } else {
                LOGGER.warning("Failed to create database directory: " + parentDir.getAbsolutePath());
            }
        }
    }
    
    /**
     * Performs database setup including directory creation and schema initialization.
     * This method should be called during application startup.
     * 
     * @throws SQLException if setup fails
     */
    public void setupDatabase() throws SQLException {
        LOGGER.info("Setting up database...");
        
        // Ensure database directory exists
        ensureDatabaseDirectory();
        
        // Initialize schema if needed
        if (!isSchemaInitialized()) {
            initializeSchema();
        } else {
            LOGGER.info("Database schema already initialized");
        }
        
        LOGGER.info("Database setup completed");
    }
    
    /**
     * Tests the database connection.
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("SELECT 1");
            LOGGER.info("Database connection test successful");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
    
    /**
     * Executes a SQL script from a resource file.
     * 
     * @param resourcePath the path to the SQL script resource
     * @throws SQLException if script execution fails
     */
    public void executeScript(String resourcePath) throws SQLException {
        LOGGER.info("Executing SQL script: " + resourcePath);
        
        try (Connection conn = getConnection();
             InputStream scriptStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            
            if (scriptStream == null) {
                throw new SQLException("Script file not found: " + resourcePath);
            }
            
            String scriptSQL;
            try (Scanner scanner = new Scanner(scriptStream, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                scriptSQL = scanner.hasNext() ? scanner.next() : "";
            }
            
            try (Statement stmt = conn.createStatement()) {
                String[] statements = scriptSQL.split(";");
                
                for (String sql : statements) {
                    String trimmedSQL = sql.trim();
                    if (!trimmedSQL.isEmpty() && !trimmedSQL.startsWith("--")) {
                        stmt.execute(trimmedSQL);
                    }
                }
                
                LOGGER.info("SQL script executed successfully: " + resourcePath);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to execute SQL script: " + resourcePath, e);
            throw new SQLException("Failed to execute SQL script: " + resourcePath, e);
        }
    }
    
    /**
     * Gets the database file path.
     * 
     * @return the database file path
     */
    public String getDatabasePath() {
        return databasePath;
    }
}