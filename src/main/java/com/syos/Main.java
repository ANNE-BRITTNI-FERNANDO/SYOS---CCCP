package com.syos;

import com.syos.inventory.application.config.ApplicationConfig;
import com.syos.inventory.application.service.UserService;
import com.syos.inventory.application.seeder.AdminSeeder;
import com.syos.inventory.infrastructure.database.DatabaseInitializer;
import com.syos.inventory.infrastructure.database.DatabaseManager;
import com.syos.inventory.infrastructure.repository.SqliteUserRepository;
import com.syos.inventory.ui.console.LoginUI;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main entry point for the SYOS Inventory System.
 * 
 * This class follows the Clean Architecture principles and serves as the 
 * composition root for the application. It initializes all dependencies
 * and starts the console application interface.
 * 
 * @author SYOS Development Team
 * @version 1.0.0
 * @since 2025-09-23
 */
public class Main {
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    /**
     * Main method - Entry point of the SYOS application.
     * 
     * Initializes the application configuration, sets up database connections,
     * seeds the admin account, and starts the console-based user interface.
     * 
     * @param args Command line arguments (currently not used)
     */
    public static void main(String[] args) {
        ApplicationConfig config = null;
        try {
            LOGGER.info("Starting SYOS Inventory System...");
            
            // Initialize application configuration
            config = ApplicationConfig.getInstance();
            LOGGER.info("Application configuration loaded successfully");
            
            // Initialize database
            if (!DatabaseInitializer.initializeDatabase()) {
                LOGGER.severe("Failed to initialize database");
                System.err.println("Failed to initialize database. Please check your configuration.");
                System.exit(1);
            }
            LOGGER.info("Database initialized successfully");
            
            // Check if we should load sample data (for development/testing)
            String loadSampleData = config.getProperty("database.load_sample_data", "false");
            if ("true".equalsIgnoreCase(loadSampleData)) {
                LOGGER.info("Loading sample data for development/testing...");
                if (DatabaseInitializer.loadSampleData()) {
                    LOGGER.info("Sample data loaded successfully");
                } else {
                    LOGGER.warning("Failed to load sample data");
                }
            }
            
            // Initialize application dependencies
            initializeApplication();
            
            System.out.println("SYOS Inventory System initialized successfully!");
            System.out.println("Database: " + config.getProperty("database.path"));
            System.out.println("Ready for operation.");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error occurred during application startup", e);
            System.err.println("Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            
        } finally {
            // Cleanup resources
            try {
                if (config != null) {
                    config.shutdown();
                }
                LOGGER.info("Application shutdown completed");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during application shutdown", e);
            }
        }
    }
    
    /**
     * Initializes the application dependencies and starts the console interface
     */
    private static void initializeApplication() {
        try {
            LOGGER.info("Initializing application components...");
            
            // Get database manager instance
            DatabaseManager databaseManager = DatabaseManager.getInstance();
            LOGGER.info("Database manager initialized");
            
            // Create repository layer
            SqliteUserRepository userRepository = new SqliteUserRepository(databaseManager);
            LOGGER.info("User repository initialized");
            
            // Create service layer
            UserService userService = new UserService(userRepository);
            LOGGER.info("User service initialized");
            
            // Seed admin account
            AdminSeeder adminSeeder = new AdminSeeder(userService);
            adminSeeder.seedAdminAccount();
            LOGGER.info("Admin account seeding completed");
            
            // Create and start login UI
            LoginUI loginUI = new LoginUI(userService);
            LOGGER.info("Login UI initialized");
            
            // Start the application
            loginUI.start();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application components", e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }
}