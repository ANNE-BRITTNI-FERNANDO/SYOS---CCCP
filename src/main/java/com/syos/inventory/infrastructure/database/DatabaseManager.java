package com.syos.inventory.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Database manager for SQLite database connections
 */
public class DatabaseManager {
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    
    private static DatabaseManager instance;
    private Connection connection;
    private String databasePath;
    private Properties config;
    
    private DatabaseManager() {
        // Load SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
        
        loadConfiguration();
        this.databasePath = config.getProperty("database.path", "data/syos_inventory.db");
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void loadConfiguration() {
        config = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/application.properties")) {
            
            if (input != null) {
                config.load(input);
            }
        } catch (IOException e) {
            logger.warning("Could not load configuration, using defaults: " + e.getMessage());
        }
        
        // Set defaults if not loaded
        config.putIfAbsent("database.type", "sqlite");
        config.putIfAbsent("database.path", "data/syos_inventory.db");
        config.putIfAbsent("database.schema.file", "database/schema.sql");
        config.putIfAbsent("database.sample.data.file", "database/sample_data.sql");
        config.putIfAbsent("database.connection.timeout", "30000");
        config.putIfAbsent("database.pragma.foreign_keys", "ON");
        config.putIfAbsent("database.pragma.journal_mode", "WAL");
        config.putIfAbsent("database.pragma.synchronous", "NORMAL");
    }
    
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }
        return connection;
    }
    
    private void createConnection() throws SQLException {
        try {
            // Ensure data directory exists
            java.io.File dataDir = new java.io.File(databasePath).getParentFile();
            if (dataDir != null && !dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            String url = "jdbc:sqlite:" + databasePath;
            connection = DriverManager.getConnection(url);
            
            // Configure SQLite pragmas
            configurePragmas();
            
            logger.fine("Database connection established: " + databasePath);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create database connection", e);
            throw e;
        }
    }
    
    private void configurePragmas() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = " + config.getProperty("database.pragma.foreign_keys", "ON"));
            
            // Set journal mode for better performance
            stmt.execute("PRAGMA journal_mode = " + config.getProperty("database.pragma.journal_mode", "WAL"));
            
            // Set synchronous mode
            stmt.execute("PRAGMA synchronous = " + config.getProperty("database.pragma.synchronous", "NORMAL"));
            
            logger.fine("SQLite pragmas configured successfully");
        }
    }
    
    public String getDatabasePath() {
        return databasePath;
    }
    
    public Properties getConfig() {
        return config;
    }
    
    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.fine("Database connection closed");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
    
    public void closeConnection() {
        close();
    }
}