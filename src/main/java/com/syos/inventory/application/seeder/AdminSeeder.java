package com.syos.inventory.application.seeder;

import com.syos.inventory.application.service.UserService;
import com.syos.inventory.domain.value.UserRole;

import java.util.logging.Logger;

/**
 * AdminSeeder is responsible for creating the default admin account
 * when the application starts up if it doesn't already exist.
 * 
 * This ensures there's always at least one admin user available
 * to manage the system.
 */
public class AdminSeeder {
    
    private static final Logger logger = Logger.getLogger(AdminSeeder.class.getName());
    
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@syos.com";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "System";
    private static final String DEFAULT_ADMIN_LAST_NAME = "Administrator";
    
    private final UserService userService;
    
    /**
     * Constructor
     * @param userService User service for user operations
     */
    public AdminSeeder(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Seeds the default admin account if it doesn't exist
     * This method is idempotent and can be safely called multiple times
     */
    public void seedAdminAccount() {
        try {
            logger.info("Checking for default admin account...");
            
            // Try to find existing admin user by email
            try {
                userService.getUserByEmail(DEFAULT_ADMIN_EMAIL);
                logger.info("Default admin account already exists - skipping creation");
                return; // Admin exists, no need to create
            } catch (Exception emailException) {
                // Admin not found by email, check by username as backup
                try {
                    userService.getUserByUsername(DEFAULT_ADMIN_USERNAME);
                    logger.info("Default admin account found by username - skipping creation");
                    return; // Admin exists, no need to create
                } catch (Exception usernameException) {
                    // Admin doesn't exist, proceed to create
                    logger.info("Default admin account not found - creating new admin account");
                    
                    createAdminAccount();
                    logger.info("Default admin account created successfully");
                    logger.info("Login credentials - Email: " + DEFAULT_ADMIN_EMAIL + ", Password: " + DEFAULT_ADMIN_PASSWORD);
                }
            }
            
        } catch (Exception e) {
            logger.severe("Failed to seed admin account: " + e.getMessage());
            throw new RuntimeException("Critical error: Unable to create default admin account", e);
        }
    }
    
    /**
     * Creates the default admin account with predefined credentials
     */
    private void createAdminAccount() {
        userService.createUser(
            DEFAULT_ADMIN_USERNAME,
            DEFAULT_ADMIN_PASSWORD,
            DEFAULT_ADMIN_FIRST_NAME,
            DEFAULT_ADMIN_LAST_NAME,
            DEFAULT_ADMIN_EMAIL,
            UserRole.ADMIN
        );
        
        logger.info("Admin account created with username: " + DEFAULT_ADMIN_USERNAME);
    }
    
    /**
     * Gets the default admin username (for reference)
     * @return Default admin username
     */
    public static String getDefaultAdminUsername() {
        return DEFAULT_ADMIN_USERNAME;
    }
    
    /**
     * Gets the default admin password (for reference)
     * @return Default admin password
     */
    public static String getDefaultAdminPassword() {
        return DEFAULT_ADMIN_PASSWORD;
    }
}